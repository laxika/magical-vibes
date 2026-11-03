package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandsMillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "152")
@CardRegistration(set = "C16", collectorNumber = "189")
@CardRegistration(set = "FDN", collectorNumber = "238")
@CardRegistration(set = "GK1", collectorNumber = "10")
@CardRegistration(set = "TSR", collectorNumber = "374")
@CardRegistration(set = "TDC", collectorNumber = "283")
@CardRegistration(set = "ZNC", collectorNumber = "89")
public class ConsumingAberration extends Card {

    public ConsumingAberration() {
        CardsInGraveyard opponentGraveyards = new CardsInGraveyard(null, CountScope.OPPONENTS);
        addStaticEffect(new SetPowerToughnessToAmountEffect(opponentGraveyards, opponentGraveyards));

        addTrigger(Triggers.youCastSpell().anySpell(),
                new RevealUntilLandsMillTargetPlayerEffect(1, MillRecipient.EACH_OPPONENT));
    }
}
