package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "282")
public class KryShield extends Card {

    public KryShield() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        PreventDamageEffect.allByTargetCreatures(),
                        new BoostTargetCreatureEffect(new Fixed(0), new TargetManaValue())
                ),
                "{2}, {T}: Prevent all damage that would be dealt this turn by target creature you control. That creature gets +0/+X until end of turn, where X is its mana value.",
                TargetFilters.creatureYouControl()
        ));
    }
}
