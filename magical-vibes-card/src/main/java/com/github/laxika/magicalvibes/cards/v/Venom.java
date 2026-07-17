package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "5ED", collectorNumber = "336")
public class Venom extends Card {

    public Venom() {
        // Enchant creature
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));

        // Whenever enchanted creature blocks or becomes blocked by a non-Wall creature,
        // destroy the other creature at end of combat.
        PermanentNotPredicate nonWall = new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL));
        addEffect(EffectSlot.ON_BLOCK, new DestroyCombatOpponentAtEndOfCombatEffect(nonWall, false));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DestroyCombatOpponentAtEndOfCombatEffect(nonWall, false),
                TriggerMode.PER_BLOCKER);
    }
}
