package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "17")
public class InvisibleWomanSueStorm extends Card {

    public InvisibleWomanSueStorm() {
        addEffect(EffectSlot.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_OTHER_HERO,
                new MayEffect(
                        new CreateTokenEffect("Wall", 0, 4, null, List.of(CardSubtype.WALL),
                                Set.of(Keyword.DEFENDER), Set.of()),
                        "Create a Wall token?"));
    }
}
