package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeTextBoxesEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1753")
public class DeadpoolTradingCard extends Card {

    public DeadpoolTradingCard() {
        ExchangeTextBoxesEffect exchangeTextBoxes = new ExchangeTextBoxesEffect();
        target(new PermanentPredicateTargetFilter(
                exchangeTextBoxes.targetPredicate(), "Target must be another creature"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(exchangeTextBoxes, "Exchange text boxes?"));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(3));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new SacrificeSelfCost(), new EachOtherPlayerDrawsCardEffect(1)),
                "{3}, Sacrifice this creature: Each other player draws a card."
        ));
    }
}
