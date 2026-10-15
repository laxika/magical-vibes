package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "213")
public class TrumpetingGnarr extends Card {

    public TrumpetingGnarr() {
        addEffect(EffectSlot.ON_SELF_MUTATES, new CreateTokenEffect(
                "Beast", 3, 3, CardColor.GREEN, List.of(CardSubtype.BEAST), Set.of(), Set.of()));
    }
}
