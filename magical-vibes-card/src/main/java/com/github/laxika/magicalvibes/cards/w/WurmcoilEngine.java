package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "223")
public class WurmcoilEngine extends Card {

    public WurmcoilEngine() {
        // When this creature dies, create a 3/3 colorless Phyrexian Wurm artifact creature token
        // with deathtouch and a 3/3 colorless Phyrexian Wurm artifact creature token with lifelink.
        addEffect(EffectSlot.ON_DEATH, new CreateCreatureTokenEffect(
                1, "Phyrexian Wurm", 3, 3, null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.WURM),
                Set.of(Keyword.DEATHTOUCH),
                Set.of(CardType.ARTIFACT)));

        addEffect(EffectSlot.ON_DEATH, new CreateCreatureTokenEffect(
                1, "Phyrexian Wurm", 3, 3, null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.WURM),
                Set.of(Keyword.LIFELINK),
                Set.of(CardType.ARTIFACT)));
    }
}
