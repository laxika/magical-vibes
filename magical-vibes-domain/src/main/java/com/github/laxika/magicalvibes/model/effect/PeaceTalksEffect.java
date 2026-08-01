package com.github.laxika.magicalvibes.model.effect;

/**
 * Peace Talks: "This turn and next turn, creatures can't attack, and players and permanents can't be
 * the targets of spells or activated abilities." A one-shot SPELL effect that installs a game-wide
 * two-turn lock on {@code GameData.peaceTalksTurnsRemaining}. Resolved by
 * {@code PeaceTalksEffectHandler}; attack side is read in {@code AttackLegalityService}, targeting
 * side in {@code TargetLegalityService}/{@code ValidTargetService}. Triggered abilities are
 * unaffected (oracle names only spells and activated abilities). Untargeted.
 */
public record PeaceTalksEffect() implements CardEffect {
}
