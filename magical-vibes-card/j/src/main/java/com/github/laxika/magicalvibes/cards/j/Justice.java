package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.ReflectSourceDamageToItsControllerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "41")
@CardRegistration(set = "ICE", collectorNumber = "32")
public class Justice extends Card {

    public Justice() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {W}{W}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{W}{W}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // Whenever a red creature or spell deals damage, this enchantment deals that much
        // damage to that creature's or spell's controller.
        addTrigger(Triggers.sourceDealsDamage(),
                new ReflectSourceDamageToItsControllerEffect(CardColor.RED));
    }
}
