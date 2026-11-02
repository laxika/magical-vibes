package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopTenCardsForColorPairsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "208")
@CardRegistration(set = "MUL", collectorNumber = "53")
@CardRegistration(set = "MUL", collectorNumber = "118")
@CardRegistration(set = "MUL", collectorNumber = "183")
public class NivMizzetReborn extends Card {

    public NivMizzetReborn() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RevealTopTenCardsForColorPairsEffect());
    }
}
