package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "18")
public class FalconerAdept extends Card {

    public FalconerAdept() {
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                1, "Bird", 1, 1, CardColor.WHITE, List.of(CardSubtype.BIRD),
                Set.of(Keyword.FLYING), true, false
        ));
    }
}
