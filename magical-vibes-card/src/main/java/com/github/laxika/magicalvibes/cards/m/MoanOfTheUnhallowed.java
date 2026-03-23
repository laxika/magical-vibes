package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "109")
public class MoanOfTheUnhallowed extends Card {

    public MoanOfTheUnhallowed() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2,
                "Zombie",
                2,
                2,
                CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE),
                Set.of(),
                Set.of()
        ));
        addCastingOption(new FlashbackCast("{5}{B}{B}"));
    }
}
