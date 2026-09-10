package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "ZNR", collectorNumber = "115")
public class NighthawkScavenger extends Card {

    public NighthawkScavenger() {
        CardTypesAmongCardsInGraveyard opponentCardTypes =
                new CardTypesAmongCardsInGraveyard(CountScope.OPPONENTS);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new Sum(new Fixed(1), opponentCardTypes), new Fixed(3)));
    }
}
