package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "OTJ", collectorNumber = "112")
public class UnscrupulousContractor extends Card {

    public UnscrupulousContractor() {
        // When this creature enters, you may sacrifice a creature. When you do, target player
        // draws two cards and loses 2 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentIsCreaturePredicate(),
                        SequenceEffect.of(
                                new DrawCardForTargetPlayerEffect(2),
                                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER)),
                        "a creature"),
                "Sacrifice a creature?"));
    }
}
