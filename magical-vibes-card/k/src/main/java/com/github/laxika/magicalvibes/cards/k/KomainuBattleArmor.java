package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesDamagedPlayerControlsUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEC", collectorNumber = "21")
@CardRegistration(set = "NEC", collectorNumber = "60")
public class KomainuBattleArmor extends Card {

    public KomainuBattleArmor() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.SELF,
                EffectDuration.WHILE_ATTACHED));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new GoadCreaturesDamagedPlayerControlsUntilNextTurnEffect());
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER,
                new GoadCreaturesDamagedPlayerControlsUntilNextTurnEffect());

        var reconfigureTarget = TargetPredicates.permanents(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate())));
        addActivatedAbility(new ActivatedAbility(false, "{4}",
                List.of(EquipEffect.reconfigure(reconfigureTarget)),
                "Reconfigure {4}", TargetFilters.creatureYouControl(), null, null,
                ActivationTimingRestriction.SORCERY_SPEED));
        addActivatedAbility(new ActivatedAbility(false, "{4}",
                List.of(UnattachEquipmentEffect.source()),
                "Reconfigure {4} (unattach)", ActivationTimingRestriction.SORCERY_SPEED));
    }
}
