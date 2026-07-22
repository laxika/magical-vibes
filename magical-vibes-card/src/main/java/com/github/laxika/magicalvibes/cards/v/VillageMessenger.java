package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MoonriseIntruder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "INR", collectorNumber = "179")
public class VillageMessenger extends Card {

    public VillageMessenger() {
        // Set up back face
        MoonriseIntruder backFace = new MoonriseIntruder();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Haste is loaded from Scryfall.

        // At the beginning of each upkeep, if no spells were cast last turn, transform Village Messenger.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "MoonriseIntruder";
    }
}
