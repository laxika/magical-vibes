package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "98")
public class CallTheSkybreaker extends Card {

    public CallTheSkybreaker() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(1, "Elemental", 5, 5,
                CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED),
                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.FLYING), Set.of()));
        addCastingOption(new Retrace());
    }
}
