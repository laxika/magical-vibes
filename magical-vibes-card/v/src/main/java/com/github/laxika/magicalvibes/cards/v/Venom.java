package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "336")
@CardRegistration(set = "4ED", collectorNumber = "280")
@CardRegistration(set = "DRK", collectorNumber = "90")
public class Venom extends Card {

    public Venom() {
        // Enchant creature
        target(TargetFilters.creature());

        // Whenever enchanted creature blocks or becomes blocked by a non-Wall creature,
        // destroy the other creature at end of combat.
        PermanentNotPredicate nonWall = new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL));
        addTrigger(Triggers.blocks().withFilter(nonWall), new DestroyCombatOpponentAtEndOfCombatEffect(false));
        addTrigger(Triggers.becomesBlocked().withFilter(nonWall),
                TriggerOptions.EACH_BLOCKER,
                new DestroyCombatOpponentAtEndOfCombatEffect(false));
    }
}
