package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

public class CleaveShadows extends Card {

    public CleaveShadows() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-1, -1,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
