package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.GrantFlyingToTargetCreatureOrPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "51")
@CardRegistration(set = "MSC", collectorNumber = "361")
public class FlameOn extends Card {

    public FlameOn() {
        CardsInGraveyard noncreatureNonlandCards = new CardsInGraveyard(
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))
                )), CountScope.CONTROLLER);

        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, noncreatureNonlandCards))
                .addEffect(EffectSlot.SPELL, new GrantFlyingToTargetCreatureOrPlayerEffect());
    }
}
