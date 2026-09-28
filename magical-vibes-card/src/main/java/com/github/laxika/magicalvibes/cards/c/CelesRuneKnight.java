package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFromGraveyardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "FIC", collectorNumber = "1")
@CardRegistration(set = "FIC", collectorNumber = "167")
@CardRegistration(set = "FIC", collectorNumber = "201")
@CardRegistration(set = "FIC", collectorNumber = "209")
@CardRegistration(set = "FIC", collectorNumber = "220")
public class CelesRuneKnight extends Card {

    public CelesRuneKnight() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardUpToThenDrawThatManyEffect(DiscardUpToThenDrawThatManyEffect.ANY_NUMBER, 1));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD,
                new EnteringCreatureFromGraveyardConditionalEffect(
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())));
    }
}
