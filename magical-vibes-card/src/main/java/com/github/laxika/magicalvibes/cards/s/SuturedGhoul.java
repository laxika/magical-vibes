package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.TotalToughnessOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCreatureCardsFromGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "M12", collectorNumber = "112")
@CardRegistration(set = "JUD", collectorNumber = "73")
public class SuturedGhoul extends Card {

    public SuturedGhoul() {
        // As-enters replacement (CR 614.1c); the exiled cards are tracked with the permanent.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileAnyNumberOfCreatureCardsFromGraveyardOnEnterEffect());
        // Characteristic-defining power/toughness from the exiled cards (CR 613.4a).
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new TotalPowerOfCardsExiledWithSource(), new TotalToughnessOfCardsExiledWithSource()));
    }
}
