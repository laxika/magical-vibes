package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "AFR", collectorNumber = "164")
@CardRegistration(set = "SLD", collectorNumber = "2092")
@CardRegistration(set = "MAR", collectorNumber = "29")
@CardRegistration(set = "OMB", collectorNumber = "29")
@CardRegistration(set = "PIP", collectorNumber = "193")
@CardRegistration(set = "PIP", collectorNumber = "721")
@CardRegistration(set = "HBG", collectorNumber = "192")
@CardRegistration(set = "FDC", collectorNumber = "184")
public class UnexpectedWindfall extends Card {

    public UnexpectedWindfall() {
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTreasureToken(2));
    }
}
