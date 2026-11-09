package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;

import java.util.List;

/**
 * Creates an emblem carrying {@code staticEffects} and {@code triggeredAbilities} under {@code recipient}'s control
 * ({@code com.github.laxika.magicalvibes.model.Emblem}). The single effect behind every "you get an
 * emblem with …" ultimate; the emblem's contents are declared on the card, not baked into a
 * per-card effect class.
 *
 * <p>{@code staticEffects} is a pass-through payload — the handler stores it on the emblem verbatim
 * and never resolves it. Each element is a static/marker effect read later by whichever service
 * owns that behaviour (the layered pass for {@code GrantKeywordEffect}, {@code TriggerCollectionService} for the
 * emblem trigger markers, …). Emblems are never removed once created.</p>
 *
 * <p>{@code triggeredAbilities} are the emblem's triggered abilities written as abilities: "whenever you cast a
 * creature or planeswalker spell, target opponent gets two poison counters" is
 * {@code new TriggeredAbility(Triggers.youCastSpell().withFilter(creatureOrPlaneswalker),
 * new GivePoisonCountersEffect(2, PoisonRecipient.TARGET_PLAYER)).withTargetFilter(opponent)}. An emblem has no card
 * that binds its effects to a target group, so a targeted ability names its target filter itself
 * ({@link TriggeredAbility#targetFilter()}). The spell-cast collector reads the cast abilities, the emblem enters, tap,
 * creature-death and combat damage readers theirs ("whenever a creature dies", Liliana, Defiant Necromancer; "whenever a
 * creature you control deals combat damage to a player", Kaito Shizuki), and
 * {@code TriggerCollectionService.checkEmblemAttackTriggers} "whenever a creature attacks you"
 * ({@code Triggers.creatureAttacksYou()}, Garruk, Apex Predator).</p>
 *
 * <p>{@code reminderText} is the emblem's own rules text without the enclosing quotes — the handler
 * logs {@code "<player> gets an emblem with \"<reminderText>\"."}.</p>
 *
 * @param staticEffects      the emblem's static/marker effects, stored verbatim
 * @param reminderText       the emblem's rules text, unquoted, used for the game log
 * @param recipient          who gets the emblem; {@link EmblemRecipient#TARGET_PLAYER} makes the effect
 *                           declare a benign {@link TargetPredicates#player()} target
 * @param activatedAbilities the emblem's activated abilities
 * @param triggeredAbilities the emblem's triggered abilities
 */
public record CreateEmblemEffect(List<CardEffect> staticEffects,
                                 String reminderText,
                                 EmblemRecipient recipient,
                                 List<ActivatedAbility> activatedAbilities,
                                 List<TriggeredAbility> triggeredAbilities) implements CardEffect {

    public CreateEmblemEffect {
        staticEffects = List.copyOf(staticEffects);
        activatedAbilities = List.copyOf(activatedAbilities);
        triggeredAbilities = List.copyOf(triggeredAbilities);
    }

    public CreateEmblemEffect(List<CardEffect> staticEffects, String reminderText, EmblemRecipient recipient,
                              List<ActivatedAbility> activatedAbilities) {
        this(staticEffects, reminderText, recipient, activatedAbilities, List.of());
    }

    public CreateEmblemEffect(List<CardEffect> staticEffects, String reminderText, EmblemRecipient recipient) {
        this(staticEffects, reminderText, recipient, List.of());
    }

    /** "You get an emblem with …" — the controller is the recipient. */
    public CreateEmblemEffect(List<CardEffect> staticEffects, String reminderText) {
        this(staticEffects, reminderText, EmblemRecipient.CONTROLLER, List.of());
    }

    /** "You get an emblem with '[triggered ability]'" — the controller is the recipient. */
    public CreateEmblemEffect(TriggeredAbility triggeredAbility, String reminderText) {
        this(List.of(), reminderText, EmblemRecipient.CONTROLLER, List.of(), List.of(triggeredAbility));
    }

    @Override
    public TargetSpec targetSpec() {
        return recipient == EmblemRecipient.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
