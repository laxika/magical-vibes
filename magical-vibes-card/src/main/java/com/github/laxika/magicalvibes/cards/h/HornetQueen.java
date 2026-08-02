package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "178")
public class HornetQueen extends Card {

    public HornetQueen() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                4,
                "Insect",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.INSECT),
                Set.of(Keyword.FLYING, Keyword.DEATHTOUCH),
                Set.of()
        ));
    }
}
