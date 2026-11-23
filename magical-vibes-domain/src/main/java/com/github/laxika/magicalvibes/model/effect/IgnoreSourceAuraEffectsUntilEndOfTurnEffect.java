package com.github.laxika.magicalvibes.model.effect;

/**
 * "[Pay a cost] for that player to ignore this effect until end of turn": the player who paid ignores the source
 * permanent's effect until end of turn. Resolving it makes a floating effect on the source until end of turn whose
 * controller is the player who paid ({@code GameData.ignoresEffectOf}); each reader asks for it by the source's id.
 * Volrath's Curse and Lost in Thought (an Aura: {@code GameQueryService.hasAuraWithEffect} then skips that Aura for that
 * player, the single lookup behind the enchanted-creature attack, block and activated-ability restrictions) and Damping
 * Engine (its land and spell restriction, read by {@code CastingPermissionService} and the activation condition).
 */
public record IgnoreSourceAuraEffectsUntilEndOfTurnEffect() implements CardEffect {
    @Override
    public boolean isSpecialAction() {
        return true;
    }
}
