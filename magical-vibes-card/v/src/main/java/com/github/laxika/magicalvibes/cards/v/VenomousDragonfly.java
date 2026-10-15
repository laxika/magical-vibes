package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "282")
public class VenomousDragonfly extends Card {

    public VenomousDragonfly() {
        // Whenever this creature blocks or becomes blocked by a creature,
        // destroy that creature at end of combat.
        DestroyCombatOpponentAtEndOfCombatEffect destroyAtEndOfCombat =
                new DestroyCombatOpponentAtEndOfCombatEffect(new PermanentIsCreaturePredicate(), false);
        addEffect(EffectSlot.ON_BLOCK, destroyAtEndOfCombat);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, destroyAtEndOfCombat, TriggerMode.PER_BLOCKER);
    }
}
