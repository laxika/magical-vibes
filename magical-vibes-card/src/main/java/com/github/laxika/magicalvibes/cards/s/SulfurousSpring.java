package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "359")
public class SulfurousSpring extends Card {

    public SulfurousSpring() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {T}: Add {B}. Sulfurous Springs deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLACK), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{T}: Add {B}. Sulfurous Springs deals 1 damage to you."
        ));
        // {T}: Add {R}. Sulfurous Springs deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED), new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)),
                "{T}: Add {R}. Sulfurous Springs deals 1 damage to you."
        ));
    }
}
