package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "261")
public class ElvishChampion extends Card {

    public ElvishChampion() {
        addEffect(EffectSlot.STATIC, new BoostCreaturesBySubtypeEffect(Set.of(CardSubtype.ELF), 1, 1, Set.of(Keyword.FORESTWALK)));
    }
}
