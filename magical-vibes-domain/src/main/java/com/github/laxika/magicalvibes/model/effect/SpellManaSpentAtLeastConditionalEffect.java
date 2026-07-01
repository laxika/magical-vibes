package com.github.laxika.magicalvibes.model.effect;

/**
 * Wraps an effect that resolves only if at least {@code minMana} was spent to cast the
 * instant or sorcery spell that triggered the containing {@link SpellCastTriggerEffect}.
 * <p>
 * The trigger collector snapshots mana spent onto the stack entry's {@code xValue}; this
 * wrapper checks that value at resolution time (intervening-if).
 */
public record SpellManaSpentAtLeastConditionalEffect(
        int minMana,
        CardEffect wrapped
) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return minMana + "+ mana spent on cast spell";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minMana + " mana was spent to cast that spell";
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return wrapped.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return wrapped.canTargetGraveyard();
    }
}
