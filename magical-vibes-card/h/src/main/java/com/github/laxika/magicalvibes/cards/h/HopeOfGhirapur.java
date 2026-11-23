package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerDamagedBySourceCombatThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "154")
@CardRegistration(set = "KLR", collectorNumber = "243")
public class HopeOfGhirapur extends Card {

    public HopeOfGhirapur() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new PlayerRestrictionEffect(Subjects.targetPlayer(),
                        PlayerRestriction.castingNoncreatureSpells(), Duration.UNTIL_YOUR_NEXT_TURN)),
                "Sacrifice Hope of Ghirapur: Until your next turn, target player who was dealt combat damage by Hope of Ghirapur this turn can't cast noncreature spells.",
                new PlayerPredicateTargetFilter(
                        new PlayerDamagedBySourceCombatThisTurnPredicate(),
                        "Target player must have been dealt combat damage by Hope of Ghirapur this turn"
                )
        ));
    }
}
