package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutLandsIntoGraveyardRepeatEffect;

@CardRegistration(set = "MOR", collectorNumber = "89")
public class CountrysideCrusher extends Card {

    public CountrysideCrusher() {
        // At the beginning of your upkeep, reveal the top card of your library. If it's a land card,
        // put it into your graveyard and repeat this process.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new RevealTopCardPutLandsIntoGraveyardRepeatEffect());

        // Whenever a land card is put into your graveyard from anywhere, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
