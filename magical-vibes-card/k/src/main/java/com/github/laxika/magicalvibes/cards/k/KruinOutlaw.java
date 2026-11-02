package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TerrorOfKruinPass;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "152")
@CardRegistration(set = "INR", collectorNumber = "161")
@CardRegistration(set = "SIS", collectorNumber = "42")
public class KruinOutlaw extends Card {

    public KruinOutlaw() {
        setBackFaceCard(new TerrorOfKruinPass());

        // First strike is loaded from Scryfall.

        // At the beginning of each upkeep, if no spells were cast last turn, transform Kruin Outlaw.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new ConditionalEffect(new NoSpellsCastLastTurn(), new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "TerrorOfKruinPass";
    }
}
