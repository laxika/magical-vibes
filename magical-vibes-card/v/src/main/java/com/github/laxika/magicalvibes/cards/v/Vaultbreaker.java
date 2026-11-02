package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "117")
public class Vaultbreaker extends Card {

    public Vaultbreaker() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}"))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReturnSelfToHandIfDashCostPaidEffect());
        addTrigger(Triggers.attacks(), new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
