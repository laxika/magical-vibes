package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only wrapper that fires the wrapped effect only when the spell or ability that caused
 * the trigger is controlled by this permanent's controller. Don't author it: the triggers
 * {@code Triggers.becomesTargetOfYourSpell()} and {@code Triggers.becomesTargetOfYourSpellOrAbility()}
 * say this, and their legacy view ({@code TriggeredAbility.legacyEffects()}) gives it to the
 * becomes-the-target collectors that still judge it.
 */
public record TriggeringSpellControllerConditionalEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
