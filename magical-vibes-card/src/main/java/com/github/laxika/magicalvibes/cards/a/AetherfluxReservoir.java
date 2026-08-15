package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "192")
public class AetherfluxReservoir extends Card {

    public AetherfluxReservoir() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new GainLifeEffect(new SpellsCastThisTurn(CountScope.CONTROLLER)))
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(50), new DealDamageToAnyTargetEffect(50)),
                "Pay 50 life: Aetherflux Reservoir deals 50 damage to any target."
        ));
    }
}
