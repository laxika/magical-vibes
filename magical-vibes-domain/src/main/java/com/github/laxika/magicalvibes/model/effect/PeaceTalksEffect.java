package com.github.laxika.magicalvibes.model.effect;

/**
 * Peace Talks: "This turn and next turn, creatures can't attack, and players and permanents can't be
 * the targets of spells or activated abilities." A SPELL effect that resolves into itself as a floating effect on
 * every player lasting {@link Duration#THIS_TURN_AND_NEXT_TURN}. Resolved by
 * {@code PeaceTalksEffectHandler}; attack side is read in {@code AttackLegalityService}, targeting
 * side in {@code TargetLegalityService}/{@code ValidTargetService}. Triggered abilities are
 * unaffected (oracle names only spells and activated abilities). Untargeted.
 */
public record PeaceTalksEffect() implements CardEffect {
}
