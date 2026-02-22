package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "348")
public class BattlefieldForge extends Card {

    public BattlefieldForge() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                false,
                "{T}: Add {C}."
        ));
        // {T}: Add {R}. Battlefield Forge deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED), new DealDamageToControllerEffect(1)),
                false,
                "{T}: Add {R}. Battlefield Forge deals 1 damage to you."
        ));
        // {T}: Add {W}. Battlefield Forge deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE), new DealDamageToControllerEffect(1)),
                false,
                "{T}: Add {W}. Battlefield Forge deals 1 damage to you."
        ));
    }
}
