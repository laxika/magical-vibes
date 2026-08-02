package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "177")
public class HornetNest extends Card {

    public HornetNest() {
        // Whenever this creature is dealt damage, create that many 1/1 green Insect
        // creature tokens with flying and deathtouch.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new CreateTokenEffect(
                new EventValue(), "Insect", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.INSECT),
                Set.of(Keyword.FLYING, Keyword.DEATHTOUCH), Set.of()));
    }
}
