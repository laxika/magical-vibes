package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "80")
public class WaterbendingLesson extends Card {

    public WaterbendingLesson() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new ForcedCostOrElseEffect(new WaterbendCost(2),
                List.of(new DiscardEffect(1, DiscardRecipient.CONTROLLER)), true));
    }
}
