package com.github.laxika.magicalvibes.model.effect;

/** Exiles the target spell and grants this effect's controller permission to cast it from exile. */
public record ExileTargetSpellAndGrantCastPermissionEffect(boolean withoutPayingManaCost)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.spellOnStack());
    }
}
