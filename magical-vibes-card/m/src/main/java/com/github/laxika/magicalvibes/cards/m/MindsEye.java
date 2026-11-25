package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "205")
@CardRegistration(set = "MPS", collectorNumber = "18")
@CardRegistration(set = "BRR", collectorNumber = "33")
@CardRegistration(set = "MSC", collectorNumber = "288")
@CardRegistration(set = "MSC", collectorNumber = "447")
@CardRegistration(set = "FDC", collectorNumber = "272")
public class MindsEye extends Card {

    public MindsEye() {
        addTrigger(Triggers.opponentDrawsCard(),
                UnlessPaysEffect.youMayPay("{1}", new DrawCardEffect(1)).withPrompt("Pay {1} to draw a card?"));
    }
}
