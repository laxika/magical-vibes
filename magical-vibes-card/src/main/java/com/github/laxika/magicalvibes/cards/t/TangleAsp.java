package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "5DN", collectorNumber = "94")
public class TangleAsp extends Card {

    public TangleAsp() {
        DestroyCombatOpponentAtEndOfCombatEffect destroyAtEndOfCombat =
                new DestroyCombatOpponentAtEndOfCombatEffect(new PermanentIsCreaturePredicate(), false);
        addEffect(EffectSlot.ON_BLOCK, destroyAtEndOfCombat);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, destroyAtEndOfCombat, TriggerMode.PER_BLOCKER);
    }
}
