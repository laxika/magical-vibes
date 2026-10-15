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

@CardRegistration(set = "JUD", collectorNumber = "110")
public class CrushOfWurms extends Card {

    public CrushOfWurms() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(3, "Wurm", 6, 6,
                CardColor.GREEN, List.of(CardSubtype.WURM), Set.of(), Set.of()));
        addCastingOption(new FlashbackCast("{9}{G}{G}{G}"));
    }
}
