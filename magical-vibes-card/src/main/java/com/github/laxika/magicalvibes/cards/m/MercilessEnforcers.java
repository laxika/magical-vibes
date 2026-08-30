package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "58")
public class MercilessEnforcers extends Card {

    public MercilessEnforcers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)),
                "{3}{B}: This creature deals 1 damage to each opponent."
        ));
    }
}
