package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "156")
public class SymbioteSpiderMan extends Card {

    public SymbioteSpiderMan() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, combatDamageAbility());

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U/B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantEffectToTargetEffect(
                                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                combatDamageAbility())
                ),
                "Find New Host — {2}{U/B}, Exile this card from your graveyard: Put a +1/+1 counter "
                        + "on target creature you control. It gains this card's other abilities. Activate "
                        + "only as a sorcery.",
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    private LookAtTopCardsEffect combatDamageAbility() {
        return new LookAtTopCardsEffect(
                new EventValue(),
                new Fixed(1),
                null,
                LookDestination.GRAVEYARD,
                false);
    }
}
