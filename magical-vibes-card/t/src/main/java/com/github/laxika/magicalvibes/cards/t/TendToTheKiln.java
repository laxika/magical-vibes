package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YECL", collectorNumber = "13")
public class TendToTheKiln extends Card {

    public TendToTheKiln() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PerpetuallyGrantCardCharacteristicsEffect(
                        Set.of(CardType.KINDRED), Set.of(CardSubtype.ELEMENTAL), List.of())
                        .to(Subjects.cardsYouOwn(new CardAllOfPredicate(List.of(
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY))),
                                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.ELEMENTAL)))))));

        addTrigger(Triggers.youCastSpell().withFilter(new CardSubtypePredicate(CardSubtype.ELEMENTAL)),
                PutCountersEffect.onSelf(CounterType.FLAME),
                new ConditionalEffect(
                        new SourceCounterThreshold(3, CounterType.FLAME),
                        SequenceEffect.of(
                                RemoveCountersEffect.allFromSelf(CounterType.FLAME),
                                ConjureCardsEffect.named("ECL", "139", ConjureDestination.ontoBattlefield()),
                                new GrantKeywordEffect(
                                        Keyword.HASTE,
                                        Subjects.createdThisResolution(),
                                        Duration.PERMANENT),
                                new GrantEffectToTargetEffect(
                                        new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                                                new SacrificeSelfEffect(true)),
                                        Duration.PERMANENT, false, Subjects.createdThisResolution())
                )));
    }
}
