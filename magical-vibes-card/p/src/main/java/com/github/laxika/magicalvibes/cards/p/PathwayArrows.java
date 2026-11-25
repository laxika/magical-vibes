package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "225")
public class PathwayArrows extends Card {

    public PathwayArrows() {
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(DealDamageEffect.toTargetCreature(1),
                                new TapPermanentsEffect(Subjects.thisWay(StepKind.DEAL_DAMAGE,
                                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                                new PermanentIsColorlessPredicate()))))),
                        "{2}, {T}: This creature deals 1 damage to target creature. If a colorless creature is dealt damage this way, tap it.",
                        TargetFilters.creature()
                ),
                Subjects.attachedCreature()
        ));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
