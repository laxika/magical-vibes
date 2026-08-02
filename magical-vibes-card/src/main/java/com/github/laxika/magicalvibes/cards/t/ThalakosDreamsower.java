package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.TapChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TMP", collectorNumber = "92")
public class ThalakosDreamsower extends Card {

    public ThalakosDreamsower() {
        // Static: "You may choose not to untap this creature during your untap step."
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // Whenever this creature deals damage to an opponent, tap target creature. That creature
        // doesn't untap during its controller's untap step for as long as this creature remains tapped.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                new TapChosenPermanentEffect(new PermanentIsCreaturePredicate(), true));
    }
}
