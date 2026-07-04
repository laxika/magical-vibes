package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DidntGainLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SOS", collectorNumber = "102")
public class TragedyFeaster extends Card {

    public TragedyFeaster() {
        // Trample is auto-loaded from Scryfall.

        // Ward—Discard a card. (Whenever this creature becomes the target of a spell or ability
        // an opponent controls, counter it unless that player discards a card.)
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessDiscardsEffect());

        // Infusion — At the beginning of your end step, sacrifice a permanent unless you gained
        // life this turn. The sacrifice only happens at resolution if you didn't gain life.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new DidntGainLifeThisTurn(),
                new SacrificePermanentThenEffect(new PermanentTruePredicate(), null, "a permanent")));
    }
}
