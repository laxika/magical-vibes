package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ICE", collectorNumber = "296")
public class KjeldoranFrostbeast extends Card {

    public KjeldoranFrostbeast() {
        // At end of combat, destroy all creatures blocking or blocked by this creature.
        PermanentIsCreaturePredicate anyCreature = new PermanentIsCreaturePredicate();
        addEffect(EffectSlot.ON_BLOCK, new DestroyCombatOpponentAtEndOfCombatEffect(anyCreature, false));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DestroyCombatOpponentAtEndOfCombatEffect(anyCreature, false),
                TriggerMode.PER_BLOCKER);
    }
}
