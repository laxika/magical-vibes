package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "8")
public class CennsHeir extends Card {

    public CennsHeir() {
        // Whenever Cenn's Heir attacks, it gets +1/+1 until end of turn
        // for each other attacking Kithkin.
        PermanentCount otherAttackingKithkin = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.KITHKIN)
                )),
                CountScope.CONTROLLER,
                true);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(otherAttackingKithkin, otherAttackingKithkin));
    }
}
