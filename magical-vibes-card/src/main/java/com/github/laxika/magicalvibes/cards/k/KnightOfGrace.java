package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerControlsColorConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.HexproofFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "23")
public class KnightOfGrace extends Card {

    public KnightOfGrace() {
        // First strike is auto-loaded from Scryfall
        // Scryfall includes "Hexproof" alongside "Hexproof from", but this card only has hexproof from black
        removeKeyword(Keyword.HEXPROOF);

        // Hexproof from black
        addEffect(EffectSlot.STATIC, new HexproofFromColorsEffect(Set.of(CardColor.BLACK)));

        // +1/+0 as long as any player controls a black permanent
        addEffect(EffectSlot.STATIC, new AnyPlayerControlsColorConditionalEffect(
                CardColor.BLACK,
                new StaticBoostEffect(1, 0, GrantScope.SELF)
        ));
    }
}
