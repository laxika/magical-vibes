package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "45")
public class SchoolOfPiranha extends Card {

    public SchoolOfPiranha() {
        // At the beginning of your upkeep, sacrifice this creature unless you pay {1}{U}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{1}{U}"),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
