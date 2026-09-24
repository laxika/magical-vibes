package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLZ", collectorNumber = "72")
@CardRegistration(set = "SLZ", collectorNumber = "193")
@CardRegistration(set = "SLZ", collectorNumber = "314")
@CardRegistration(set = "MH2", collectorNumber = "152")
public class Chatterstorm extends Card {

    public Chatterstorm() {
        // Create a 1/1 green Squirrel creature token.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Squirrel",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.SQUIRREL),
                Set.of(),
                Set.of()));

        // Storm (When you cast this spell, copy it for each spell cast before it this turn.)
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
