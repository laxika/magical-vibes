package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "210")
public class PenumbraSpider extends Card {

    public PenumbraSpider() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                "Spider",
                2,
                4,
                CardColor.BLACK,
                List.of(CardSubtype.SPIDER),
                Set.of(Keyword.REACH),
                Set.of()
        ));
    }
}
