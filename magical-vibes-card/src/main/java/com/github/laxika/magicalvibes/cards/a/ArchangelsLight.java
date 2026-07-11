package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;

@CardRegistration(set = "DKA", collectorNumber = "1")
public class ArchangelsLight extends Card {

    public ArchangelsLight() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(new CardsInGraveyard(null, CountScope.CONTROLLER), 2)));
        addEffect(EffectSlot.SPELL, new ShuffleGraveyardIntoLibraryEffect(false));
    }
}
