package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HarmonizeCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "205")
public class MammothBellow extends Card {

    public MammothBellow() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Elephant", 5, 5, CardColor.GREEN, List.of(CardSubtype.ELEPHANT), Set.of(), Set.of()));
        addCastingOption(new HarmonizeCast("{5}{G}{U}{R}"));
    }
}
