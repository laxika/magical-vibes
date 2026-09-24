package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "C14", collectorNumber = "21")
public class DemonOfWailingAgonies extends Card {

    public DemonOfWailingAgonies() {
        ControlledCommanderAsCast lieutenant = new ControlledCommanderAsCast();

        addEffect(EffectSlot.STATIC, new ConditionalEffect(lieutenant,
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(lieutenant,
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                SacrificeRecipient.TARGET_PLAYER),
                        GrantScope.SELF)));
    }
}
