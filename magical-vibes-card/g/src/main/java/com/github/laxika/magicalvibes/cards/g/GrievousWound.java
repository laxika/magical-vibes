package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "102")
public class GrievousWound extends Card {

    public GrievousWound() {
        setEnchantPlayer(true);
        addStaticEffect(new PlayersCantGainLifeEffect(
                PlayersCantGainLifeEffect.Scope.ENCHANTED_PLAYER));
        addTrigger(Triggers.enchantedPlayerDealtDamage(),
                new LoseLifeEffect(new HalvedRoundedUp(new TargetPlayerLifeTotal()), LoseLifeRecipient.TARGET_PLAYER));
    }
}
