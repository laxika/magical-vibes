package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLS", collectorNumber = "84")
public class MultanisHarmony extends Card {

    public MultanisHarmony() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        ManaAbilities.tapForAnyColor(),
                        Subjects.attachedCreature()
                ));
    }
}
