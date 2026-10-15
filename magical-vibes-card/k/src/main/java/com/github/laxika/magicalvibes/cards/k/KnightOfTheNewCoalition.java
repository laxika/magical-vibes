package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "25")
public class KnightOfTheNewCoalition extends Card {

    public KnightOfTheNewCoalition() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                1, "Knight", 2, 2, null,
                Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.KNIGHT),
                Set.of(Keyword.VIGILANCE), Set.of()));
    }
}
