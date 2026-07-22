package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

/**
 * Bloodsoaked Reveler — back face of Restless Bloodseeker.
 */
public class BloodsoakedReveler extends Card {

    public BloodsoakedReveler() {
        // At the beginning of your end step, if you gained life this turn, create a Blood token.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(),
                CreateTokenEffect.ofBloodToken(1)));

        // {4}{B}: Each opponent loses 2 life and you gain 2 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}",
                List.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2)),
                "{4}{B}: Each opponent loses 2 life and you gain 2 life."
        ));
    }
}
