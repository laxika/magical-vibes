package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;

@CardRegistration(set = "VOW", collectorNumber = "230")
public class AncientLumberknot extends Card {

    public AncientLumberknot() {
        addEffect(EffectSlot.STATIC, new AssignCombatDamageWithToughnessEffect(
                GrantScope.ALL_OWN_CREATURES,
                new PermanentToughnessGreaterThanPowerPredicate()));
    }
}
