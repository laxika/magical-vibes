package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.ReflectSourceDamageToItsControllerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "41")
public class Justice extends Card {

    public Justice() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {W}{W}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{W}{W}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // Whenever a red creature or spell deals damage, this enchantment deals that much
        // damage to that creature's or spell's controller.
        addEffect(EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE,
                new ReflectSourceDamageToItsControllerEffect(CardColor.RED));
    }
}
