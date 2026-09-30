package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerEnergyCounters;
import com.github.laxika.magicalvibes.model.effect.BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "42")
@CardRegistration(set = "M3C", collectorNumber = "94")
public class RazorfieldRipper extends Card {

    public RazorfieldRipper() {
        addEffect(EffectSlot.STATIC, new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.SELF,
                EffectDuration.WHILE_ATTACHED));
        addEffect(EffectSlot.ON_ATTACK, new EnergyCountersEffect(1));
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect(
                new ControllerEnergyCounters(), new ControllerEnergyCounters()));

        var reconfigureTarget = TargetPredicates.permanents(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate())));
        addReconfigureAbilities(reconfigureTarget, "{2}", null);
        addReconfigureAbilities(reconfigureTarget, null, new PayEnergyCost(3));
    }

    private void addReconfigureAbilities(TargetPredicate target, String manaCost, PayEnergyCost energyCost) {
        List<CardEffect> costs = energyCost == null ? List.of() : List.of(energyCost);
        String description = energyCost == null ? "Reconfigure {2}" : "Reconfigure {E}{E}{E}";

        var attachEffects = new ArrayList<CardEffect>(costs);
        attachEffects.add(EquipEffect.reconfigure(target));
        addActivatedAbility(new ActivatedAbility(false, manaCost,
                attachEffects, description, TargetFilters.creatureYouControl(), null, null,
                ActivationTimingRestriction.SORCERY_SPEED));

        var unattachEffects = new ArrayList<CardEffect>(costs);
        unattachEffects.add(UnattachEquipmentEffect.source());
        addActivatedAbility(new ActivatedAbility(false, manaCost,
                unattachEffects, description + " (unattach)", ActivationTimingRestriction.SORCERY_SPEED));
    }
}
