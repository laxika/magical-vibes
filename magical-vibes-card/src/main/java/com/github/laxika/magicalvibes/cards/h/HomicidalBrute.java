package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DidntAttackConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndTransformSelfEffect;

/**
 * Homicidal Brute — back face of Civilized Scholar.
 * 5/1 Human Mutant.
 * At the beginning of your end step, if Homicidal Brute didn't attack this turn,
 * tap Homicidal Brute, then transform it.
 */
public class HomicidalBrute extends Card {

    public HomicidalBrute() {
        // At the beginning of your end step, if this creature didn't attack this turn,
        // tap it, then transform it.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new DidntAttackConditionalEffect(new TapAndTransformSelfEffect()));
    }
}
