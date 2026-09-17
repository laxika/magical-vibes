package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.AmplifyEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "104")
@CardRegistration(set = "DDG", collectorNumber = "59")
public class KilnmouthDragon extends Card {

    public KilnmouthDragon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AmplifyEffect(
                3, new CardSubtypePredicate(CardSubtype.DRAGON)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(
                        new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE))),
                "{T}: This creature deals damage equal to the number of +1/+1 counters on it to any target."
        ));
    }
}
