package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "43")
@CardRegistration(set = "M11", collectorNumber = "43")
@CardRegistration(set = "M12", collectorNumber = "42")
public class AlluringSiren extends Card {

    public AlluringSiren() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(SetCombatRequirementThisTurnEffect.mustAttack(Subjects.target(), Subjects.you())),
                "{T}: Target creature an opponent controls attacks you this turn if able.",
                TargetFilters.creatureAnOpponentControls()
        ));
    }
}
