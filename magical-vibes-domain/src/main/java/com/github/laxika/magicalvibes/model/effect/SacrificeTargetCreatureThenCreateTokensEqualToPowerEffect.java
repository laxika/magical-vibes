package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target creature's controller sacrifices it, then creates X tokens, where X is that creature's
 * power." (Mercy Killing.)
 *
 * <p>Targets a creature. At resolution its controller sacrifices it and — using the creature's
 * effective power captured before it leaves the battlefield (last-known information) — that same
 * player creates X copies of {@code tokenTemplate}. The template's own amount is ignored. If the
 * target is no longer a legal creature the spell does nothing.
 *
 * @param tokenTemplate the token to create (power/toughness/color/subtypes/etc.); its amount is ignored
 */
public record SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect(
        CreateTokenEffect tokenTemplate
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
