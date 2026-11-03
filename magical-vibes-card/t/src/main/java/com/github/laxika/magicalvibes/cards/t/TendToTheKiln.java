package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "13")
public class TendToTheKiln extends Card {

    public TendToTheKiln() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect(
                        new CardAllOfPredicate(List.of(
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY))),
                                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.ELEMENTAL)))),
                        CardType.KINDRED,
                        CardSubtype.ELEMENTAL));

        addTrigger(Triggers.youCastSpell().withFilter(new CardSubtypePredicate(CardSubtype.ELEMENTAL)),
                new PutCountersOnSelfEffect(CounterType.FLAME),
                new ConditionalEffect(
                        new SourceCounterThreshold(3, CounterType.FLAME),
                        SequenceEffect.of(
                                new RemoveAllCountersEffect(CounterType.FLAME),
                                new ConjureCardToBattlefieldEffect("ECL", "139"),
                                new GrantKeywordEffect(
                                        Keyword.HASTE,
                                        GrantScope.TOKENS_CREATED_THIS_RESOLUTION,
                                        GrantDuration.INDEFINITE),
                                new GrantTriggeredAbilityToCreatedPermanentsEffect(
                                        new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                                                new SacrificeSelfEffect(true)))
                )));
    }
}
