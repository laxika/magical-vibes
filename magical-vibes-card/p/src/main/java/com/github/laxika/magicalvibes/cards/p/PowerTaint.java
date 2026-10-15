package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "90")
public class PowerTaint extends Card {

    public PowerTaint() {
        // At the beginning of the upkeep of enchanted enchantment's controller, that player loses
        // 2 life unless they pay {2}.
        target(TargetFilters.enchantment()).addEffect(
                EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                ForcedCostOrElseEffect.enchantedControllerMayPay(
                        new PayManaCost("{2}"),
                        List.of(new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER))));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
