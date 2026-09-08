package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "7")
public class DalkovanPackbeasts extends Card {

    public DalkovanPackbeasts() {
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                3, "Warrior", 1, 1, CardColor.RED, List.of(CardSubtype.WARRIOR), true
        ));
        addEffect(EffectSlot.ON_ATTACK, new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
