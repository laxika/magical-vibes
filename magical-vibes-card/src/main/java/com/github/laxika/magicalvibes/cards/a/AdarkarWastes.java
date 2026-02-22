package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "347")
public class AdarkarWastes extends Card {

    public AdarkarWastes() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                false,
                "{T}: Add {C}."
        ));
        // {T}: Add {W}. Adarkar Wastes deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE), new DealDamageToControllerEffect(1)),
                false,
                "{T}: Add {W}. Adarkar Wastes deals 1 damage to you."
        ));
        // {T}: Add {U}. Adarkar Wastes deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE), new DealDamageToControllerEffect(1)),
                false,
                "{T}: Add {U}. Adarkar Wastes deals 1 damage to you."
        ));
    }
}
