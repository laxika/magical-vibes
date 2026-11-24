package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

public class RadiantRestraints extends Card {

    public RadiantRestraints() {
        addStaticEffect(EnterPermanentsOfTypesTappedEffect.matching(
                new PermanentIsCreaturePredicate(), Subjects.enchantedPlayer()));
    }
}
