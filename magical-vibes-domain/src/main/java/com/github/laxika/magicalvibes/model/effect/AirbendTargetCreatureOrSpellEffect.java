package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the target creature or spell and lets its owner cast the card for {@code {2}} while it
 * remains exiled.
 */
public record AirbendTargetCreatureOrSpellEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyOf(
                TargetPredicates.creature(), TargetPredicates.spellOnStack()));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
