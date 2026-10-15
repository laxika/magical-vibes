package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.RemoveReferencedPermanentFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringTapAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "107")
public class Imprison extends Card {

    public Imprison() {
        target(TargetFilters.creature());
        CardEffect activationTrigger = new TriggeringTapAbilityConditionalEffect(
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsHostOfSourceAuraPredicate(),
                        new MayPayManaEffect(
                                "{1}",
                                new CounterSpellEffect(),
                                "Pay {1} to counter that ability?",
                                new DestroyReferencedPermanentEffect(PermanentReference.SOURCE))));
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY, activationTrigger);
        addEffect(EffectSlot.ON_OPPONENT_ACTIVATES_NONMANA_ABILITY, activationTrigger);

        CardEffect combatTrigger = new MayPayManaEffect(
                "{1}",
                SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.ENCHANTED),
                        new RemoveReferencedPermanentFromCombatEffect(PermanentReference.TRIGGERING)),
                "Pay {1} to tap and remove the creature from combat?",
                new DestroyReferencedPermanentEffect(PermanentReference.SOURCE));
        addEffect(EffectSlot.ON_ATTACK, combatTrigger);
        addEffect(EffectSlot.ON_BLOCK, combatTrigger);
    }
}
