package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "51")
public class BalduvianFallen extends Card {

    public BalduvianFallen() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), CumulativeUpkeepEffect.withPaidEffects(
                "{1}", List.of(new BoostSelfEffect(AmountThisWay.manaPaid(ManaColor.BLACK, ManaColor.RED), new Fixed(0)))));
    }
}
