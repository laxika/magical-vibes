package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "76")
public class SyggWanderwineWisdom extends Card {

    public SyggWanderwineWisdom() {
        setBackFaceCard(new SyggWanderbrineShield());

        GrantEffectToTargetUntilEndOfTurnEffect combatDamageDraw =
                new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1));

        // Whenever this creature enters or transforms into Sygg, Wanderwine Wisdom, target creature
        // gains "Whenever this creature deals combat damage to a player or planeswalker, draw a card"
        // until end of turn.
        target(TargetFilters.creature());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, combatDamageDraw);
        addEffect(EffectSlot.ON_TRANSFORM_TO_FRONT_FACE, combatDamageDraw);

        // At the beginning of your first main phase, you may pay {W}. If you do, transform Sygg.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{W}", new TransformSelfEffect(), "Pay {W} to transform Sygg?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SyggWanderbrineShield";
    }
}
