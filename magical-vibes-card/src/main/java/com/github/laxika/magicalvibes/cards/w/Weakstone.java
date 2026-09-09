package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "ATQ", collectorNumber = "78")
public class Weakstone extends Card {

    public Weakstone() {
        // Attacking creatures get -1/-0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, 0, GrantScope.ALL_CREATURES,
                new PermanentIsAttackingPredicate()));
    }
}
