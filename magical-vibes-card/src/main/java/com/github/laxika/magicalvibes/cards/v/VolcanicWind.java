package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "223")
public class VolcanicWind extends Card {

    public VolcanicWind() {
        // Volcanic Wind deals X damage divided as you choose among any number of target creatures,
        // where X is the number of creatures on the battlefield as you cast this spell.
        addEffect(EffectSlot.SPELL, new DealDividedDamageEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER),
                null,
                DivisionMode.CHOSEN,
                new PermanentIsCreaturePredicate(),
                0,
                false,
                false,
                false
        ));
    }
}
