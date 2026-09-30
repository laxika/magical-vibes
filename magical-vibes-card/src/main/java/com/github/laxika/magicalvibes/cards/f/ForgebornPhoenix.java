package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YNEO", collectorNumber = "23")
public class ForgebornPhoenix extends Card {

    public ForgebornPhoenix() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.SELF,
                EffectDuration.WHILE_ATTACHED));

        var returnTrigger = new AllyCombatDamageTriggerEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEquippedPredicate())),
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(true));
        var perpetualGrant = new PerpetuallyGrantTriggeredAbilityToSourceCardEffect(
                EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
                returnTrigger);
        addEffect(EffectSlot.ON_DEATH, perpetualGrant);
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, perpetualGrant);

        var reconfigureTarget = TargetPredicates.permanents(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate())));
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(EquipEffect.reconfigure(reconfigureTarget)),
                "Reconfigure {R}", TargetFilters.creatureYouControl(), null, null,
                ActivationTimingRestriction.SORCERY_SPEED));
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(UnattachEquipmentEffect.source()),
                "Reconfigure {R} (unattach)", ActivationTimingRestriction.SORCERY_SPEED));
    }
}
