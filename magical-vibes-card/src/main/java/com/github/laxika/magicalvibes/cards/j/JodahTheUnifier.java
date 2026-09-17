package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.JodahTheUnifierTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "203")
public class JodahTheUnifier extends Card {

    public JodahTheUnifier() {
        PermanentCount legendaryCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                legendaryCreatures,
                legendaryCreatures,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new JodahTheUnifierTriggerEffect());
    }
}
