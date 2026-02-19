package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityToEnchantedCreatureEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "200")
public class Firebreathing extends Card {

    public Firebreathing() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityToEnchantedCreatureEffect(
                new ActivatedAbility(
                        false,
                        "{R}",
                        List.of(new BoostSelfEffect(1, 0)),
                        false,
                        "{R}: This creature gets +1/+0 until end of turn."
                )
        ));
    }
}
