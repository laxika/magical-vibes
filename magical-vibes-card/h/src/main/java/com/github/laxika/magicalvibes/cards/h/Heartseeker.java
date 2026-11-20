package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnattachSourceEquipmentCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "124")
public class Heartseeker extends Card {

    public Heartseeker() {
        addStaticEffect(new StaticBoostEffect(2, 1, Subjects.attachedCreature()));
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new UnattachSourceEquipmentCost(), new DestroyTargetPermanentEffect()),
                        "{T}, Unattach Heartseeker: Destroy target creature.",
                        TargetFilters.creature()
                ),
                Subjects.attachedCreature()
        ));
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
