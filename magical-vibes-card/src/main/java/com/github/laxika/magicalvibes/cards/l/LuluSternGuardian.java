package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "38")
@CardRegistration(set = "FIC", collectorNumber = "143")
public class LuluSternGuardian extends Card {

    public LuluSternGuardian() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.ON_CREATURES_ATTACK_YOU,
                        new PutCounterOnTargetPermanentEffect(CounterType.STUN));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new ProliferateEffect()),
                "{3}{U}: Proliferate."
        ));
    }
}
