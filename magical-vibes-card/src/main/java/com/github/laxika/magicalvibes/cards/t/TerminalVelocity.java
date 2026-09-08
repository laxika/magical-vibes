package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourceManaValueMinusOne;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "163")
public class TerminalVelocity extends Card {

    public TerminalVelocity() {
        MassDamageEffect leavesDamage = new MassDamageEffect(
                new Sum(new SourceManaValueMinusOne(), new Fixed(1)), false, false, null);
        addEffect(EffectSlot.SPELL, new MayEffect(
                new PutCardToBattlefieldThenEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE))),
                        "artifact or creature",
                        null,
                        SequenceEffect.of(
                                new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF, GrantDuration.INDEFINITE),
                                GrantEffectToTargetEffect.toSourcePermanent(
                                        EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, leavesDamage),
                                new SacrificeSelfAtEndStepEffect())),
                "Put an artifact or creature card from your hand onto the battlefield?"));
    }
}
