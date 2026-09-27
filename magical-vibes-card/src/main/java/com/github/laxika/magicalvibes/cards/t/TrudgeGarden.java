package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "290")
public class TrudgeGarden extends Card {

    public TrudgeGarden() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new MayPayManaEffect(
                "{2}",
                new CreateTokenEffect(
                        "Fungus Beast", 4, 4, CardColor.GREEN,
                        List.of(CardSubtype.FUNGUS, CardSubtype.BEAST), Set.of(Keyword.TRAMPLE), Set.of()),
                "Pay {2} to create a 4/4 green Fungus Beast creature token with trample?"));
    }
}
