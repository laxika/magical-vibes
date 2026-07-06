package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ISD", collectorNumber = "121")
public class UnbreathingHorde extends Card {

    public UnbreathingHorde() {
        // Unbreathing Horde enters the battlefield with a +1/+1 counter on it for each
        // other Zombie you control and each Zombie card in your graveyard.
        // (It isn't on the battlefield yet while the amount is evaluated, so the
        // battlefield count naturally covers only the *other* Zombies.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new Sum(
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER),
                        new CardsInGraveyard(new CardSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER))));

        // If damage would be dealt to Unbreathing Horde, prevent that damage and
        // remove a +1/+1 counter from it.
        addEffect(EffectSlot.STATIC, new PreventDamageAndRemovePlusOnePlusOneCountersEffect(true));
    }
}
