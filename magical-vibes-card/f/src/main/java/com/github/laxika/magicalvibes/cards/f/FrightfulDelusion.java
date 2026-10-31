package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDiscardsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "57")
public class FrightfulDelusion extends Card {

    public FrightfulDelusion() {
        // The discard follows the pay-or-counter decision (CR 608.2c), so it rides on both outcomes
        // of the counter-unless-pays effect and acts on the spell's controller ("that player").
        List<CardEffect> thatPlayerDiscards = List.of(new TargetSpellControllerDiscardsEffect(1));
        addSpellEffect(new CounterUnlessPaysEffect(1, false, false, null, thatPlayerDiscards,
                0, null, thatPlayerDiscards));
    }
}
