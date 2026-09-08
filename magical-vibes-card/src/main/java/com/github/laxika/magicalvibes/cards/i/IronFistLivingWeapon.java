package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "138")
public class IronFistLivingWeapon extends Card {

    public IronFistLivingWeapon() {
        ActivatedAbility damageAbility = new ActivatedAbility(
                true,
                null,
                List.of(DealDamageToAnyTargetEffect.toAnyOtherTarget(new SourcePower())),
                "{T}: Iron Fist deals damage equal to his power to any other target."
        );
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new GrantActivatedAbilityEffect(
                        damageAbility,
                        GrantScope.SELF,
                        null,
                        EffectDuration.UNTIL_END_OF_TURN
                )),
                new StackEntryTargetsPermanentPredicate(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )))
        ));
    }
}
