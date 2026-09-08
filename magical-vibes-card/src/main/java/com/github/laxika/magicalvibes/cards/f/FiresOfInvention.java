package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCanCastSpellsOnlyDuringOwnTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellLimitScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ELD", collectorNumber = "125")
public class FiresOfInvention extends Card {

    public FiresOfInvention() {
        addEffect(EffectSlot.STATIC, new ControllerCanCastSpellsOnlyDuringOwnTurnEffect());
        addEffect(EffectSlot.STATIC, new LimitSpellsPerTurnEffect(2, SpellLimitScope.CONTROLLER));
        addEffect(EffectSlot.STATIC, AlternativeCostForSpellsEffect.zeroManaValueAtMost(null,
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
    }
}
