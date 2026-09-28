package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBecomeArtifactCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "15")
public class KavaronConsumed extends Card {

    public KavaronConsumed() {
        addEffect(EffectSlot.SPELL, new MayEffect(
                new PutCardToBattlefieldThenEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE))),
                        "artifact or creature",
                        null,
                        SequenceEffect.of(
                                new PerpetuallyBecomeArtifactCreatureEffect(4, 4),
                                new SacrificeSelfAtEndStepEffect())),
                "Put an artifact or creature card from your hand onto the battlefield?"));
    }
}
