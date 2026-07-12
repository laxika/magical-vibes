package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "93")
public class FurystokeGiant extends Card {

    public FurystokeGiant() {
        // When this creature enters, other creatures you control gain
        // "{T}: This creature deals 2 damage to any target" until end of turn.
        // (Persist is auto-loaded from Scryfall and handled by the engine.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new DealDamageToAnyTargetEffect(2)),
                        "{T}: This creature deals 2 damage to any target."
                ),
                GrantScope.OWN_CREATURES,
                null,
                EffectDuration.UNTIL_END_OF_TURN
        ));
    }
}
