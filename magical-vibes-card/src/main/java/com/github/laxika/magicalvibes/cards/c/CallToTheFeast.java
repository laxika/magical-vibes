package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "219")
public class CallToTheFeast extends Card {

    public CallToTheFeast() {
        // Create three 1/1 white Vampire creature tokens with lifelink.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                3, "Vampire", 1, 1, CardColor.WHITE, List.of(CardSubtype.VAMPIRE),
                Set.of(Keyword.LIFELINK), Set.of()));
    }
}
