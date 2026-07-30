package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "114")
public class KoskunKeep extends Card {

    public KoskunKeep() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 1)),
                "{T}: Add {C}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.RED, 1)),
                "{1}, {T}: Add {R}."
        ));

        // "Add {B} or {G}" is two separate mana abilities, one per color option.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new AwardManaEffect(ManaColor.BLACK, 1)),
                "{2}, {T}: Add {B}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new AwardManaEffect(ManaColor.GREEN, 1)),
                "{2}, {T}: Add {G}."
        ));
    }
}
