package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;

import java.util.Set;

public class GoblinKing extends Card {

    public GoblinKing() {
        addEffect(EffectSlot.STATIC, new BoostCreaturesBySubtypeEffect(Set.of(CardSubtype.GOBLIN), 1, 1, Set.of(Keyword.MOUNTAINWALK)));
    }
}
