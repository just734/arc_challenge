package com.company;

import java.util.*;

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

    public UsedMaterial(String matCode, String name, AmountType amountType, Double weight, Double price, Double amount){
        materialCode = matCode;
        materialName = name;
        materialAmountType = amountType;
        totalWeight = weight;
        totalPrice = price;
        usedAmount = amount;
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

        //main code
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

                if (mat.materialAmountType == AmountType.pieces && amount%1!=0) {
                    System.out.println("\nTaneyle ölçülen bir materyale ondalık giriş yaptınız. Lütfen malzeme kodu ve miktarlarının eşleşmesine dikkat edin.\n");
                    Thread.sleep(1500);
                    continue mainLoop;
                }//if a decimal was entered as amount for a material that is measured in pieces

                selectedMaterials[i] = new UsedMaterial(mat.materialCode, mat.materialName, mat.materialAmountType, mat.materialWeight*amount, mat.materialPrice*amount, amount); //add the selected material as an object to the array
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

            }

            cont = false;
        }
    }
}
