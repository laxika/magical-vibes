package com.github.laxika.magicalvibes.model.effect;

/**
 * Knowledge Pool's "that player exiles it. If the player does, they may cast a spell from among other cards exiled with
 * this artifact without paying its mana cost", on a {@code Triggers.playerCastsSpell().castFrom(Zone.HAND)} ability.
 * <p>
 * It reads the trigger event from the stack entry as it resolves: "it" is the triggering spell (exiled only while it is
 * still on the stack; otherwise the rest does nothing), "that player" the caster (the entry's non-targeting
 * {@code targetId}), and the cards exiled with the artifact are those of the entry's source permanent, which still
 * counts once the artifact has left the battlefield (CR 113.7a). The ability is controlled by the artifact's controller
 * (CR 603.3a); the caster makes the choices.
 */
public record KnowledgePoolExileAndCastEffect() implements CardEffect {
}
