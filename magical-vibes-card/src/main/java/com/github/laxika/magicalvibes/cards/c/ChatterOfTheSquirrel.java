package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "233")
public class ChatterOfTheSquirrel extends Card {

    public ChatterOfTheSquirrel() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Squirrel",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.SQUIRREL),
                Set.of(),
                Set.of()));
        addCastingOption(new FlashbackCast("{1}{G}"));
    }
}
