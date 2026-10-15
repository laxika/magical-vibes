package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ODY", collectorNumber = "2")
public class AncestralTribute extends Card {

    public AncestralTribute() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(new CardsInGraveyard(null, CountScope.CONTROLLER), 2)));
        addCastingOption(new FlashbackCast("{9}{W}{W}{W}"));
    }
}
