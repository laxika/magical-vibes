package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "228")
public class HungryForMore extends Card {

    public HungryForMore() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1,
                "Vampire",
                3,
                1,
                CardColor.BLACK,
                Set.of(CardColor.BLACK, CardColor.RED),
                List.of(CardSubtype.VAMPIRE),
                Set.of(Keyword.TRAMPLE, Keyword.LIFELINK, Keyword.HASTE),
                Set.of()));
        addEffect(EffectSlot.SPELL, new SacrificeCreatedPermanentsAtEndStepEffect());
        addCastingOption(new FlashbackCast("{1}{B}{R}"));
    }
}
