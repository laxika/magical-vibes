package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "128")
public class SuicidalCharge extends Card {

    public SuicidalCharge() {
        // "Creatures your opponents control" = creatures not controlled by this card's controller.
        var opponentCreatures = new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new BoostAllCreaturesEffect(-1, -1, opponentCreatures),
                        new MatchingCreaturesMustAttackThisTurnEffect(opponentCreatures)
                ),
                "Sacrifice Suicidal Charge: Creatures your opponents control get -1/-1 until end of turn. "
                        + "Those creatures attack this turn if able."
        ));
    }
}
