package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "23")
public class Ornitharch extends Card {

    public Ornitharch() {
        addEffect(EffectSlot.STATIC, new TributeEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TributeNotPaidEffect(
                new CreateTokenEffect(2, "Bird", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())));
    }
}
