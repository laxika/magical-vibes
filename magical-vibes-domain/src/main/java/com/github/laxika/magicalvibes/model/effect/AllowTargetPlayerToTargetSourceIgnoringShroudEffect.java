package com.github.laxika.magicalvibes.model.effect;

/**
 * "Until end of turn, this permanent can be the target of spells and abilities controlled by target
 * player as though it didn't have shroud." (Autumn Willow)
 *
 * <p>Resolving it makes a floating effect on the source with {@code playerId} set to the target player
 * ({@code GameData.mayTargetIgnoringShroud}), which the shroud checks in {@code TargetLegalityService} /
 * {@code ValidTargetService} consult. Only that player gains it — every other player still can't target the
 * permanent — and it ends at cleanup.
 *
 * @param playerId the player the permission is for; {@code null} on the card's ability, set on the floating effect
 */
public record AllowTargetPlayerToTargetSourceIgnoringShroudEffect(java.util.UUID playerId) implements CardEffect {

    public AllowTargetPlayerToTargetSourceIgnoringShroudEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
