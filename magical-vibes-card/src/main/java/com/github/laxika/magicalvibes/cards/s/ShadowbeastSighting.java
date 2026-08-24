package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "198")
public class ShadowbeastSighting extends Card {

    public ShadowbeastSighting() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Beast",
                4,
                4,
                CardColor.GREEN,
                List.of(CardSubtype.BEAST),
                Set.of(),
                Set.of()));
        addCastingOption(new FlashbackCast("{6}{G}"));
    }
}
