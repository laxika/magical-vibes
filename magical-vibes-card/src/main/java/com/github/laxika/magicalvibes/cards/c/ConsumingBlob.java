package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "177")
public class ConsumingBlob extends Card {

    public ConsumingBlob() {
        DynamicAmount power = new CardTypesAmongCardsInGraveyard();
        DynamicAmount toughness = new Sum(power, new Fixed(1));
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(power, toughness));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new CreateTokenEffect(
                1, "Ooze", 0, 0,
                CardColor.GREEN, List.of(CardSubtype.OOZE), Set.of(), Set.of(),
                Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(power, toughness))
        ));
    }
}
