package com.company;

import java.util.*;
import java.math.BigDecimal; //we are importing big decimal so that our decimal calculations don't mess up when working with small numbers to calculate item removals

//the types of units for the mats
enum AmountType {
    pieces,
    metres,
    squareMeters,
}

//a material class for easy creation of material instances later on
class Material {
    String materialCode;
    String materialName;
    AmountType materialAmountType;
    Double materialWeight;
    Double materialPrice;

    public Material(String matCode, String name, AmountType amountType, Double weight, Double price){
        materialCode = matCode;
        materialName = name;
        materialAmountType = amountType;
        materialWeight = weight;
        materialPrice = price;
    }
}

//creating a separate class for the material after selection, probably unnecessary but a lot easier
class UsedMaterial{
    String materialCode;
    String materialName;
    AmountType materialAmountType;
    Double totalWeight;
    Double totalPrice;
    Double usedAmount;
    Double materialWeight;

    public UsedMaterial(String matCode, String name, AmountType amountType, Double weight, Double price, Double amount, Double weightPer){
        materialCode = matCode;
        materialName = name;
        materialAmountType = amountType;
        totalWeight = weight;
        totalPrice = price;
        usedAmount = amount;
        materialWeight = weightPer;
    }
}

//creating comparators for the selected materials, one compares price and the other compares weight
class PriceComparator implements Comparator<UsedMaterial> {
    @Override
    public int compare(UsedMaterial a, UsedMaterial b) {
        return a.totalPrice.compareTo(b.totalPrice);
    }
}

class WeightComparator implements Comparator<UsedMaterial> {
    @Override
    public int compare(UsedMaterial a, UsedMaterial b) {
        return a.totalWeight.compareTo(b.totalWeight);
    }
}

//creating a new class for the objects that will represent our removed materials, this is so that we don't have calculate again after finding the mods efficient material
//we use big decimal so that we will be able to calculate accurately
class MatToRemove {
    String materialCode;
    String materialName;
    AmountType materialAmountType;
    BigDecimal reachedWeight;
    BigDecimal removedWeight;
    BigDecimal amountToUse;
    BigDecimal removedAmount;
    BigDecimal materialWeight;

    public MatToRemove(String matCode, String name, AmountType amountType, BigDecimal weight, BigDecimal weightToRemove, BigDecimal amount, BigDecimal amountToRemove){
        materialCode = matCode;
        materialName = name;
        materialAmountType = amountType;
        reachedWeight = weight;
        removedWeight = weightToRemove;
        amountToUse = amount;
        removedAmount = amountToRemove;
    }
}

//this is so that we can sort the array of removal options later on
class RemovalComparator implements Comparator<MatToRemove> {
    @Override
    public int compare(MatToRemove a, MatToRemove b) {
        return a.reachedWeight.compareTo(b.reachedWeight);
    }
}

public class Main {

    public static void main(String[] args) throws InterruptedException {
        //constants
        Material[] materials = {
                new Material("4.1", "Encoder", AmountType.pieces, 0.01, 20.0),
                new Material("4.2M", "Magnetic Encoder", AmountType.pieces, 0.03, 50.0),
                new Material("3.1", "Policarbon Plaka", AmountType.squareMeters, 0.5, 200.0),
                new Material("3.2-2", "Alüminyum 2024 Plaka", AmountType.squareMeters, 3.5, 30.0),
                new Material("3.2-4", "Alüminyum 4145 Plaka", AmountType.squareMeters, 4.5, 50.0),
                new Material("2.Omni", "Omniwheel", AmountType.pieces, 0.04, 30.0),
                new Material("2.'6in'", "6 in. wheel", AmountType.pieces, 0.03, 20.0),
                new Material("2.'4in'", "4 in. wheel", AmountType.pieces, 0.02, 15.0),
                new Material("1/NavX", "NavX", AmountType.pieces, 0.01, 200.0),
                new Material("1/RIO", "RoboRIO", AmountType.pieces, 1.0, 250.0),
                new Material("1/Modem", "Modem", AmountType.pieces, 0.2, 50.0),
                new Material("5: CB", "Circuit Breaker", AmountType.pieces, 1.5, 20.0),
                new Material("5: RSL'", "Robot Signal Light", AmountType.pieces, 0.5, 80.0),
                new Material("6.'16'", "16 AWG Wire", AmountType.metres, 0.25, 2.2),
                new Material("6.'18'", "18 AWG Wire", AmountType.metres, 0.35, 0.75),
        }; //create an array with all mats in it

        Map<String, Material> MaterialCatalog = new HashMap<String, Material>(); //create a map that will allow us to get materials according to their code

        for (Material i : materials) {
            MaterialCatalog.put(i.materialCode, i);
        } //put all of the materials in the map

        final double weightLimit = 52.2; //kgs

        Scanner scanner = new Scanner(System.in);

        //variables
        boolean cont = true;

        //the loop for the dialogue in the terminal, the logic will go here
        mainLoop:
        while (cont) {
            //print the list of materials
            System.out.println("Robot Dizayn Ölçüt Programı \n---------------------------\n\nMalzemeler:\n\nmalzemeKodu - malzemeİsmi - malzemeÖlçüBirimi - malzemeFiyatı - malzemeAğırlığı");
            for (Material mat: materials) {
                String[] message = {mat.materialCode, mat.materialName, mat.materialAmountType.toString(), mat.materialWeight.toString() + "kg", mat.materialPrice.toString() + "$"};
                System.out.println(String.join(" - ",message));
            }//print out the material list

            //get the desired materials and amounts
            System.out.println("\nLütfen kullandığınız malzemelerin kodunu virgülle (, ) ayırarak girin:");
            String input1 = scanner.nextLine();
            String[] matSelections = input1.split(", ");

            System.out.println("\nLütfen istediğiniz miktarı malzemeyle aynı sırada virgülle (, ) ayırarak girin. Ölçü birimlerine dikkat edin.");
            String input2 = scanner.nextLine();
            String[] matAmounts = input2.split(", ");

            if (matSelections.length != matAmounts.length) {
                System.out.println("\nFarklı sayıda giriş yaptınız. Lütfen malzeme kodu ve miktarlarının eşleşmesine dikkat edin.\n");
                Thread.sleep(1500);
                continue mainLoop;
            }//check if the number of inputs match for the selections and the amount of materials

            UsedMaterial[] selectedMaterials = new UsedMaterial[matSelections.length];
            double totalWeight = 0.0;
            double totalPrice = 0.0;

            for(int i = 0; i< matSelections.length; i++) {
                double amount;
                Material mat = MaterialCatalog.get(matSelections[i]); //get the current selection for the index

                if (mat==null) {
                    System.out.println("\nGirdiğiniz materyal kodu yanlış.\n");
                    Thread.sleep(1500);
                    continue mainLoop;
                }

                try {
                    amount = Double.parseDouble(matAmounts[i]); //get the current amount for the index
                }catch (NumberFormatException exc) {
                    System.out.println("\nGirdiğiniz miktarlar sayı değil. Lütfen doğru türde giriş yapın.\n");
                    Thread.sleep(1500);
                    continue mainLoop;
                }//if there is an amount that is not a number, catch the error during conversion

                if (amount <= 0) {
                    System.out.println("\nGirdiğiniz miktarlar pozitif olmalı. Lütfen doğru türde giriş yapın. Eğer sıfır yazacaksanız o materyale giriş yapmayın.\n");
                    Thread.sleep(1500);
                    continue mainLoop;
                }

                if (mat.materialAmountType == AmountType.pieces && amount%1!=0) {
                    System.out.println("\nTaneyle ölçülen bir materyale ondalık giriş yaptınız. Lütfen malzeme kodu ve miktarlarının eşleşmesine dikkat edin.\n");
                    Thread.sleep(1500);
                    continue mainLoop;
                }//if a decimal was entered as amount for a material that is measured in pieces

                selectedMaterials[i] = new UsedMaterial(mat.materialCode, mat.materialName, mat.materialAmountType, mat.materialWeight*amount, mat.materialPrice*amount, amount, mat.materialWeight); //add the selected material as an object to the array
            }//loop through all selected materials

            //sort the arrays to print them
            UsedMaterial[] sortedByPrice = selectedMaterials;
            Arrays.sort(sortedByPrice, new PriceComparator());
            Collections.reverse(Arrays.asList(sortedByPrice));

            UsedMaterial[] sortedByWeight = selectedMaterials;
            Arrays.sort(sortedByWeight, new WeightComparator());

            System.out.println("\nPahalıdan ucuza seçtiğiniz malzemeler:");
            for (int i=sortedByPrice.length-1;i>-1;i--) {
                UsedMaterial mat = sortedByPrice[i];
                System.out.println(mat.materialCode + " - " + mat.materialName + " - " + mat.usedAmount + "birim - " + mat.totalPrice + "$");
            }

            System.out.println("\nEn hafif malzemeler:");
            for (int i=0;i<5 && i< sortedByWeight.length;i++) {
                UsedMaterial mat = sortedByWeight[i];
                System.out.println(mat.materialCode + " - " + mat.materialName + " - " + mat.usedAmount + "birim - " + mat.totalWeight + "kg");
            }

            //calculate the total weight and price
            for (UsedMaterial i : selectedMaterials) {
                totalWeight += i.totalWeight;
                totalPrice += i.totalPrice;
            }

            //check for the weight limit
            if (totalWeight > weightLimit) {
                System.out.println("\nAğırlık limitinin üstündesiniz. "+totalWeight+"kg malzeme kullandınız ama maksimum "+weightLimit+"kg malzeme kullanabilirsiniz.");
            }else {
                System.out.println("\nAğırlık limitinin üstünde değilsiniz.");
            }

            //print out the total price
            System.out.println("\nToplam "+totalPrice+"$ harcama yaptınız.");

            //this part is for finding out the material to remove, will be done later
            if (totalWeight > weightLimit) {
                //create a list for all possible solo materials you can erase to get down from the limit without running out
                MatToRemove[] itemsToRemove = new MatToRemove[sortedByPrice.length];
                //loop through all selected materials
                for (int x = 0; x<sortedByPrice.length; x++) {
                    //create corresponding values for the variables of the object we will use for the material to remove
                    UsedMaterial i = sortedByPrice[x];
                    BigDecimal remainingWeight = BigDecimal.valueOf(totalWeight);
                    BigDecimal remainingAmount = BigDecimal.valueOf(i.usedAmount);
                    BigDecimal weightPerPiece = BigDecimal.valueOf(i.materialWeight);
                    BigDecimal removedWeight = BigDecimal.valueOf(0.0);
                    BigDecimal removedAmount = BigDecimal.valueOf(0.0);
                    //if it's a piece, we can just go down one by one until we get down from the limit
                    if (i.materialAmountType==AmountType.pieces){
                            while (remainingWeight.compareTo(BigDecimal.valueOf(weightLimit)) == 1) {
                                remainingAmount=remainingAmount.subtract(BigDecimal.valueOf(1));
                                removedAmount=removedAmount.add(BigDecimal.valueOf(1));;
                                remainingWeight = remainingWeight.subtract(weightPerPiece);
                                removedWeight= removedWeight.add(weightPerPiece);
                            }
                    }
                    //if not, we will use a hundredth of it since realistically you won't be using/purchasing material my millimeters or square-millimeters
                    else {
                        while (remainingWeight.compareTo(BigDecimal.valueOf(weightLimit)) == 1) {
                            remainingAmount = remainingAmount.subtract(BigDecimal.valueOf(0.01));
                            removedAmount = removedAmount.add(BigDecimal.valueOf(0.01));
                            remainingWeight = remainingWeight.subtract(weightPerPiece.multiply(BigDecimal.valueOf(0.01)));
                            removedWeight = removedWeight.add(weightPerPiece.multiply(BigDecimal.valueOf(0.01)));
                        }
                    }
                    //if it didn't run out before getting down to the limit, add it to the list
                    if (remainingWeight.compareTo(BigDecimal.valueOf(0))>-1) {
                        itemsToRemove[x] = new MatToRemove(i.materialCode, i.materialName, i.materialAmountType, remainingWeight, removedWeight, remainingAmount, removedAmount);
                    }
                }
                //sort the list with the comparator we created so that it is ordered in ascending order of possible weights
                Arrays.sort(itemsToRemove, new RemovalComparator());
                //get the last, therefore biggest element of the array. this will be our closest result to the limit
                MatToRemove itemToRemove = itemsToRemove[itemsToRemove.length-1];

                //print out the result
                System.out.println("\nMateryal çıkarma önerisi:");
                System.out.println(itemToRemove.materialCode+" kodlu, " + itemToRemove.materialName +" adlı materyalden "+ itemToRemove.removedAmount + " birim çıkararak " + itemToRemove.reachedWeight + "kg ağırlığına ulaşabilirsiniz. Bu ağırlık limitini geçmeden kalabileceğiniz en yüksek ağırlıkdır. Buna ulaşmak için "+itemToRemove.removedWeight+"kg materyal çıkarılmıştır. "+itemToRemove.amountToUse+" birim materyal hala kullanımda.");
            }

            //ask the user if they want to use the program from the start again
            System.out.println("\nProgramı tekrar kullanmak ister misiniz? (Y/N)");
            String constStr = scanner.nextLine();

            if (constStr.equals("y") || constStr.equals("Y")) {
                System.out.println("Tekrar başlatılıyor.");
                Thread.sleep(500);
                try //clear screen if they want to restart
                {
                    final String os = System.getProperty("os.name");
                    if (os.contains("Windows"))
                    {
                        Runtime.getRuntime().exec("cls");
                    }
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            } else if (constStr.equals("n") || constStr.equals("N")) {
                cont = false;
            } else {
                System.out.println("Geçersiz giriş yaptınız. Çıkmak istediğiniz varsayıldı.");
                cont = false;
            }
        }
    }
}
