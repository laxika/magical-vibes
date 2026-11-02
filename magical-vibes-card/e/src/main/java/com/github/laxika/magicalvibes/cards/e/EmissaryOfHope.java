package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DST", collectorNumber = "3")
public class EmissaryOfHope extends Card {

    public EmissaryOfHope() {
        // Whenever this creature deals combat damage to a player, you gain 1 life for each artifact
        // that player controls. The combat-damaged player is bound as the trigger's target, so the
        // TARGET_PLAYER count scope reads that player's artifacts.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new GainLifeEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.TARGET_PLAYER)));
    }
}
