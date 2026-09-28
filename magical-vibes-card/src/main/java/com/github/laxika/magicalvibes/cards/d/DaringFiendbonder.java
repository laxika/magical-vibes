package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ActivationTimingRestriction.SORCERY_SPEED;

@CardRegistration(set = "C20", collectorNumber = "41")
public class DaringFiendbonder extends Card {

    public DaringFiendbonder() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.INDESTRUCTIBLE, 1)),
                "{1}{B}, Exile this card from your graveyard: Put an indestructible counter on target creature. "
                        + "Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                SORCERY_SPEED));
    }
}
