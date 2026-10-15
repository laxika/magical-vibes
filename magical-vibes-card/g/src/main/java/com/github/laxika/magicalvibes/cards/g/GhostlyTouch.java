package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "58")
public class GhostlyTouch extends Card {

    public GhostlyTouch() {
        // Enchant creature
        target(TargetFilters.creature());
        // Enchanted creature has "Whenever this creature attacks, you may tap or untap
        // target permanent." The aura's ON_ATTACK slot is scanned for the attacking
        // creature by CombatTriggerService.checkAuraTriggersForCreature. Added outside the
        // enchant target slot so the granted trigger is not bound to the creature target filter.
        addEffect(EffectSlot.ON_ATTACK,
                new MayEffect(new TapOrUntapTargetPermanentEffect(),
                        "tap or untap the target permanent?"));
    }
}
