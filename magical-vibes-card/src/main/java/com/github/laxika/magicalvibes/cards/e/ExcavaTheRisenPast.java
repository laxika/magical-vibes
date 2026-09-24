package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateReturnedPermanentIfSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "2")
public class ExcavaTheRisenPast extends Card {

    public ExcavaTheRisenPast() {
        CardPredicate returnableCard = new CardAllOfPredicate(List.of(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.CREATURE),
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ENCHANTMENT),
                                new CardNotPredicate(new CardIsAuraPredicate()))))),
                new CardMaxManaValuePredicate(3)));

        addEffect(EffectSlot.ON_ATTACK,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(returnableCard)
                        .targetGraveyard(true)
                        .upTo(true)
                        .enterWithCounter(CounterType.FINALITY)
                        .enterWithCounterCount(1)
                        .grantSubtype(CardSubtype.SPIRIT)
                        .build());
        addEffect(EffectSlot.ON_ATTACK, new AnimateReturnedPermanentIfSubtypeEffect(
                CardSubtype.SPIRIT,
                new AnimatePermanentsEffect(1, 1, List.of(CardSubtype.SPIRIT),
                        Set.of(Keyword.FLYING), null, Set.of(), GrantScope.TARGET,
                        EffectDuration.PERMANENT)));
    }
}
