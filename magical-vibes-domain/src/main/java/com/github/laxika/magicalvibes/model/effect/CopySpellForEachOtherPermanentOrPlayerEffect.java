package com.github.laxika.magicalvibes.model.effect;

/**
 * Copies a single-target spell for each other spell, permanent, card outside the battlefield or player it could
 * target, each copy targeting a different one of them.
 * <p>
 * As a spell or a targeted ability's effect (Radiate, Radiant Performer) it copies the target spell or ability, and its
 * controller controls the copies. As a cast trigger's effect (Glimmervoid Basin, with "a single target" as the
 * trigger's {@code StackEntryHasOneTargetPredicate} filter) the generic cast collector aims the entry at the triggering
 * spell without targeting it; it copies the spell while it is still on the stack, else as it last existed there
 * (CR 608.2h), and the spell's controller controls the copies.
 *
 * @param permanentOrPlayerOnly whether the copies may target only permanents and players
 */
public record CopySpellForEachOtherPermanentOrPlayerEffect(boolean permanentOrPlayerOnly) implements CardEffect {

    public CopySpellForEachOtherPermanentOrPlayerEffect() {
        this(false);
    }

    /** The form whose copies may target only permanents or players. */
    public static CopySpellForEachOtherPermanentOrPlayerEffect permanentsAndPlayersOnly() {
        return new CopySpellForEachOtherPermanentOrPlayerEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
