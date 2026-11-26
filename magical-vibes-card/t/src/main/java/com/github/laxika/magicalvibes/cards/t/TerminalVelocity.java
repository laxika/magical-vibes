package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourceManaValueMinusOne;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "163")
public class TerminalVelocity extends Card {

    public TerminalVelocity() {
        DealDamageEffect leavesDamage = DealDamageEffect.toEachCreature(new Sum(new SourceManaValueMinusOne(), new Fixed(1)));
        addSpellEffect(new MayEffect(
                new PutCardToBattlefieldThenEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE))),
                        "artifact or creature",
                        null,
                        SequenceEffect.of(
                                new GrantKeywordEffect(Keyword.HASTE, Subjects.source(), Duration.PERMANENT),
                                GrantEffectToTargetEffect.toSourcePermanent(
                                        new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS), leavesDamage)),
                                GrantEffectToTargetEffect.toSourcePermanent(new TriggeredAbility(
                                        Triggers.endStep(ActivePlayer.YOU), new SacrificeSelfEffect())))),
                "Put an artifact or creature card from your hand onto the battlefield?"));
    }
}
