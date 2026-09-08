package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "186")
@CardRegistration(set = "MKM", collectorNumber = "356")
@CardRegistration(set = "MKM", collectorNumber = "385")
public class AnzragTheQuakeMole extends Card {

    public AnzragTheQuakeMole() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new AdditionalCombatPhaseEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}{G}{G}",
                List.of(new SetCombatRequirementThisTurnEffect(
                        CombatRequirement.MUST_BE_BLOCKED, GrantScope.SELF)),
                "{3}{R}{R}{G}{G}: This creature must be blocked each combat this turn if able."
        ));
    }
}
