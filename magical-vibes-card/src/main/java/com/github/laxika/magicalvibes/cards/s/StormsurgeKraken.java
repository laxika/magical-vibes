package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsCommander;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "CMM", collectorNumber = "122")
@CardRegistration(set = "CMM", collectorNumber = "497")
public class StormsurgeKraken extends Card {

    public StormsurgeKraken() {
        var lieutenant = new ControllerControlsCommander();
        addEffect(EffectSlot.STATIC, new ConditionalEffect(lieutenant,
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(lieutenant,
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_BECOMES_BLOCKED,
                        new MayEffect(new DrawCardEffect(2), "Draw two cards?"),
                        GrantScope.SELF)));
    }
}
