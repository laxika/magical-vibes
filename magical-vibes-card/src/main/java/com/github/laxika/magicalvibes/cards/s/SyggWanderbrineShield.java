package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorsUntilYourNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.EnumSet;

/** Back face of {@link SyggWanderwineWisdom}. */
public class SyggWanderbrineShield extends Card {

    public SyggWanderbrineShield() {
        // Whenever this creature transforms into Sygg, Wanderbrine Shield, target creature you control
        // gains protection from each color until your next turn.
        target(TargetFilters.creatureYouControl());
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE,
                new GrantProtectionFromColorsUntilYourNextTurnEffect(EnumSet.allOf(CardColor.class)));

        // At the beginning of your first main phase, you may pay {U}. If you do, transform Sygg.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{U}", new TransformSelfEffect(), "Pay {U} to transform Sygg?"));
    }
}
