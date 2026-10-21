package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "LCI", collectorNumber = "37")
public class ThousandMoonsCrackshot extends Card {

    public ThousandMoonsCrackshot() {
        // Whenever this creature attacks, you may pay {2}{W}. When you do, tap target creature.
        // The reflexive trigger chooses its target only after the payment (CR 603.12).
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect(
                "{2}{W}",
                new QueueReflexiveAbilityEffect(
                        new TapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsCreaturePredicate())),
                "Pay {2}{W} to tap target creature?"
        ));
    }
}
