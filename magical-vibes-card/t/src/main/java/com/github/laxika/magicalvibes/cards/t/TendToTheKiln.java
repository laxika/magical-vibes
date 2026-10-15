package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "13")
public class TendToTheKiln extends Card {

    public TendToTheKiln() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect(
                        new CardAllOfPredicate(List.of(
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY))),
                                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.ELEMENTAL)))),
                        CardType.KINDRED,
                        CardSubtype.ELEMENTAL));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardSubtypePredicate(CardSubtype.ELEMENTAL),
                List.of(
                        new PutCountersOnSelfEffect(CounterType.FLAME),
                        new ConditionalEffect(
                                new SourceCounterThreshold(3, CounterType.FLAME),
                                SequenceEffect.of(
                                        new RemoveAllCountersEffect(CounterType.FLAME),
                                        new ConjureCardToBattlefieldEffect("Flamebraider"),
                                        new GrantKeywordEffect(
                                                Keyword.HASTE,
                                                GrantScope.TOKENS_CREATED_THIS_RESOLUTION),
                                        new SacrificeCreatedPermanentsAtEndStepEffect()
                                )))));
    }
}
