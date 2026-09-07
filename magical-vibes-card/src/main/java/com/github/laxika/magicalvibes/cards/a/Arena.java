package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSB", collectorNumber = "117")
public class Arena extends Card {

    public Arena() {
        ActivatedAbility ability = new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new FightTargetsEffect()
                ),
                "{3}, {T}: Tap target creature you control and target creature of an opponent's choice "
                        + "they control. Those creatures fight each other.",
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls()),
                2,
                2
        );
        ability.withOpponentChosenTargetByController(1, TargetFilters.creatureAnOpponentControls());
        addActivatedAbility(ability);
    }
}
