package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class BattleDisplay extends Card {

    public BattleDisplay() {
        target(TargetFilters.artifact()).addSpellEffect(new DestroyTargetPermanentEffect());
    }
}
