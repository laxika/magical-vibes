package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensWithSameNameAsLeavingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

@CardRegistration(set = "PCY", collectorNumber = "112")
public class DualNature extends Card {

    public DualNature() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        CreateTokenCopyOfTargetPermanentEffect.trackedForTargetController()));
        addEffect(EffectSlot.ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD,
                new ExileTokensWithSameNameAsLeavingCreatureEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ExileTokensCreatedWithSourceEffect());
    }
}
