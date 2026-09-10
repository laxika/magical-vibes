package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "53")
public class UnifiedFront extends Card {

    public UnifiedFront() {
        addEffect(EffectSlot.SPELL,
                new CreateTokenEffect(new XValue(), "Kor Ally", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.KOR, CardSubtype.ALLY), Set.of(), Set.of()));
    }
}
