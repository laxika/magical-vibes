package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "39")
public class DawnrayArcher extends Card {

    public DawnrayArcher() {
        // Exalted: whenever a creature you control attacks alone, that creature gets +1/+1 until
        // end of turn. Triggers.creatureYouControlAttacks() records the attacker as the trigger's target;
        // AttacksAlone (checked at resolution) restricts it to lone attackers.
        addTrigger(Triggers.creatureYouControlAttacks(),
                new ConditionalEffect(new AttacksAlone(), new BoostTargetCreatureEffect(1, 1)));

        // {W}, {T}: This creature deals 1 damage to target attacking or blocking creature.
        addActivatedAbility(new ActivatedAbility(true, "{W}",
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "{W}, {T}: This creature deals 1 damage to target attacking or blocking creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentIsBlockingPredicate()
                        )),
                        "Target must be an attacking or blocking creature"
                )));
    }
}
