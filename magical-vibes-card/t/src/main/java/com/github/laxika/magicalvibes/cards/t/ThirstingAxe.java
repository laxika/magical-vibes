package com.github.laxika.magicalvibes.cards.t;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.condition.EquippedCreatureDidntDealCombatDamageToCreatureThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "EMN", collectorNumber = "202")
public class ThirstingAxe extends Card {

    public ThirstingAxe() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(4, 0, GrantScope.EQUIPPED_CREATURE));
        // You can only sacrifice a permanent you control (CR 701.21a), so an opponent's equipped
        // creature is left alone.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new AllConditions(List.of(
                        new EquippedCreatureDidntDealCombatDamageToCreatureThisTurn(),
                        new EnchantedPermanentMatches(new PermanentControlledBySourceControllerPredicate(),
                                "you control equipped creature"))),
                new SacrificeEnchantedCreatureEffect()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
