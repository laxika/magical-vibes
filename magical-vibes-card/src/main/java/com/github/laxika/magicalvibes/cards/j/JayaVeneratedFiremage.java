package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalColorSourceDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "135")
public class JayaVeneratedFiremage extends Card {

    public JayaVeneratedFiremage() {
        addEffect(EffectSlot.STATIC, new AdditionalColorSourceDamageEffect(1, CardColor.RED));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new DealDamageToAnyTargetEffect(2)),
                "−2: Jaya deals 2 damage to any target."
        ));
    }
}
