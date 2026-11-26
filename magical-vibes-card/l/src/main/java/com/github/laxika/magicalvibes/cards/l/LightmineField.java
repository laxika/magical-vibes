package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Lightmine Field — whenever one or more creatures attack, it deals damage to each of those creatures (the attackers
 * the trigger recorded, whether or not they are still attacking) equal to the number of attacking creatures.
 */
@CardRegistration(set = "ROE", collectorNumber = "32")
public class LightmineField extends Card {

    public LightmineField() {
        addTrigger(Triggers.playerAttacks(), DealDamageEffect.toEach(Subjects.triggeringAttackers(),
                new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER)));
    }
}
