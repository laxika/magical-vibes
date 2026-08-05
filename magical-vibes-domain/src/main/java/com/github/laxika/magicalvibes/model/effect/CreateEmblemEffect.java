package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Creates an emblem carrying {@code staticEffects} under {@code recipient}'s control
 * ({@code com.github.laxika.magicalvibes.model.Emblem}). The single effect behind every "you get an
 * emblem with …" ultimate; the emblem's contents are declared on the card, not baked into a
 * per-card effect class.
 *
 * <p>{@code staticEffects} is a pass-through payload — the handler stores it on the emblem verbatim
 * and never resolves it. Each element is a static/marker effect read later by whichever service
 * owns that behaviour (the layered pass for {@code GrantKeywordEffect}, {@code CombatAttackService}
 * for {@code BoostAttackingCreatureOnAttacksYouEffect}, {@code TriggerCollectionService} for the
 * emblem trigger markers, …). Emblems are never removed once created.</p>
 *
 * <p>{@code reminderText} is the emblem's own rules text without the enclosing quotes — the handler
 * logs {@code "<player> gets an emblem with \"<reminderText>\"."}.</p>
 *
 * @param staticEffects the emblem's static/marker effects, stored verbatim
 * @param reminderText  the emblem's rules text, unquoted, used for the game log
 * @param recipient     who gets the emblem; {@link EmblemRecipient#TARGET_PLAYER} makes the effect
 *                      declare a benign {@link TargetCategory#PLAYER} target
 */
public record CreateEmblemEffect(List<CardEffect> staticEffects,
                                 String reminderText,
                                 EmblemRecipient recipient) implements CardEffect {

    public CreateEmblemEffect {
        staticEffects = List.copyOf(staticEffects);
    }

    /** "You get an emblem with …" — the controller is the recipient. */
    public CreateEmblemEffect(List<CardEffect> staticEffects, String reminderText) {
        this(staticEffects, reminderText, EmblemRecipient.CONTROLLER);
    }

    @Override
    public TargetSpec targetSpec() {
        return recipient == EmblemRecipient.TARGET_PLAYER
                ? TargetSpec.benign(TargetCategory.PLAYER)
                : TargetSpec.NONE;
    }
}
