package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MoonriseIntruder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "179")
@CardRegistration(set = "INR", collectorNumber = "466")
@CardRegistration(set = "SOI", collectorNumber = "190")
@CardRegistration(set = "SIR", collectorNumber = "184")
public class VillageMessenger extends Card {

    public VillageMessenger() {
        setBackFaceCard(new MoonriseIntruder());

        // Haste is loaded from Scryfall.

        // At the beginning of each upkeep, if no spells were cast last turn, transform Village Messenger.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "MoonriseIntruder";
    }
}
