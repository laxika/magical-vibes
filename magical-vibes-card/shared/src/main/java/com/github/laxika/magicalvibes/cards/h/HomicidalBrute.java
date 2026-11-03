package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new DidntAttack()),
                SequenceEffect.of(new TapPermanentsEffect(TapUntapScope.SELF), new TransformSelfEffect()));
    }
}
