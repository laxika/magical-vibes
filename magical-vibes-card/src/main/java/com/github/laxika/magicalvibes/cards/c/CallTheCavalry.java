package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "9")
public class CallTheCavalry extends Card {

    public CallTheCavalry() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Knight", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.KNIGHT),
                Set.of(Keyword.VIGILANCE), Set.of()
        ));
    }
}
