package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;
import java.util.Set;

public class MemoryOfToshiro extends Card {

    public MemoryOfToshiro() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        new AwardRestrictedManaEffect(
                                ManaColor.BLACK,
                                1,
                                new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY)))
                ),
                "{T}, Pay 1 life: Add {B}. Spend this mana only to cast an instant or sorcery spell."
        ));
    }
}
