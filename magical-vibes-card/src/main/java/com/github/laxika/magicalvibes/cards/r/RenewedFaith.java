package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "25")
public class RenewedFaith extends Card {

    public RenewedFaith() {
        // You gain 6 life.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(6));

        // Cycling {1}{W} ({1}{W}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, you may gain 2 life." The reflexive trigger rides on the cycling
        // ability: the optional life gain resolves first, then the cycling draw.
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(new MayEffect(new GainLifeEffect(2), "Gain 2 life?"), new DrawCardEffect(1)),
                "Cycling {1}{W} ({1}{W}, Discard this card: Draw a card.)"));
    }
}
