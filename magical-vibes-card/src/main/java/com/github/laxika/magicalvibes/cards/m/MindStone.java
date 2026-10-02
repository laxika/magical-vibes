package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "335")
@CardRegistration(set = "WTH", collectorNumber = "153")
@CardRegistration(set = "DD2", collectorNumber = "22")
@CardRegistration(set = "DDP", collectorNumber = "65")
@CardRegistration(set = "JVC", collectorNumber = "22")
@CardRegistration(set = "MB1", collectorNumber = "210")
@CardRegistration(set = "IMA", collectorNumber = "219")
@CardRegistration(set = "HA1", collectorNumber = "19")
@CardRegistration(set = "DMR", collectorNumber = "232")
@CardRegistration(set = "SOC", collectorNumber = "352")
@CardRegistration(set = "C14", collectorNumber = "250")
@CardRegistration(set = "C15", collectorNumber = "259")
@CardRegistration(set = "WHO", collectorNumber = "244")
@CardRegistration(set = "WHO", collectorNumber = "835")
@CardRegistration(set = "PIP", collectorNumber = "235")
@CardRegistration(set = "PIP", collectorNumber = "763")
@CardRegistration(set = "MOC", collectorNumber = "364")
@CardRegistration(set = "C21", collectorNumber = "251")
@CardRegistration(set = "40K", collectorNumber = "244")
@CardRegistration(set = "40K", collectorNumber = "245")
@CardRegistration(set = "DSC", collectorNumber = "248")
@CardRegistration(set = "LTC", collectorNumber = "282")
@CardRegistration(set = "MKC", collectorNumber = "232")
@CardRegistration(set = "AFC", collectorNumber = "211")
@CardRegistration(set = "LCC", collectorNumber = "309")
@CardRegistration(set = "BLC", collectorNumber = "280")
@CardRegistration(set = "C18", collectorNumber = "210")
public class MindStone extends Card {

    public MindStone() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {1}, {T}, Sacrifice Mind Stone: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}, {T}, Sacrifice Mind Stone: Draw a card."
        ));
    }
}
