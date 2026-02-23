package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "349")
public class Brushland extends Card {

    public Brushland() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));
        // {T}: Add {G}. Brushland deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN), new DealDamageToControllerEffect(1)),
                "{T}: Add {G}. Brushland deals 1 damage to you."
        ));
        // {T}: Add {W}. Brushland deals 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.WHITE), new DealDamageToControllerEffect(1)),
                "{T}: Add {W}. Brushland deals 1 damage to you."
        ));
    }
}
