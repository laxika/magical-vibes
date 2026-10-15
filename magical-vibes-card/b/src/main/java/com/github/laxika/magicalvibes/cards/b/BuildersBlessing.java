package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "AVR", collectorNumber = "8")
public class BuildersBlessing extends Card {

    public BuildersBlessing() {
        // Untapped creatures you control get +0/+2.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 2, GrantScope.ALL_OWN_CREATURES,
                new PermanentNotPredicate(new PermanentIsTappedPredicate())));
    }
}
