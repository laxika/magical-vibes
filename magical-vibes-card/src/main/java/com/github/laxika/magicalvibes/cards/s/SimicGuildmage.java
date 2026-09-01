package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToAnotherPermanentWithSameControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "148")
public class SimicGuildmage extends Card {

    public SimicGuildmage() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(MoveCounterFromTargetCreatureToTargetCreatureEffect.single(CounterType.PLUS_ONE_PLUS_ONE)),
                "{1}{G}: Move a +1/+1 counter from target creature onto another target creature with the same controller.",
                List.of(TargetFilters.creature(), TargetFilters.creature()),
                2,
                2
        ).withMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new AttachTargetAuraToAnotherPermanentWithSameControllerEffect()),
                "{1}{U}: Attach target Aura attached to a permanent to another permanent with the same controller."
        ));
    }
}
