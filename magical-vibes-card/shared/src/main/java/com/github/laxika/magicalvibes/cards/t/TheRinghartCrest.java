package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

public class TheRinghartCrest extends Card {

    public TheRinghartCrest() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(ManaColor.GREEN, 1,
                        new ManaRestriction.SubtypeOrLegendaryCreatureSpells(null))),
                "{T}: Add {G}. Spend this mana only to cast a creature spell of the chosen type or a legendary creature spell."
        ));
    }
}
