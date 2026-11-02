package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfCasualtyPaidEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "83")
public class IllicitShipment extends Card {

    public IllicitShipment() {
        addTrigger(Triggers.youCastThisSpell(), new CopyThisSpellIfCasualtyPaidEffect());
        addCastingCost(new CasualtyCost(3));
        addSpellEffect(new SearchLibraryEffect());
    }
}
