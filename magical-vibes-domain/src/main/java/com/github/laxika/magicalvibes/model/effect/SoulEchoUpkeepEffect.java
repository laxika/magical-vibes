package com.github.laxika.magicalvibes.model.effect;

/**
 * Soul Echo's upkeep trigger: "At the beginning of your upkeep, sacrifice this enchantment if there
 * are no echo counters on it. Otherwise, target opponent may choose that for each 1 damage that would
 * be dealt to you until your next upkeep, you remove an echo counter from this enchantment instead."
 *
 * <p>The sacrifice clause is checked on resolution, not as an intervening-if — the trigger targets an
 * opponent every upkeep regardless. When counters remain, the targeted opponent is the decision maker
 * and is prompted through the may-ability system; accepting flips
 * {@code Permanent.echoDamageRedirectionActive} on the source, which
 * {@code DamageSupport.applySoulEchoCounterRemoval} reads on both the combat and noncombat
 * damage-to-player paths. The previous duration is expired at the top of every resolution, which is
 * what makes the grant last "until your next upkeep".
 *
 * <p>Targets the opponent player — the card declares "target opponent" through a
 * {@code PlayerRelationPredicate}; {@link #targetSpec()} declares the harmful player category.
 */
public record SoulEchoUpkeepEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
