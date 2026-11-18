package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the source Aura's static effects be ignored until end of turn (Volrath's Curse:
 * "That creature's controller may sacrifice a permanent of their choice for that player to
 * ignore this effect until end of turn"). Resolving it makes a floating effect on the Aura until end of turn whose
 * controller is the player who paid ({@code GameData.ignoresEffectOf}); {@code GameQueryService.hasAuraWithEffect}
 * then skips that Aura for that player, which is the single lookup behind the enchanted-creature attack, block, and
 * activated-ability restrictions.
 */
public record IgnoreSourceAuraEffectsUntilEndOfTurnEffect() implements CardEffect {
    @Override
    public boolean isSpecialAction() {
        return true;
    }
}
