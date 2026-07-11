package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Static lookup of {@link AiInteractionStrategy} instances by interaction record class.
 * Strategies are stateless, so shared singletons suffice; register new ones here as
 * interaction kinds migrate to the engine's {@code InteractionHandlerRegistry}.
 */
public final class AiInteractionStrategies {

    private static final Map<Class<? extends PendingInteraction>, AiInteractionStrategy<?>> STRATEGIES =
            new LinkedHashMap<>();

    static {
        register(new XValueChoiceAiStrategy());
        register(new ScryAiStrategy());
        register(new HandTopBottomChoiceAiStrategy());
        register(new LibraryReorderAiStrategy());
        register(new MayAbilityChoiceAiStrategy());
        register(new KnowledgePoolCastChoiceAiStrategy());
        register(new ImprovisationCapstoneCastChoiceAiStrategy());
        register(new MirrorOfFateChoiceAiStrategy());
        register(new MultiZoneExileChoiceAiStrategy());
        register(new MultiPermanentChoiceAiStrategy());
        register(new MultiGraveyardChoiceAiStrategy());
        register(new ColorChoiceAiStrategy());
        register(new RevealedHandChoiceAiStrategy());
        register(new RevealCardsDiscardChoiceAiStrategy());
        register(new GraveyardChoiceAiStrategy());
        register(new GraveyardExileCostChoiceAiStrategy());
        register(new LibraryRevealChoiceAiStrategy());
        register(new LibrarySearchAiStrategy());
        register(new PermanentChoiceAiStrategy());
        register(new CombatDamageAssignmentAiStrategy());
    }

    private AiInteractionStrategies() {
    }

    private static void register(AiInteractionStrategy<?> strategy) {
        STRATEGIES.put(strategy.handledType(), strategy);
    }

    /** The strategy for the given interaction, or {@code null} if its kind has none. */
    @SuppressWarnings("unchecked")
    public static AiInteractionStrategy<PendingInteraction> forInteraction(PendingInteraction interaction) {
        return (AiInteractionStrategy<PendingInteraction>) STRATEGIES.get(interaction.getClass());
    }
}
