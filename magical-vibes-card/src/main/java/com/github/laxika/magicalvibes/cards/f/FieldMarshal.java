package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;

import java.util.Set;

public class FieldMarshal extends Card {

    public FieldMarshal() {
        addEffect(EffectSlot.STATIC, new BoostCreaturesBySubtypeEffect(Set.of(CardSubtype.SOLDIER), 1, 1, Set.of(Keyword.FIRST_STRIKE)));
    }
}
