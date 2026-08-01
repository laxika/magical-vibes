package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "128")
public class HorncallersChant extends Card {

    public HorncallersChant() {
        // Create a 4/4 green Rhino creature token with trample, then populate.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect("Rhino", 4, 4, CardColor.GREEN,
                List.of(CardSubtype.RHINO), Set.of(Keyword.TRAMPLE), Set.of()));
        addEffect(EffectSlot.SPELL, new PopulateEffect());
    }
}
