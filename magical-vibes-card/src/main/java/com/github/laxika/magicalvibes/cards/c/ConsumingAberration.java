package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandsMillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "152")
@CardRegistration(set = "FDN", collectorNumber = "238")
public class ConsumingAberration extends Card {

    public ConsumingAberration() {
        CardsInGraveyard opponentGraveyards = new CardsInGraveyard(null, CountScope.OPPONENTS);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(opponentGraveyards, opponentGraveyards));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new RevealUntilLandsMillTargetPlayerEffect(1, MillRecipient.EACH_OPPONENT))));
    }
}
