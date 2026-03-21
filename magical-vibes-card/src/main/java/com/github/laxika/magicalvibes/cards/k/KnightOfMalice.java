package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerControlsPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.HexproofFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "97")
public class KnightOfMalice extends Card {

    public KnightOfMalice() {
        // Scryfall reports "Hexproof" keyword, but this card only has "hexproof from white"
        removeKeyword(Keyword.HEXPROOF);

        // Hexproof from white
        addEffect(EffectSlot.STATIC, new HexproofFromColorsEffect(Set.of(CardColor.WHITE)));

        // This creature gets +1/+0 as long as any player controls a white permanent.
        addEffect(EffectSlot.STATIC, new AnyPlayerControlsPermanentConditionalEffect(
                new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                new StaticBoostEffect(1, 0, GrantScope.SELF)
        ));
    }
}
