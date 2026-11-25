package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "270")
@CardRegistration(set = "AFC", collectorNumber = "221")
public class ViridianLongbow extends Card {

    public ViridianLongbow() {
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(DealDamageEffect.toAnyTarget(1)),
                        "{T}: This creature deals 1 damage to any target."
                ),
                Subjects.attachedCreature()
        ));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
