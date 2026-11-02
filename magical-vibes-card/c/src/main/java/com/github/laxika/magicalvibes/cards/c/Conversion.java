package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.LandsOfSubtypeBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "19")
@CardRegistration(set = "SUM", collectorNumber = "14")
@CardRegistration(set = "3ED", collectorNumber = "14")
@CardRegistration(set = "ME4", collectorNumber = "9")
@CardRegistration(set = "2ED", collectorNumber = "16")
@CardRegistration(set = "LEA", collectorNumber = "15")
@CardRegistration(set = "LEB", collectorNumber = "16")
public class Conversion extends Card {

    public Conversion() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {W}{W}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{W}{W}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // All Mountains are Plains.
        addStaticEffect(new LandsOfSubtypeBecomeTypeEffect(CardSubtype.MOUNTAIN, CardSubtype.PLAINS));
    }
}
