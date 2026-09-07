package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "214")
public class CorpseCobble extends Card {

    public CorpseCobble() {
        addEffect(EffectSlot.SPELL, new SacrificeAnyNumberOfPermanentsCost(
                new PermanentIsCreaturePredicate(), true));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, new XValue(), "Zombie", new XValue(), new XValue(), CardColor.BLUE,
                Set.of(CardColor.BLUE, CardColor.BLACK), List.of(CardSubtype.ZOMBIE), Set.of(Keyword.MENACE),
                Set.of(), false, false, Map.of(), List.of(), false, false, false, 0, Set.of(), Set.of()));
        addCastingOption(new FlashbackCast("{3}{U}{B}"));
    }
}
