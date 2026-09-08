package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "122")
public class BiogenicOoze extends Card {

    public BiogenicOoze() {
        CreateTokenEffect oozeToken = new CreateTokenEffect(
                "Ooze", 2, 2, CardColor.GREEN, List.of(CardSubtype.OOZE), Set.of(), Set.of());

        // When this creature enters, create a 2/2 green Ooze creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, oozeToken);

        // At the beginning of your end step, put a +1/+1 counter on each Ooze you control.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentHasSubtypePredicate(CardSubtype.OOZE)));

        // {1}{G}{G}{G}: Create a 2/2 green Ooze creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}{G}{G}",
                List.of(oozeToken),
                "{1}{G}{G}{G}: Create a 2/2 green Ooze creature token."
        ));
    }
}
