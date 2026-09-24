package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "165")
public class HerdBaloth extends Card {

    public HerdBaloth() {
        // Whenever one or more +1/+1 counters are put on this creature, you may create a 4/4 green
        // Beast creature token.
        addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
                new MayEffect(
                        new CreateTokenEffect("Beast", 4, 4, CardColor.GREEN,
                                List.of(CardSubtype.BEAST), Set.of(), Set.of()),
                        "Create a 4/4 green Beast creature token?"));
    }
}
