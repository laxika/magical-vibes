package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "149")
public class ShinenOfLifesRoar extends Card {

    public ShinenOfLifesRoar() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect());

        addHandActivatedAbility(new ActivatedAbility(false, "{2}{G}{G}",
                List.of(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BE_BLOCKED_BY_ALL)),
                "Channel — {2}{G}{G}, Discard this card: All creatures able to block target creature this turn do so.",
                TargetFilters.creature()));
    }
}
