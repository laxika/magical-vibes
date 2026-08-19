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
        register(new AlternateCastXValueChoiceAiStrategy());
        register(new ScryAiStrategy());
        register(new HandTopBottomChoiceAiStrategy());
        register(new PutCardsFromHandOnLibraryCardChoiceAiStrategy());
        register(new PutCardsFromHandOnLibraryDestinationChoiceAiStrategy());
        register(new CounteredSpellLibraryDestinationChoiceAiStrategy());
        register(new LibraryReorderAiStrategy());
        register(new MayAbilityChoiceAiStrategy());
        register(new KnowledgePoolCastChoiceAiStrategy());
        register(new ImprovisationCapstoneCastChoiceAiStrategy());
        register(new ExiledSpellCopyChoiceAiStrategy());
        register(new TargetHandSpellCopyChoiceAiStrategy());
        register(new ExiledCardMayPlayChoiceAiStrategy());
        register(new ExileInstantOrSorcerySpellCostChoiceAiStrategy());
        register(new BrilliantUltimatumPileSeparationChoiceAiStrategy());
        register(new BrilliantUltimatumPileChoiceAiStrategy());
        register(new BrilliantUltimatumPlayChoiceAiStrategy());
        register(new HostileNegotiationsFaceUpChoiceAiStrategy());
        register(new HostileNegotiationsOpponentPileChoiceAiStrategy());
        register(new MirrorOfFateChoiceAiStrategy());
        register(new KeepCardsInHandChoiceAiStrategy());
        register(new EachPlayerChoosesOneCardOfEachColorChoiceAiStrategy());
        register(new PutLandsFromHandChoiceAiStrategy());
        register(new PutUpToCardsFromHandOntoBattlefieldChoiceAiStrategy());
        register(new EachPlayerMayPutCardFromHandChoiceAiStrategy());
        register(new RevealAnyNumberOfCardsFromHandChoiceAiStrategy());
        register(new DoomsdayChoiceAiStrategy());
        register(new SearchLibraryAndOrGraveyardChoiceAiStrategy());
        register(new SearchLibraryToTopChoiceAiStrategy());
        register(new IntuitionSearchChoiceAiStrategy());
        register(new PermanentAuctionChoiceAiStrategy());
        register(new IllicitAuctionBidChoiceAiStrategy());
        register(new MagesContestBidChoiceAiStrategy());
        register(new PainsRewardBidChoiceAiStrategy());
        register(new ExileNonlandCardFromTargetHandOrGraveyardChoiceAiStrategy());
        register(new MultiZoneExileChoiceAiStrategy());
        register(new ExilePermanentsOrHandCardsChoiceAiStrategy());
        register(new AttachAurasChoiceAiStrategy());
        register(new MultiPermanentChoiceAiStrategy());
        register(new MultiGraveyardChoiceAiStrategy());
        register(new ColorChoiceAiStrategy());
        register(new RevealedHandChoiceAiStrategy());
        register(new RevealCardsDiscardChoiceAiStrategy());
        register(new AlternatingHandExileChoiceAiStrategy());
        register(new MasterOfPredicamentsCardChoiceAiStrategy());
        register(new StrongholdGambitCardChoiceAiStrategy());
        register(new GraveyardChoiceAiStrategy());
        register(new GraveyardExileCostChoiceAiStrategy());
        register(new ActivatedAbilityGraveyardExileCostChoiceAiStrategy());
        register(new LibraryRevealChoiceAiStrategy());
        register(new LibrarySearchAiStrategy());
        register(new SearchOutsideGameOrExileCardChoiceAiStrategy());
        register(new PermanentChoiceAiStrategy());
        register(new SylvanLibraryChoiceAiStrategy());
        register(new AdNauseamRepeatChoiceAiStrategy());
        register(new ForbiddenRitualRepeatChoiceAiStrategy());
        register(new ExiledPermanentPutOntoBattlefieldChoiceAiStrategy());
        register(new LimDulsVaultRepeatChoiceAiStrategy());
        register(new LimDulsVaultOrderChoiceAiStrategy());
        register(new TargetLibraryDestinationChoiceAiStrategy());
        register(new VividCardChoiceAiStrategy());
        register(new CombatDamageAssignmentAiStrategy());
    }

    private AiInteractionStrategies() {
    }

    private static void register(AiInteractionStrategy<?> strategy) {
        AiInteractionStrategy<?> previous = STRATEGIES.putIfAbsent(strategy.handledType(), strategy);
        if (previous != null) {
            throw new IllegalStateException(
                    "Duplicate AI interaction strategy for " + strategy.handledType().getName());
        }
    }

    /** The strategy for the given interaction, or {@code null} if its kind has none. */
    @SuppressWarnings("unchecked")
    public static AiInteractionStrategy<PendingInteraction> forInteraction(PendingInteraction interaction) {
        return (AiInteractionStrategy<PendingInteraction>) STRATEGIES.get(interaction.getClass());
    }

    /** Exact pending-interaction classes with a registry-managed AI answer strategy. */
    public static java.util.Set<Class<? extends PendingInteraction>> registeredTypes() {
        return java.util.Set.copyOf(STRATEGIES.keySet());
    }
}
