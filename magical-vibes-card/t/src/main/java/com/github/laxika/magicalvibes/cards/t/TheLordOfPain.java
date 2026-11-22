package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerOtherThanTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "3")
public class TheLordOfPain extends Card {

    public TheLordOfPain() {
        addStaticEffect(new PlayersCantGainLifeEffect(Subjects.eachOpponent()));
        target(new PlayerPredicateTargetFilter(new PlayerOtherThanTriggeringPlayerPredicate(),
                "Target must be another player"))
                .addTrigger(Triggers.playerCastsSpell().nthThisTurn(1),
                        new DealDamageToPlayersEffect(new TargetSpellManaValue(), Subjects.targetPlayer()));
    }
}
