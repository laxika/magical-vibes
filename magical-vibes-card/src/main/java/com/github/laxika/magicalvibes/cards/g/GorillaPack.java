package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "247")
public class GorillaPack extends Card {

    public GorillaPack() {
        // "This creature can't attack unless defending player controls a Forest."
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                "a Forest"
        ));

        // "When you control no Forests, sacrifice this creature." —
        // State-triggered ability (MTG rule 603.8).
        addEffect(EffectSlot.STATE_TRIGGERED, StateTriggerEffect.whenBattlefieldHasAtMost(0,
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.FOREST))),
                List.of(new SacrificeSelfEffect()),
                "Gorilla Pack's state-triggered ability"));
    }
}
