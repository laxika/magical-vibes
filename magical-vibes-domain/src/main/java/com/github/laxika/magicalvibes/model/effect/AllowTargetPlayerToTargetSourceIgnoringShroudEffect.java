package com.github.laxika.magicalvibes.model.effect;

/**
 * "Until end of turn, this permanent can be the target of spells and abilities controlled by target
 * player as though it didn't have shroud." (Autumn Willow)
 *
 * <p>The permission is recorded per player on the source permanent
 * ({@code Permanent.shroudIgnoredByPlayersUntilEndOfTurn}) and consulted by the shroud checks in
 * {@code TargetLegalityService} / {@code ValidTargetService}. Only the targeted player gains it —
 * every other player still can't target the permanent — and it expires with the turn.
 */
public record AllowTargetPlayerToTargetSourceIgnoringShroudEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
