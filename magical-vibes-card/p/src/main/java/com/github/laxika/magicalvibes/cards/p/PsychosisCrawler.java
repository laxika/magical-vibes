package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "MBS", collectorNumber = "126")
@CardRegistration(set = "BRR", collectorNumber = "44")
@CardRegistration(set = "C15", collectorNumber = "263")
@CardRegistration(set = "MOC", collectorNumber = "371")
@CardRegistration(set = "MKC", collectorNumber = "234")
@CardRegistration(set = "C20", collectorNumber = "248")
@CardRegistration(set = "BLC", collectorNumber = "282")
@CardRegistration(set = "C18", collectorNumber = "217")
@CardRegistration(set = "C16", collectorNumber = "267")
@CardRegistration(set = "FDC", collectorNumber = "280")
public class PsychosisCrawler extends Card {

    public PsychosisCrawler() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(cardsInHand, cardsInHand));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
