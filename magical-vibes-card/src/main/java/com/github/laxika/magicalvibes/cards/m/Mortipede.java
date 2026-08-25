package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "96")
public class Mortipede extends Card {

    public Mortipede() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new SetCombatRequirementThisTurnEffect(
                        CombatRequirement.MUST_BE_BLOCKED_BY_ALL,
                        GrantScope.SELF)),
                "{2}{G}: All creatures able to block this creature this turn do so."
        ));
    }
}
