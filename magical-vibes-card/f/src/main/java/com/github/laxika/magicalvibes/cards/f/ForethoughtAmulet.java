package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReplaceInstantOrSorceryDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "LEG", collectorNumber = "277")
public class ForethoughtAmulet extends Card {

    public ForethoughtAmulet() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.unlessYouPay("{3}", new SacrificeSelfEffect()));
        addStaticEffect(new ReplaceInstantOrSorceryDamageToControllerEffect(3, 2));
    }
}
