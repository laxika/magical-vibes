package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;

/** Perpetually grants a triggered ability to a controlled creature or a creature card in its controller's graveyard. */
public record PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect(
        Ability grantedAbility) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect(EffectSlot triggeredAbilitySlot,
                                                                                 CardEffect triggeredAbility) {
        this(LegacySlots.grant(triggeredAbilitySlot, triggeredAbility));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                TargetPredicates.anyOf(
                        TargetPredicates.creature(),
                        TargetPredicates.graveyardCards(
                                new CardTypePredicate(CardType.CREATURE),
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                new PermanentControlledBySourceControllerPredicate());
    }

    /** The slot of {@link #grantedAbility}, or {@code null} when there is none. */
    public EffectSlot triggeredAbilitySlot() {
        return grantedAbility == null ? null : LegacySlots.slotOf(grantedAbility);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredAbility() {
        return grantedAbility == null ? null : LegacySlots.effectsOf(grantedAbility).getFirst();
    }
}
