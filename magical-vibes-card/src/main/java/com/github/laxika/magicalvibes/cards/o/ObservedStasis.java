package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "40")
@CardRegistration(set = "FIC", collectorNumber = "113")
public class ObservedStasis extends Card {

    public ObservedStasis() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RemoveTargetFromCombatEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate()
                        )),
                        CountScope.ATTACHED_CONTROLLER
                )))
                .addEffect(EffectSlot.STATIC, new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());
    }
}
