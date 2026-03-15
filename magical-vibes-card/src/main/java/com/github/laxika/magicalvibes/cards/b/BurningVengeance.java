package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastFromGraveyardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "133")
public class BurningVengeance extends Card {

    public BurningVengeance() {
        // Whenever you cast a spell from your graveyard, Burning Vengeance deals 2 damage to any target.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CastFromGraveyardTriggerEffect(
                List.of(new DealDamageToAnyTargetEffect(2))
        ));
    }
}
