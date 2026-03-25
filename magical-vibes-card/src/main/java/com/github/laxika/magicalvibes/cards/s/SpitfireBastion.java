package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

/**
 * Spitfire Bastion — back face of Vance's Blasting Cannons // Spitfire Bastion.
 * Legendary Land.
 * (Transforms from Vance's Blasting Cannons.)
 * {T}: Add {R}.
 * {2}{R}, {T}: Spitfire Bastion deals 3 damage to any target.
 */
public class SpitfireBastion extends Card {

    public SpitfireBastion() {
        // {T}: Add {R}.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.RED)),
                "{T}: Add {R}."
        ));

        // {2}{R}, {T}: Spitfire Bastion deals 3 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}{R}",
                List.of(new DealDamageToAnyTargetEffect(3)),
                "{2}{R}, {T}: Spitfire Bastion deals 3 damage to any target."
        ));
    }
}
