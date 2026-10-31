package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.InventoryManagementEffect;

@CardRegistration(set = "PIP", collectorNumber = "105")
@CardRegistration(set = "PIP", collectorNumber = "342")
@CardRegistration(set = "PIP", collectorNumber = "633")
@CardRegistration(set = "PIP", collectorNumber = "870")
public class InventoryManagement extends Card {

    public InventoryManagement() {
        addSpellEffect(new InventoryManagementEffect());
    }
}
