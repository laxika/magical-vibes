package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "205")
public class OrcishCannoneers extends Card {

    public OrcishCannoneers() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(2), new DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER)),
                "{T}: Orcish Cannoneers deals 2 damage to any target and 3 damage to you."
        ));
    }
}
