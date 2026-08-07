package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "255")
public class SkyshroudElf extends Card {

    public SkyshroudElf() {
        // "{T}: Add {G}."
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}."
        ));
        // "{1}: Add {R} or {W}." — modelled as two separate mana abilities, one per colour.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.RED)),
                "{1}: Add {R}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.WHITE)),
                "{1}: Add {W}."
        ));
    }
}
