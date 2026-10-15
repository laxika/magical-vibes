package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "226")
public class ShaleskinBruiser extends Card {

    public ShaleskinBruiser() {
        PermanentAllOfPredicate otherAttackingBeast = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.BEAST)));
        PermanentCount otherAttackingBeasts = new PermanentCount(otherAttackingBeast, CountScope.ANY_PLAYER, true);
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(new Scaled(otherAttackingBeasts, 3), new Fixed(0)));
    }
}
