package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "GTC", collectorNumber = "143")
public class AureliaTheWarleader extends Card {

    public AureliaTheWarleader() {
        // Flying, vigilance and haste are auto-loaded keywords. "Whenever Aurelia attacks for the
        // first time each turn": both halves are wrapped in OncePerTurnTriggerEffect, which the
        // ON_ATTACK collector unwraps at most once per turn per permanent — so attacking again in
        // the extra combat phase this grants doesn't re-trigger and loop.
        addEffect(EffectSlot.ON_ATTACK, new OncePerTurnTriggerEffect(
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate())));
        addEffect(EffectSlot.ON_ATTACK, new OncePerTurnTriggerEffect(new AdditionalCombatPhaseEffect(1)));
    }
}
