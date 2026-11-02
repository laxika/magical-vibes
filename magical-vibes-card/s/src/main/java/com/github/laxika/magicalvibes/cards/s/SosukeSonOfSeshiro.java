package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "244")
public class SosukeSonOfSeshiro extends Card {

    public SosukeSonOfSeshiro() {
        // Other Snake creatures you control get +1/+0.
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SNAKE)));

        // Whenever a Warrior you control deals combat damage to a creature, destroy that creature
        // at end of combat.
        addTrigger(Triggers.creatureYouControlDealsDamageToCreature(),
                new DestroyDamagedCreatureAtEndOfCombatEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)));
    }
}
