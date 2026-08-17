package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/**
 * A queued player decision: everything the engine needs to prompt the deciding player and
 * apply their answer later. Instances wait in {@link GameData#pendingInteractions} until
 * serviced; consumers scan the queue for the first entry of the kind they handle (see the
 * type-filtered helpers on {@link GameData}), which preserves FIFO order per kind.
 *
 * <p>This is the unification point for the pending-choice subsystem: the
 * {@link PermanentChoiceContext} records are members alongside the other
 * {@code Pending*} / {@code ChoiceContext} shapes, so every queued decision has one type.
 */
public sealed interface PendingInteraction permits PermanentChoiceContext,
        PendingSphinxAmbassadorChoice, PendingCapriciousEfreetState,
        PendingKarnScionRevealChoice, PendingKarnScionExileReturn,
        PendingIntuitionRevealChoice,
        PendingThranTomeChoice,
        PendingDubiousChallengeChoice,
        PendingReturnExiledWithSourceCard, PendingPortalPileSearch,
        PendingKarnRestart, PendingKnowledgePoolCast, PendingPileSeparation, PendingBendOrBreak,
        PendingWhimsOfTheFates,
        PendingHostileNegotiations,
        PendingEachPlayerLibraryExile, PendingGuildFeud,
        PendingInteraction.XValueChoice, PendingInteraction.AlternateCastXValueChoice,
        PendingInteraction.Scry,
        PendingInteraction.HandTopBottomChoice, PendingInteraction.LibraryReorder,
        PendingInteraction.MayAbilityChoice, PendingInteraction.KnowledgePoolCastChoice,
        PendingInteraction.ImprovisationCapstoneCastChoice,
        PendingInteraction.ExiledSpellCopyChoice,
        PendingInteraction.TargetHandSpellCopyChoice,
        PendingInteraction.ExiledCardMayPlayChoice,
        PendingInteraction.ExileInstantOrSorcerySpellCostChoice,
        PendingInteraction.BrilliantUltimatumPileSeparationChoice,
        PendingInteraction.BrilliantUltimatumPileChoice,
        PendingInteraction.BrilliantUltimatumPlayChoice,
        PendingInteraction.HostileNegotiationsFaceUpChoice,
        PendingInteraction.HostileNegotiationsOpponentPileChoice,
        PendingInteraction.MirrorOfFateChoice, PendingInteraction.KeepCardsInHandChoice,
        PendingInteraction.PutLandsFromHandChoice,
        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice,
        PendingInteraction.EachPlayerMayPutCardFromHandChoice,
        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice,
        PendingInteraction.DoomsdayChoice,
        PendingInteraction.SearchLibraryAndOrGraveyardChoice,
        PendingInteraction.SearchLibraryToTopChoice,
        PendingInteraction.IntuitionSearchChoice,
        PendingInteraction.PermanentAuctionChoice,
        PendingInteraction.IllicitAuctionBidChoice,
        PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice,
        PendingInteraction.MagesContestBidChoice,
        PendingInteraction.MultiZoneExileChoice,
        PendingInteraction.ExilePermanentsOrHandCardsChoice,
        PendingInteraction.AttachAurasChoice,
        PendingInteraction.MultiPermanentChoice, PendingInteraction.MultiGraveyardChoice,
        PendingInteraction.ColorChoice, PendingInteraction.RevealedHandChoice,
        PendingInteraction.RevealCardsDiscardChoice,
        PendingInteraction.AlternatingHandExileChoice,
        PendingInteraction.GraveyardChoice, PendingInteraction.GraveyardExileCostChoice,
        PendingInteraction.ActivatedAbilityGraveyardExileCostChoice,
        PendingInteraction.HandCardChoice, PendingInteraction.StrongholdGambitCardChoice,
        PendingInteraction.TargetedHandCardChoice,
        PendingInteraction.MasterOfPredicamentsCardChoice,
        PendingInteraction.RevealedFreeCastGroup,
        PendingInteraction.PutCardsFromHandOnLibraryCardChoice,
        PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice,
        PendingInteraction.TargetLibraryDestinationChoice,
        PendingInteraction.CounteredSpellLibraryDestinationChoice,
        PendingInteraction.SylvanLibraryChoice,
        PendingInteraction.DiscardChoice, PendingInteraction.ExileFromHandChoice,
        PendingInteraction.ImprintFromHandChoice, PendingInteraction.DiscardCostChoice,
        PendingInteraction.LibraryRevealChoice,
        PendingInteraction.VividCardChoice,
        PendingInteraction.LibrarySearch,
        PendingInteraction.SearchOutsideGameOrExileCardChoice,
        PendingInteraction.PermanentChoice,
        PendingInteraction.AdNauseamRepeatChoice,
        PendingInteraction.ForbiddenRitualRepeatChoice,
        PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice,
        PendingInteraction.LimDulsVaultRepeatChoice,
        PendingInteraction.LimDulsVaultOrderChoice,
        PendingInteraction.CombatDamageAssignment,
        PendingInteraction.AttackerDeclaration,
        PendingInteraction.BlockerDeclaration {

    /**
     * The player whose decision this is. Every kind that can become the active interaction
     * (i.e. has a registered {@code InteractionHandler}) overrides this; it stays {@code null}
     * only for the queue-only carrier records ({@link PermanentChoiceContext} and the
     * standalone {@code Pending*} state records), which are serviced by beginning one of the
     * promptable kinds below. Guarded by {@code PendingInteractionContractTest}.
     */
    default UUID decidingPlayerId() {
        return null;
    }

    /**
     * The legal answer space of this interaction, as a derived view over the record's own
     * components (see {@link InteractionOptions} for the shape ↔ answer-payload mapping).
     * Same override contract as {@link #decidingPlayerId()}: non-null for every promptable
     * kind, {@code null} for queue-only carriers. (Named {@code legalOptions} rather than
     * {@code options} because {@link ColorChoice} carries an {@code options} component.)
     */
    default InteractionOptions legalOptions() {
        return null;
    }

    // ------------------------------------------------------------------
    // Generic interaction kinds. Each record carries everything needed to
    // prompt the deciding player and apply the answer (dispatched via the
    // engine's InteractionHandlerRegistry).
    // ------------------------------------------------------------------

    /**
     * "Choose a value for X" (e.g. Vigil for the Lost's ETB payment, Jaya's rummage count).
     * {@code manaPayment} marks prompts whose X is charged from the mana pool — those open
     * the CR 605.3a window so the player may tap mana sources while the prompt is up.
     */
    record XValueChoice(UUID playerId, int minValue, int maxValue, String prompt, String cardName,
                        boolean manaPayment, String manaCost)
            implements PendingInteraction {

        /** Non-mana number pick (discard counts, life payments, bids). */
        public XValueChoice(UUID playerId, int maxValue, String prompt, String cardName) {
            this(playerId, 0, maxValue, prompt, cardName, false, null);
        }

        /** Non-mana number pick with an explicit lower bound. */
        public XValueChoice(UUID playerId, int minValue, int maxValue, String prompt, String cardName) {
            this(playerId, minValue, maxValue, prompt, cardName, false, null);
        }

        /** Backward-compatible mana-payment form with the usual zero lower bound. */
        public XValueChoice(UUID playerId, int maxValue, String prompt, String cardName,
                            boolean manaPayment) {
            this(playerId, 0, maxValue, prompt, cardName, manaPayment, null);
        }

        /** Mana-payment form that carries the full cost when X is only one part of it. */
        public XValueChoice(UUID playerId, int maxValue, String prompt, String cardName,
                            boolean manaPayment, String manaCost) {
            this(playerId, 0, maxValue, prompt, cardName, manaPayment, manaCost);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.NumberPick(minValue, maxValue);
        }
    }

    /**
     * "Choose a value for X" while casting a card for an alternative mana cost that contains
     * {X} (Entreat the Angels' miracle cost {X}{W}{W}). Announcing X is part of casting
     * (CR 601.2b), and the miracle/madness cast flows run outside the normal cast path, so the
     * announcement is its own interaction. {@code cardId} identifies the hand card to cast and
     * {@code manaCost} is the alternative cost string charged once X is known. Always a mana
     * payment, so the CR 605.3a tap window is open while the prompt is up.
     */
    record AlternateCastXValueChoice(UUID playerId, UUID cardId, String manaCost, int maxValue,
                                     String prompt, String cardName, String costLabel)
            implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.NumberPick(0, maxValue);
        }
    }

    /**
     * Scry N / surveil N: {@code cards} are held out of the library while the player splits them
     * into a keep-on-top pile and a reject pile. When {@code toGraveyard} is false (scry) the
     * reject pile goes to the bottom of the library; when true (surveil) it goes to the
     * graveyard. Both variants share the SCRY_ORDER interaction shape and answer.
     */
    record Scry(UUID playerId, java.util.List<Card> cards, boolean toGraveyard, UUID libraryOwnerId)
            implements PendingInteraction {

        /** Scry variant: the reject pile goes to the bottom of the library. */
        public Scry(UUID playerId, java.util.List<Card> cards) {
            this(playerId, cards, false, playerId);
        }

        public Scry(UUID playerId, java.util.List<Card> cards, boolean toGraveyard) {
            this(playerId, cards, toGraveyard, playerId);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.UNENUMERATED;
        }
    }

    /** "Look at the top N cards: one to hand, one on top, rest on the bottom" (e.g. Anticipate-style picks). */
    record HandTopBottomChoice(UUID playerId, java.util.List<Card> cards) implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.UNENUMERATED;
        }
    }

    /**
     * Put the given cards on the top (or bottom) of {@code deckOwnerId}'s library in an order
     * of the deciding player's choosing. {@code prompt} is the exact text shown at begin time
     * (also re-sent on reconnect).
     */
    record LibraryReorder(UUID playerId, java.util.List<Card> cards, boolean toBottom,
                          UUID deckOwnerId, String prompt) implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.UNENUMERATED;
        }
    }

    /**
     * Accept/decline prompt for the head of {@link GameData#pendingMayAbilities}.
     * {@code description} and {@code manaCost} mirror that head entry; whether the player
     * can currently pay {@code manaCost} is computed at prompt time from their mana pool.
     */
    record MayAbilityChoice(UUID playerId, String description, String manaCost)
            implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.ACCEPT_DECLINE;
        }
    }

    /**
     * Knowledge Pool: the caster may cast one of the pool's other nonland exiled cards without
     * paying its cost (or decline with an empty selection). {@code validCardIds} keeps the
     * begin-time order; the card views are re-derived from the pool at prompt time (the pool
     * permanent is found via the queued {@link PendingKnowledgePoolCast}).
     */
    record KnowledgePoolCastChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount)
            implements PendingInteraction {

        public KnowledgePoolCastChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, maxCount);
        }
    }

    /**
     * Improvisation Capstone: choose any number of exiled spells to cast without paying their mana costs.
     */
    record ImprovisationCapstoneCastChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount)
            implements PendingInteraction {

        public ImprovisationCapstoneCastChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, maxCount);
        }
    }

    /**
     * Chandra, Pyromaster's ultimate: choose exactly one instant or sorcery card among the cards
     * exiled this way; it is then copied {@code copies} times and the copies are offered for a
     * free cast.
     */
    record ExiledSpellCopyChoice(UUID playerId, java.util.List<UUID> validCardIds, int copies)
            implements PendingInteraction {

        public ExiledSpellCopyChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 1, 1);
        }
    }

    /** Choose an instant or sorcery card from a revealed hand to copy. */
    record TargetHandSpellCopyChoice(UUID playerId, UUID targetPlayerId,
                                     java.util.List<Card> cards,
                                     java.util.List<UUID> validCardIds)
            implements PendingInteraction {

        public TargetHandSpellCopyChoice {
            cards = java.util.List.copyOf(cards);
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, 1);
        }
    }

    /** Choose one card among exiled cards and grant it a temporary play permission. */
    record ExiledCardMayPlayChoice(UUID playerId, java.util.List<UUID> validCardIds,
                                   boolean expiresAtEndOfTurn)
            implements PendingInteraction {

        public ExiledCardMayPlayChoice(UUID playerId, java.util.List<UUID> validCardIds) {
            this(playerId, validCardIds, false);
        }

        public ExiledCardMayPlayChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 1, 1);
        }
    }

    /** Choice of an instant or sorcery spell to exile as an activated-ability cost. */
    record ExileInstantOrSorcerySpellCostChoice(UUID playerId, UUID sourcePermanentId,
                                                int abilityIndex, int xValue,
                                                java.util.List<UUID> validCardIds)
            implements PendingInteraction {

        public ExileInstantOrSorcerySpellCostChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 1, 1);
        }
    }

    /**
     * Brilliant Ultimatum: the opponent separates the exiled cards into two piles by choosing
     * which cards belong to pile 1. Unselected cards form pile 2. Only stable card identities are
     * retained; presentation is reconstructed by the interaction projection registry.
     */
    record BrilliantUltimatumPileSeparationChoice(
            UUID playerId, java.util.List<UUID> validCardIds) implements PendingInteraction {

        public BrilliantUltimatumPileSeparationChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, validCardIds.size());
        }
    }

    /**
     * Brilliant Ultimatum: after the opponent has made two piles, the controller chooses pile 1
     * (accept) or pile 2 (decline). The immutable pile identities let initial delivery and
     * reconnect replay use the same projection.
     */
    record BrilliantUltimatumPileChoice(
            UUID playerId,
            java.util.List<UUID> pile1CardIds,
            java.util.List<UUID> pile2CardIds) implements PendingInteraction {

        public BrilliantUltimatumPileChoice {
            pile1CardIds = java.util.List.copyOf(pile1CardIds);
            pile2CardIds = java.util.List.copyOf(pile2CardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.ACCEPT_DECLINE;
        }
    }

    /**
     * Brilliant Ultimatum: after an opponent has separated the exiled cards into two piles and the
     * controller has chosen one, {@code playerId} chooses any number of the chosen pile's cards
     * ({@code validCardIds}, in pile order) to play — lands are put onto the battlefield (subject
     * to the one-land-per-turn limit) and spells are cast without paying their mana costs. Cards
     * not chosen remain exiled. Card views are re-derived from the exile zone at prompt time.
     */
    record BrilliantUltimatumPlayChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount)
            implements PendingInteraction {

        public BrilliantUltimatumPlayChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, maxCount);
        }
    }

    /** Hostile Negotiations: the controller chooses which of two piles to turn face up. */
    record HostileNegotiationsFaceUpChoice(UUID playerId, java.util.List<Card> pile1Cards,
                                           java.util.List<Card> pile2Cards)
            implements PendingInteraction {

        public HostileNegotiationsFaceUpChoice {
            pile1Cards = java.util.List.copyOf(pile1Cards);
            pile2Cards = java.util.List.copyOf(pile2Cards);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.ACCEPT_DECLINE;
        }
    }

    /** Hostile Negotiations: the opponent chooses which pile goes to the controller's hand. */
    record HostileNegotiationsOpponentPileChoice(UUID playerId, java.util.List<Card> pile1Cards,
                                                 java.util.List<Card> pile2Cards,
                                                 boolean pile1FaceUp)
            implements PendingInteraction {

        public HostileNegotiationsOpponentPileChoice {
            pile1Cards = java.util.List.copyOf(pile1Cards);
            pile2Cards = java.util.List.copyOf(pile2Cards);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.ACCEPT_DECLINE;
        }
    }

    /**
     * Mirror of Fate: choose up to seven face-up exiled cards to put on top of the library.
     * {@code validCardIds} keeps the begin-time order; views are re-derived from the player's
     * exile zone at prompt time.
     */
    record MirrorOfFateChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount)
            implements PendingInteraction {

        public MirrorOfFateChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, maxCount);
        }
    }

    /**
     * Worldpurge: {@code playerId} chooses up to {@code keepCount} cards from their hand to keep;
     * the rest are shuffled into their library. {@code validCardIds} are that player's hand card
     * ids at begin time (card views re-derived from the hand at prompt time). {@code remainingPlayerIds}
     * are the players still to choose after this one (APNAP order); when this player answers, the next
     * remaining player with a non-empty hand is prompted. {@code cardName} is the source spell's name
     * (for logging).
     */
    record KeepCardsInHandChoice(UUID playerId, java.util.List<UUID> validCardIds, int keepCount,
                                 java.util.List<UUID> remainingPlayerIds, String cardName)
            implements PendingInteraction {

        public KeepCardsInHandChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
            remainingPlayerIds = java.util.List.copyOf(remainingPlayerIds);
        }

        /** The maximum number of cards this player may keep (bounded by their hand size). */
        public int maxCount() {
            return Math.min(keepCount, validCardIds.size());
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, maxCount());
        }
    }

    /**
     * The Great Aurora: {@code playerId} may put any number of the land cards in their hand onto the
     * battlefield. {@code validCardIds} are the land card ids in that player's hand at begin time;
     * every chosen card enters untapped, all at once. {@code remainingPlayerIds} are the players
     * still to choose after this one (APNAP order); answering prompts the next remaining player who
     * has a land in hand. {@code cardName} is the source spell's name (for logging).
     */
    record PutLandsFromHandChoice(UUID playerId, java.util.List<UUID> validCardIds,
                                  java.util.List<UUID> remainingPlayerIds, String cardName)
            implements PendingInteraction {

        public PutLandsFromHandChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
            remainingPlayerIds = java.util.List.copyOf(remainingPlayerIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, validCardIds.size());
        }
    }

    /** Chooses up to a bounded number of matching cards from hand to enter together. */
    record PutUpToCardsFromHandOntoBattlefieldChoice(UUID playerId, java.util.List<UUID> validCardIds,
                                                      int maxCount, String cardName)
            implements PendingInteraction {

        public PutUpToCardsFromHandOntoBattlefieldChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, Math.min(maxCount, validCardIds.size()));
        }
    }

    /**
     * One player may put one matching card from their hand onto the battlefield. The selected cards
     * are held in {@code chosenCardIds} until every player has chosen, then enter simultaneously.
     */
    record EachPlayerMayPutCardFromHandChoice(UUID playerId, java.util.List<UUID> validCardIds,
                                              java.util.List<UUID> remainingPlayerIds,
                                              java.util.List<UUID> chosenCardIds,
                                              com.github.laxika.magicalvibes.model.filter.CardPredicate predicate,
                                              String label, String cardName) implements PendingInteraction {

        public EachPlayerMayPutCardFromHandChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
            remainingPlayerIds = java.util.List.copyOf(remainingPlayerIds);
            chosenCardIds = java.util.List.copyOf(chosenCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, 1);
        }
    }

    /**
     * The controller chooses any number of matching cards from their hand to reveal. The selected
     * count is written to the resolving stack entry for the next effect to use.
     */
    record ManaAbilityRevealContext(UUID sourcePermanentId, ManaColor manaColor, DynamicAmount amount,
                                    int xValue, int manaMultiplier, boolean creatureSource) {
    }

    record RevealAnyNumberOfCardsFromHandChoice(UUID playerId, java.util.List<UUID> validCardIds,
                                                String cardName, ManaAbilityRevealContext manaAbilityContext)
            implements PendingInteraction {

        public RevealAnyNumberOfCardsFromHandChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        public RevealAnyNumberOfCardsFromHandChoice(UUID playerId, java.util.List<UUID> validCardIds,
                                                   String cardName) {
            this(playerId, validCardIds, cardName, null);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, validCardIds.size());
        }
    }

    /**
     * Doomsday: choose up to five cards from the combined library+graveyard {@code pool} (held
     * out of both zones) to put on top of the library in any order; the unchosen cards are
     * exiled. IDs and card views are derived from {@code pool} at prompt time. The half-life
     * loss is applied by the effect handler before this choice begins.
     */
    record DoomsdayChoice(UUID playerId, java.util.List<Card> pool, int maxCount)
            implements PendingInteraction {

        public DoomsdayChoice {
            pool = java.util.List.copyOf(pool);
        }

        /** The selectable card IDs, in begin-time pool order. */
        public java.util.List<UUID> validCardIds() {
            return pool.stream().map(Card::getId).toList();
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds(), 0, maxCount);
        }
    }

    /** One matching card from the controller's library or graveyard is chosen for the hand. */
    record SearchLibraryAndOrGraveyardChoice(UUID playerId, java.util.List<Card> pool,
                                             java.util.Set<UUID> libraryCardIds,
                                             boolean librarySearchAllowed,
                                             String cardLabel) implements PendingInteraction {

        public SearchLibraryAndOrGraveyardChoice {
            pool = java.util.List.copyOf(pool);
            libraryCardIds = java.util.Set.copyOf(libraryCardIds);
        }

        public java.util.List<UUID> validCardIds() {
            return pool.stream().map(Card::getId).toList();
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds(), 0, 1);
        }
    }

    /**
     * Search-to-top: choose any number of the matching {@code pool} cards (held out of the library)
     * to put on top of the library. The unchosen matching cards are returned to the library, the
     * library is shuffled, and the chosen cards are placed on top in any order. {@code cardLabel}
     * is the display label of the searched-for cards (for prompt/log text).
     * IDs and card views are derived from {@code pool} at prompt time.
     */
    record SearchLibraryToTopChoice(UUID playerId, java.util.List<Card> pool, String cardLabel)
            implements PendingInteraction {

        public SearchLibraryToTopChoice {
            pool = java.util.List.copyOf(pool);
        }

        /** The selectable card IDs, in begin-time pool order. */
        public java.util.List<UUID> validCardIds() {
            return pool.stream().map(Card::getId).toList();
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds(), 0, pool.size());
        }
    }

    /**
     * Intuition's search: the controller picks exactly {@code count} cards from {@code pool}
     * (their whole library) to reveal. {@code opponentId} — the spell's target — then chooses one
     * of them through the library-reveal prompt; the unchosen library cards are never held out, so
     * only the picked cards leave the library before the shuffle.
     */
    record IntuitionSearchChoice(UUID playerId, UUID opponentId, java.util.List<Card> pool, int count)
            implements PendingInteraction {

        public IntuitionSearchChoice {
            pool = java.util.List.copyOf(pool);
        }

        /** The selectable card IDs, in begin-time pool order. */
        public java.util.List<UUID> validCardIds() {
            return pool.stream().map(Card::getId).toList();
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds(), count, count);
        }
    }

    /**
     * A shared auction over exiled cards (e.g. Thieves' Auction). {@code choosingPlayerId}
     * is the player currently picking one card from {@code pool} to put onto the battlefield tapped
     * under their control; {@code playerOrder} is the fixed turn-order rotation (controller first) used
     * to advance to the next chooser. {@code placed} accumulates each (controller, card) picked so far so
     * their enter-the-battlefield abilities can be processed once the pool empties. Each answered pick
     * begins a fresh record with the reduced pool, next chooser, and grown {@code placed}.
     */
    record PermanentAuctionChoice(UUID choosingPlayerId, java.util.List<Card> pool,
                                  java.util.List<UUID> playerOrder,
                                  java.util.List<PermanentAuctionPlacement> placed)
            implements PendingInteraction {

        public PermanentAuctionChoice {
            pool = java.util.List.copyOf(pool);
            playerOrder = java.util.List.copyOf(playerOrder);
            placed = java.util.List.copyOf(placed);
        }

        /** The selectable card IDs, in current pool order. */
        public java.util.List<UUID> validCardIds() {
            return pool.stream().map(Card::getId).toList();
        }

        @Override
        public UUID decidingPlayerId() {
            return choosingPlayerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds(), 1, 1);
        }
    }

    /** One card picked during a permanent auction, held for deferred enter-the-battlefield processing. */
    record PermanentAuctionPlacement(UUID controllerId, Card card) {
    }

    /**
     * One bid in Illicit Auction's life-bid auction: {@code playerId} is the player being prompted to
     * top the {@code highBid} for control of the target creature. A bid greater than {@code highBid}
     * (up to {@code maxBid}) tops it; any value {@code <= highBid} is a pass. The bid is a life loss,
     * so {@code maxBid} is a generous cap that lets a player bid more life than they have. The numeric
     * prompt reuses the X-value-choice wire message; {@code targetPermanentId} and
     * {@code highBidderId} are the immutable identities needed to reconstruct its text. The answer
     * ({@link com.github.laxika.magicalvibes.model.effect.CardEffect}-agnostic {@code NumberChosen})
     * re-runs the auction handler, which advances to the next bidder or finishes.
     */
    record IllicitAuctionBidChoice(UUID playerId, int highBid, int maxBid, String cardName,
                                   UUID targetPermanentId, UUID highBidderId)
            implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            // Any value <= highBid is a pass, so 0 (pass) through maxBid are all legal.
            return new InteractionOptions.NumberPick(0, maxBid);
        }
    }

    /** A life bid for the targeted spell in Mages' Contest. */
    record MagesContestBidChoice(UUID playerId, int highBid, int maxBid, String cardName,
                                 UUID targetSpellId, UUID highBidderId)
            implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.NumberPick(0, maxBid);
        }
    }

    /**
     * "Exile up to the offered maximum of cards named X" from {@code targetPlayerId}'s hand,
     * graveyard, and library (e.g. Memoricide-style effects). {@code validCardIds} keeps the begin-time
     * "Exile up to {@code maxCount} cards named X" from {@code targetPlayerId}'s hand, graveyard,
     * and library (e.g. Memoricide-style effects). {@code validCardIds} keeps the begin-time
     * hand → graveyard → library scan order; views are re-derived by the same scan at prompt
     * time. {@code controllerId} is the effect's controller (same as the deciding player).
     */
    record MultiZoneExileChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount,
                                UUID targetPlayerId, UUID controllerId, String cardName,
                                boolean drawForHandExiled,
                                com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate,
                                String sourceSetCode)
            implements PendingInteraction {

        public MultiZoneExileChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount,
                                    UUID targetPlayerId, UUID controllerId, String cardName) {
            this(playerId, validCardIds, maxCount, targetPlayerId, controllerId, cardName, false, null, null);
        }

        public MultiZoneExileChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount,
                                    UUID targetPlayerId, UUID controllerId, String cardName,
                                    boolean drawForHandExiled) {
            this(playerId, validCardIds, maxCount, targetPlayerId, controllerId, cardName,
                    drawForHandExiled, null, null);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, maxCount);
        }
    }

    /** Choose one nonland card from a target player's hand or graveyard to exile. */
    record ExileNonlandCardFromTargetHandOrGraveyardChoice(
            UUID playerId, UUID targetPlayerId, java.util.List<UUID> validCardIds,
            boolean grantPlayPermission)
            implements PendingInteraction {

        public ExileNonlandCardFromTargetHandOrGraveyardChoice(
                UUID playerId, UUID targetPlayerId, java.util.List<UUID> validCardIds) {
            this(playerId, targetPlayerId, validCardIds, true);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 1, 1);
        }
    }

    /**
     * "Exile N permanents you control and/or cards from your hand" (Descent into Madness) — the
     * only mixed battlefield + hand selection. {@code validCardIds} are <em>card</em> ids in
     * begin-time order (battlefield first, then hand), so a permanent is identified by
     * {@code permanent.getCard().getId()}; the answer handler re-scans both zones to map an id
     * back to the object to exile.
     *
     * <p>{@code remainingPlayerIds} is the rest of the APNAP queue and
     * {@code accumulatedCardIds} the picks made by the players before this one — the exiles are
     * applied together once the queue drains, so the queue state has to ride along with the
     * interaction.
     */
    record ExilePermanentsOrHandCardsChoice(UUID playerId, java.util.List<UUID> validCardIds,
                                            int count, java.util.List<UUID> remainingPlayerIds,
                                            java.util.List<UUID> accumulatedCardIds,
                                            String sourceName)
            implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            // Mandatory: exactly count, or everything when the player has fewer objects.
            int required = Math.min(count, validCardIds.size());
            return new InteractionOptions.MultiCardPick(validCardIds, required, required);
        }
    }

    /**
     * "Attach to this creature any number of Auras on the battlefield and put onto the battlefield
     * attached to it any number of Aura cards from your graveyard and/or hand" (Bruna, Light of
     * Alabaster). {@code validCardIds} are <em>card</em> ids in begin-time order (battlefield
     * Auras, then graveyard, then hand), so an Aura already on the battlefield is identified by
     * {@code permanent.getCard().getId()} and the answer handler re-scans all three zones to map
     * an id back to the object to move. Every offered Aura could already legally enchant
     * {@code hostPermanentId} at begin time; the selection is entirely optional.
     */
    record AttachAurasChoice(UUID playerId, java.util.List<UUID> validCardIds, UUID hostPermanentId,
                             String sourceName, int maxCount) implements PendingInteraction {

        public AttachAurasChoice(UUID playerId, java.util.List<UUID> validCardIds,
                                 UUID hostPermanentId, String sourceName) {
            this(playerId, validCardIds, hostPermanentId, sourceName, validCardIds.size());
        }

        public AttachAurasChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
            maxCount = Math.min(maxCount, validCardIds.size());
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, maxCount);
        }
    }

    /**
     * Select zero or more permanents from a list (sacrifice picks, proliferate targets,
     * combat-damage bounce, counter placement, …). {@code validIds} keeps the begin-time
     * order and {@code prompt} the exact begin-time text (also re-sent on reconnect).
     * {@code context} is the begin-time snapshot of the operation to run with the answer
     * (a {@link MultiPermanentChoiceContext}) and drives the answer dispatch; a null context
     * falls through to the legacy {@code GameData}-flag dispatch chain (kinds not yet
     * migrated onto the context).
     */
    record MultiPermanentChoice(UUID playerId, java.util.List<UUID> validIds, int maxCount,
                                MultiPermanentChoiceContext context, String prompt)
            implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiPermanentPick(validIds, 0, maxCount);
        }
    }

    /**
     * Select zero or more cards from a graveyard-sourced list (graveyard-targeting spells and
     * triggers, plus pile separation over just-exiled cards). {@code cards} keeps the begin-time
     * order — IDs and card views are derived from it at prompt time. {@code prompt} is the
     * exact begin-time text.
     */
    record MultiGraveyardChoice(UUID playerId, java.util.List<Card> cards, int maxCount,
                                String prompt, int minCount) implements PendingInteraction {

        /** Optional choice ({@code minCount} 0) — the shape used by every graveyard-targeting flow. */
        public MultiGraveyardChoice(UUID playerId, java.util.List<Card> cards, int maxCount, String prompt) {
            this(playerId, cards, maxCount, prompt, 0);
        }

        /** The selectable card IDs, in begin-time order (derived from {@link #cards}). */
        public java.util.List<UUID> validCardIds() {
            return cards.stream().map(Card::getId).toList();
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds(), minCount, maxCount);
        }
    }

    /**
     * A single-value "choose from a list" decision covering the COLOR_CHOICE protocol
     * family (mana color, protection color, keyword/subtype/permanent-type/basic-land-type,
     * card name, text-change word, Abundance land/nonland, …). The specific variant is carried
     * in {@code context} (a {@link ChoiceContext}) and drives answer handling. {@code options}
     * and {@code prompt} are the exact begin-time list options and prompt text — they are
     * carried so reconnect projection sends byte-identical content without re-deriving it from
     * {@code context}. {@code permanentId} /
     * {@code etbTargetId} carry the plain ETB color-choice permanent context.
     */
    /**
     * A single-value "choose from a list" decision. {@code disabledOptions} is a presentation-only
     * subset of {@code options} that the client greys out; every option stays legally answerable, so
     * the engine never rejects one because of it. The mana-payment flow uses it to point the player
     * at the only colour that keeps the spell they are paying for castable.
     */
    record ColorChoice(UUID playerId, UUID permanentId, UUID etbTargetId, ChoiceContext context,
                       java.util.List<String> options, String prompt,
                       java.util.List<String> disabledOptions) implements PendingInteraction {

        public ColorChoice {
            disabledOptions = disabledOptions == null ? java.util.List.of() : java.util.List.copyOf(disabledOptions);
        }

        public ColorChoice(UUID playerId, UUID permanentId, UUID etbTargetId, ChoiceContext context,
                           java.util.List<String> options, String prompt) {
            this(playerId, permanentId, etbTargetId, context, options, prompt, java.util.List.of());
        }

        /** Same decision with a new greyed-out subset; the decision's identity and options are unchanged. */
        public ColorChoice withDisabledOptions(java.util.List<String> disabled) {
            return new ColorChoice(playerId, permanentId, etbTargetId, context, options, prompt, disabled);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.ListPick(options);
        }
    }

    /**
     * {@code choosingPlayerId} picks a card from {@code targetPlayerId}'s revealed hand
     * (Duress-style hand disruption; also multi-pick discard/exile/top-of-library flows).
     * Card views are re-derived from the target's current hand at prompt time (as both the
     * legacy begin and replay did). {@code validIndices} keeps the begin-time order and
     * {@code prompt} the exact begin-time text (also re-sent on reconnect). Each answered
     * pick begins a fresh record with the decremented {@code remainingCount} and the
     * accumulated {@code chosenCards}; the batch action (discard / exile / put on library)
     * applies when the countdown ends. {@code sourcePermanentId} tracks
     * exile-until-source-leaves effects (e.g. Kitesail Freebooter); matching the legacy
     * re-begin, it is not carried across picks. {@code bottomThenDrawMode} routes the chosen
     * card to the bottom of the target's library and then makes them draw a card (Vendilion
     * Clique); {@code discardThenDrawMode} makes the target discard the chosen card and then draw a
     * card (Oildeep Gearhulk); {@code optional} lets the caster decline (answer {@code cardIndex == -1}) even
     * when a legal choice exists. {@code gainLifeToChooserEqualToChosenToughness} (discard mode
     * only) makes the choosing player gain life equal to the chosen card's toughness before it is
     * discarded (Talara's Bane). {@code followUpFilter}/{@code followUpPrompt} (optional) begin a
     * second single pick under a different {@link com.github.laxika.magicalvibes.model.filter.CardPredicate}
     * after the first pick completes (Distended Mindbender's dual MV bands) — skipped when no
     * remaining hand card matches. {@code declineFallbackDiscardCount} (discard mode, {@code > 0}
     * only together with {@code optional}) makes the target player discard that many cards of
     * their own choice when the caster declines (Nightsnare's "if you don't, that player discards
     * two cards").
     * remaining hand card matches. {@code choosableFilter}, when non-null, is re-applied to the
     * target's hand before every follow-up pick so a multi-pick flow keeps its restriction (Reap
     * Intellect's "up to X nonland cards"). {@code exileAllCopiesOfChosenNames} (exile mode only)
     * additionally exiles every card with a chosen card's name from the target's hand, graveyard,
     * and library and shuffles that library.
     */
    record RevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId,
                              java.util.List<Integer> validIndices, int remainingCount,
                              boolean discardMode, boolean exileMode,
                              java.util.List<Card> chosenCards, UUID sourcePermanentId,
                              String prompt, boolean bottomThenDrawMode, boolean optional,
                              boolean gainLifeToChooserEqualToChosenToughness,
                              com.github.laxika.magicalvibes.model.filter.CardPredicate followUpFilter,
                              String followUpPrompt, int declineFallbackDiscardCount,
                              com.github.laxika.magicalvibes.model.filter.CardPredicate choosableFilter,
                              boolean exileAllCopiesOfChosenNames,
                              boolean imprintOnSource,
                              boolean shuffleIntoLibraryMode,
                              boolean discardThenDrawMode)
            implements PendingInteraction {

        public RevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId,
                                  java.util.List<Integer> validIndices, int remainingCount,
                                  boolean discardMode, boolean exileMode,
                                  java.util.List<Card> chosenCards, UUID sourcePermanentId,
                                  String prompt, boolean bottomThenDrawMode, boolean optional) {
            this(choosingPlayerId, targetPlayerId, validIndices, remainingCount, discardMode, exileMode,
                    chosenCards, sourcePermanentId, prompt, bottomThenDrawMode, optional, false,
                    null, null, 0, null, false, false, false, false);
        }

        public RevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId,
                                  java.util.List<Integer> validIndices, int remainingCount,
                                  boolean discardMode, boolean exileMode,
                                  java.util.List<Card> chosenCards, UUID sourcePermanentId,
                                  String prompt, boolean bottomThenDrawMode, boolean optional,
                                  boolean gainLifeToChooserEqualToChosenToughness) {
            this(choosingPlayerId, targetPlayerId, validIndices, remainingCount, discardMode, exileMode,
                    chosenCards, sourcePermanentId, prompt, bottomThenDrawMode, optional,
                    gainLifeToChooserEqualToChosenToughness, null, null, 0, null, false, false, false, false);
        }

        public RevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId,
                                  java.util.List<Integer> validIndices, int remainingCount,
                                  boolean discardMode, boolean exileMode,
                                  java.util.List<Card> chosenCards, UUID sourcePermanentId,
                                  String prompt, boolean bottomThenDrawMode, boolean optional,
                                  boolean gainLifeToChooserEqualToChosenToughness,
                                  com.github.laxika.magicalvibes.model.filter.CardPredicate followUpFilter,
                                  String followUpPrompt) {
            this(choosingPlayerId, targetPlayerId, validIndices, remainingCount, discardMode, exileMode,
                    chosenCards, sourcePermanentId, prompt, bottomThenDrawMode, optional,
                    gainLifeToChooserEqualToChosenToughness, followUpFilter, followUpPrompt,
                    0, null, false, false, false, false);
        }

        public RevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId,
                                  java.util.List<Integer> validIndices, int remainingCount,
                                  boolean discardMode, boolean exileMode,
                                  java.util.List<Card> chosenCards, UUID sourcePermanentId,
                                  String prompt, boolean bottomThenDrawMode, boolean optional,
                                  boolean gainLifeToChooserEqualToChosenToughness,
                                  com.github.laxika.magicalvibes.model.filter.CardPredicate followUpFilter,
                                  String followUpPrompt, int declineFallbackDiscardCount) {
            this(choosingPlayerId, targetPlayerId, validIndices, remainingCount, discardMode, exileMode,
                    chosenCards, sourcePermanentId, prompt, bottomThenDrawMode, optional,
                    gainLifeToChooserEqualToChosenToughness, followUpFilter, followUpPrompt,
                    declineFallbackDiscardCount, null, false, false, false, false);
        }

        public RevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId,
                                  java.util.List<Integer> validIndices, int remainingCount,
                                  boolean discardMode, boolean exileMode,
                                  java.util.List<Card> chosenCards, UUID sourcePermanentId,
                                  String prompt, boolean bottomThenDrawMode, boolean optional,
                                  boolean gainLifeToChooserEqualToChosenToughness,
                                  com.github.laxika.magicalvibes.model.filter.CardPredicate followUpFilter,
                                  String followUpPrompt,
                                  com.github.laxika.magicalvibes.model.filter.CardPredicate choosableFilter,
                                  boolean exileAllCopiesOfChosenNames) {
            this(choosingPlayerId, targetPlayerId, validIndices, remainingCount, discardMode, exileMode,
                    chosenCards, sourcePermanentId, prompt, bottomThenDrawMode, optional,
                    gainLifeToChooserEqualToChosenToughness, followUpFilter, followUpPrompt,
                    0, choosableFilter, exileAllCopiesOfChosenNames, false, false, false);
        }

        public RevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId,
                                  java.util.List<Integer> validIndices, int remainingCount,
                                  boolean discardMode, boolean exileMode,
                                  java.util.List<Card> chosenCards, UUID sourcePermanentId,
                                  String prompt, boolean bottomThenDrawMode, boolean optional,
                                  boolean gainLifeToChooserEqualToChosenToughness,
                                  com.github.laxika.magicalvibes.model.filter.CardPredicate followUpFilter,
                                  String followUpPrompt,
                                  com.github.laxika.magicalvibes.model.filter.CardPredicate choosableFilter,
                                  boolean exileAllCopiesOfChosenNames,
                                  boolean imprintOnSource,
                                  boolean shuffleIntoLibraryMode) {
            this(choosingPlayerId, targetPlayerId, validIndices, remainingCount, discardMode, exileMode,
                    chosenCards, sourcePermanentId, prompt, bottomThenDrawMode, optional,
                    gainLifeToChooserEqualToChosenToughness, followUpFilter, followUpPrompt,
                    0, choosableFilter, exileAllCopiesOfChosenNames, imprintOnSource,
                    shuffleIntoLibraryMode, false);
        }

        @Override
        public UUID decidingPlayerId() {
            return choosingPlayerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.CardIndexPick(validIndices, optional);
        }
    }

    /**
     * Struggle for Sanity's alternating hand exile: the {@code targetPlayerId} and the
     * {@code controllerId} take turns exiling one card from the target's revealed hand until it is
     * empty. {@code decidingPlayerId} is whoever picks next (the target picks first);
     * {@code validIndices} are indices into the target's <em>current</em> hand, so a fresh record
     * must be begun after every pick. {@code targetExiledIds} / {@code controllerExiledIds}
     * accumulate the two exile piles: when the hand empties, the target's pile returns to their hand
     * and the controller's pile goes to the target's graveyard.
     */
    record AlternatingHandExileChoice(UUID decidingPlayerId, UUID targetPlayerId, UUID controllerId,
                                      java.util.List<Integer> validIndices,
                                      java.util.List<UUID> targetExiledIds,
                                      java.util.List<UUID> controllerExiledIds)
            implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return decidingPlayerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.CardIndexPick(validIndices, false);
        }
    }

    /**
     * The two-stage reveal-and-discard flow (Blackmail, Noggin Whack, Thieving Sprite): "Target
     * player reveals N cards from their hand and you choose one of them. That player discards that
     * card." In the reveal stage {@code decidingPlayerId} is the {@code targetPlayerId}, who picks
     * which cards to reveal: {@code validIndices} are the still-selectable indices into their hand,
     * {@code remainingCount} counts down the reveals, and {@code revealedCardIds} accumulates the
     * chosen (now public) card ids. When the countdown ends a fresh record begins the discard stage,
     * where {@code decidingPlayerId} is the {@code controllerId}: {@code revealedCardIds} is the
     * fixed revealed set shown to the controller and {@code validIndices} are the indices into that
     * set. Answers are {@link com.github.laxika.magicalvibes.model.effect.CardEffect}-agnostic
     * {@code CardIndexChosen} picks, dispatched by the deciding player. {@code destination} is
     * {@code DISCARD} for the discard cards, or {@code EXILE} when the controller's pick is exiled
     * from the target's hand instead (Vizkopa Confessor). {@code sourcePermanentId} tracks an exile
     * with the permanent that caused the interaction when the effect needs source-linked behavior.
     */
    record RevealCardsDiscardChoice(UUID decidingPlayerId, UUID targetPlayerId, UUID controllerId,
                                    boolean revealStage, java.util.List<Integer> validIndices,
                                    int remainingCount, java.util.List<UUID> revealedCardIds,
                                    int discardCount,
                                    com.github.laxika.magicalvibes.model.effect.HandChoiceDestination destination,
                                    UUID sourcePermanentId)
            implements PendingInteraction {
        // The decidingPlayerId component accessor doubles as the interface override.

        public RevealCardsDiscardChoice(UUID decidingPlayerId, UUID targetPlayerId, UUID controllerId,
                                        boolean revealStage, java.util.List<Integer> validIndices,
                                        int remainingCount, java.util.List<UUID> revealedCardIds,
                                        int discardCount,
                                        com.github.laxika.magicalvibes.model.effect.HandChoiceDestination destination) {
            this(decidingPlayerId, targetPlayerId, controllerId, revealStage, validIndices,
                    remainingCount, revealedCardIds, discardCount, destination, null);
        }

        public RevealCardsDiscardChoice {
            validIndices = java.util.List.copyOf(validIndices);
            revealedCardIds = java.util.List.copyOf(revealedCardIds);
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.CardIndexPick(validIndices, false);
        }
    }

    /**
     * Pick one card from a graveyard (return to hand/battlefield, exile, or may-ability
     * targeting). {@code validIndices} keeps the begin-time order — indices into the player's
     * own graveyard, or into {@code cardPool} when non-null (cross-graveyard choices; it also
     * drives the message's all-graveyards flag). {@code prompt} is the exact begin-time text
     * (also re-sent on reconnect). The remaining components mirror the auxiliary fields of the
     * deleted {@code GraveyardChoiceState}, all pre-seeded by the begin sites and consumed by
     * the answer handler; build instances via {@link #builder}.
     */
    record GraveyardChoice(UUID playerId, java.util.List<Integer> validIndices,
                           GraveyardChoiceDestination destination, java.util.List<Card> cardPool,
                           boolean gainLifeEqualToManaValue, UUID attachToSourcePermanentId,
                           CardColor grantColor, CardSubtype grantSubtype, int exileRemainingCount,
                           int gainLifeIfCreatureAmount, UUID gainLifeIfCreaturePlayerId,
                           UUID trackWithSourcePermanentId, Card mayAbilitySourceCard,
                           UUID mayAbilityControllerId, java.util.List<CardEffect> mayAbilityEffects,
                           UUID mayAbilitySourcePermanentId,
                           CardSubtype grantSourceHasteIfSubtype, UUID grantSourceHasteSourcePermanentId,
                           boolean mandatory, String prompt)
            implements PendingInteraction {

        public static Builder builder(UUID playerId, java.util.List<Integer> validIndices,
                                      GraveyardChoiceDestination destination, String prompt) {
            return new Builder(playerId, validIndices, destination, prompt);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            // Mirrors the answer handler's decline rule: exile and may-ability targeting are
            // forced, as is anything explicitly marked mandatory.
            boolean declinable = destination != GraveyardChoiceDestination.EXILE
                    && destination != GraveyardChoiceDestination.MAY_ABILITY_TARGET
                    && !mandatory;
            return new InteractionOptions.GraveyardIndexPick(validIndices, declinable);
        }

        /** Staged construction mirroring the legacy pre-seed setters. */
        public static final class Builder {
            private final UUID playerId;
            private final java.util.List<Integer> validIndices;
            private final GraveyardChoiceDestination destination;
            private final String prompt;
            private java.util.List<Card> cardPool;
            private boolean gainLifeEqualToManaValue;
            private UUID attachToSourcePermanentId;
            private CardColor grantColor;
            private CardSubtype grantSubtype;
            private int exileRemainingCount;
            private int gainLifeIfCreatureAmount;
            private UUID gainLifeIfCreaturePlayerId;
            private UUID trackWithSourcePermanentId;
            private Card mayAbilitySourceCard;
            private UUID mayAbilityControllerId;
            private java.util.List<CardEffect> mayAbilityEffects;
            private UUID mayAbilitySourcePermanentId;
            private CardSubtype grantSourceHasteIfSubtype;
            private UUID grantSourceHasteSourcePermanentId;
            private boolean mandatory;

            private Builder(UUID playerId, java.util.List<Integer> validIndices,
                            GraveyardChoiceDestination destination, String prompt) {
                this.playerId = playerId;
                this.validIndices = validIndices;
                this.destination = destination;
                this.prompt = prompt;
            }

            public Builder cardPool(java.util.List<Card> cardPool) {
                this.cardPool = cardPool;
                return this;
            }

            public Builder gainLifeEqualToManaValue(boolean value) {
                this.gainLifeEqualToManaValue = value;
                return this;
            }

            public Builder attachToSourcePermanentId(UUID permanentId) {
                this.attachToSourcePermanentId = permanentId;
                return this;
            }

            public Builder grantColor(CardColor grantColor) {
                this.grantColor = grantColor;
                return this;
            }

            public Builder grantSubtype(CardSubtype grantSubtype) {
                this.grantSubtype = grantSubtype;
                return this;
            }

            public Builder exileRemainingCount(int count) {
                this.exileRemainingCount = count;
                return this;
            }

            public Builder gainLifeIfCreature(int amount, UUID playerId) {
                this.gainLifeIfCreatureAmount = amount;
                this.gainLifeIfCreaturePlayerId = playerId;
                return this;
            }

            public Builder trackWithSourcePermanentId(UUID permanentId) {
                this.trackWithSourcePermanentId = permanentId;
                return this;
            }

            public Builder mayAbilityContext(Card sourceCard, UUID controllerId,
                                             java.util.List<CardEffect> effects, UUID sourcePermanentId) {
                this.mayAbilitySourceCard = sourceCard;
                this.mayAbilityControllerId = controllerId;
                this.mayAbilityEffects = effects;
                this.mayAbilitySourcePermanentId = sourcePermanentId;
                return this;
            }

            public Builder grantSourceHasteIfSubtype(CardSubtype subtype, UUID sourcePermanentId) {
                this.grantSourceHasteIfSubtype = subtype;
                this.grantSourceHasteSourcePermanentId = sourcePermanentId;
                return this;
            }

            public Builder mandatory(boolean mandatory) {
                this.mandatory = mandatory;
                return this;
            }

            public GraveyardChoice build() {
                return new GraveyardChoice(playerId, validIndices, destination, cardPool,
                        gainLifeEqualToManaValue, attachToSourcePermanentId, grantColor, grantSubtype,
                        exileRemainingCount, gainLifeIfCreatureAmount, gainLifeIfCreaturePlayerId,
                        trackWithSourcePermanentId, mayAbilitySourceCard, mayAbilityControllerId,
                        mayAbilityEffects, mayAbilitySourcePermanentId,
                        grantSourceHasteIfSubtype, grantSourceHasteSourcePermanentId, mandatory, prompt);
            }
        }
    }

    /**
     * "Exile a card from your graveyard" paid as an activation cost
     * (ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE). The pending activation itself lives in
     * {@link GameData#pendingAbilityActivation}; this record carries only the choice surface.
     * {@code validIndices} keeps the begin-time order and {@code prompt} the exact begin-time
     * text (also re-sent on reconnect).
     */
    record GraveyardExileCostChoice(UUID playerId, java.util.List<Integer> validIndices,
                                    String prompt) implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.GraveyardIndexPick(validIndices, false);
        }
    }

    /**
     * Select any number of cards from a graveyard as an activated ability cost. The selected count
     * becomes the ability's X value; the cards are held in this record until the cost is paid.
     */
    record ActivatedAbilityGraveyardExileCostChoice(UUID playerId, UUID sourcePermanentId,
                                                     int abilityIndex, UUID targetId, Zone targetZone,
                                                     java.util.List<Card> cards, String prompt)
            implements PendingInteraction {

        public ActivatedAbilityGraveyardExileCostChoice {
            cards = java.util.List.copyOf(cards);
        }

        public java.util.List<UUID> validCardIds() {
            return cards.stream().map(Card::getId).toList();
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds(), 0, cards.size());
        }
    }

    /**
     * Common surface of the six hand-card choice kinds: the deciding player, the selectable hand
     * indices in begin-time order, and the exact begin-time prompt (also re-sent on
     * reconnect). Implemented by the records below so generic consumers (AI heuristics, the
     * simulator) can read them uniformly.
     */
    interface HandChoice {
        UUID playerId();

        java.util.List<Integer> validIndices();

        String prompt();
    }

    /**
     * Put a card from hand onto the battlefield, declinable (CARD_CHOICE).
     * {@code enterTapped} makes the chosen card enter the battlefield tapped (e.g. Embrace the Paradox).
     * {@code attachEquipmentCardId}, when non-null, is the card id of the source Equipment to attach to the
     * chosen card once it enters (e.g. Deathrender).
     */
    record HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped,
                          boolean grantHaste, boolean sacrificeAtEndStep, UUID attachEquipmentCardId,
                          boolean enterAttacking, Integer sacrificeUnlessPayGenericReduction,
                          boolean drawAndRepeat, com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                          String drawAndRepeatLabel, boolean putAnyNumber,
                          boolean faceDown, int faceDownPower, int faceDownToughness,
                          java.util.Set<CardType> faceDownCardTypes, UUID returnExiledSourceCardId,
                          UUID returnSourcePermanentId, CounterType artifactCounterType,
                          int artifactCounterCount)
            implements PendingInteraction, HandChoice {

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped,
                              boolean grantHaste, boolean sacrificeAtEndStep, UUID attachEquipmentCardId,
                              boolean enterAttacking, Integer sacrificeUnlessPayGenericReduction,
                              boolean drawAndRepeat, com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                              String drawAndRepeatLabel, boolean putAnyNumber,
                              boolean faceDown, int faceDownPower, int faceDownToughness,
                              java.util.Set<CardType> faceDownCardTypes) {
            this(playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                    attachEquipmentCardId, enterAttacking, sacrificeUnlessPayGenericReduction,
                    drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel, putAnyNumber,
                    faceDown, faceDownPower, faceDownToughness, faceDownCardTypes, null, null, null, 0);
        }

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped,
                              boolean grantHaste, boolean sacrificeAtEndStep, UUID attachEquipmentCardId,
                              boolean enterAttacking, Integer sacrificeUnlessPayGenericReduction,
                              boolean drawAndRepeat, com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                              String drawAndRepeatLabel, boolean putAnyNumber) {
            this(playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, attachEquipmentCardId,
                    enterAttacking, sacrificeUnlessPayGenericReduction, drawAndRepeat, drawAndRepeatPredicate,
                    drawAndRepeatLabel, putAnyNumber, false, 0, 0, java.util.Set.of(), null, null, null, 0);
        }

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped,
                              boolean grantHaste, boolean sacrificeAtEndStep, UUID attachEquipmentCardId,
                              boolean enterAttacking, Integer sacrificeUnlessPayGenericReduction,
                              boolean drawAndRepeat, com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                              String drawAndRepeatLabel, boolean putAnyNumber, UUID returnSourcePermanentId) {
            this(playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep,
                    attachEquipmentCardId, enterAttacking, sacrificeUnlessPayGenericReduction,
                    drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel, putAnyNumber,
                    false, 0, 0, java.util.Set.of(), null, returnSourcePermanentId, null, 0);
        }

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt) {
            this(playerId, validIndices, prompt, false, false, false, null, false, null, false, null, null, false);
        }

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped) {
            this(playerId, validIndices, prompt, enterTapped, false, false, null, false, null, false, null, null, false);
        }

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped,
                              boolean grantHaste, boolean sacrificeAtEndStep) {
            this(playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, null, false, null, false, null, null, false);
        }

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped,
                              boolean grantHaste, boolean sacrificeAtEndStep, UUID attachEquipmentCardId) {
            this(playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, attachEquipmentCardId, false, null,
                    false, null, null, false);
        }

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped,
                              boolean grantHaste, boolean sacrificeAtEndStep, UUID attachEquipmentCardId,
                              boolean enterAttacking) {
            this(playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, attachEquipmentCardId,
                    enterAttacking, null, false, null, null, false);
        }

        /** Cultivator Colossus-style: put tapped, draw, and re-offer until declined. */
        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped,
                              boolean grantHaste, boolean sacrificeAtEndStep, UUID attachEquipmentCardId,
                              boolean enterAttacking, boolean drawAndRepeat,
                              com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                              String drawAndRepeatLabel) {
            this(playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, attachEquipmentCardId,
                    enterAttacking, null, drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel, false);
        }

        /** Wrenn and Seven / Cultivator-style repeat with optional draw. */
        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped,
                              boolean grantHaste, boolean sacrificeAtEndStep, UUID attachEquipmentCardId,
                              boolean enterAttacking, boolean drawAndRepeat,
                              com.github.laxika.magicalvibes.model.filter.CardPredicate drawAndRepeatPredicate,
                              String drawAndRepeatLabel, boolean putAnyNumber) {
            this(playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, attachEquipmentCardId,
                    enterAttacking, null, drawAndRepeat, drawAndRepeatPredicate, drawAndRepeatLabel, putAnyNumber);
        }

        /** "You may put a creature onto the battlefield; then sacrifice it unless you pay its cost reduced by N" (Flash). */
        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt,
                              Integer sacrificeUnlessPayGenericReduction) {
            this(playerId, validIndices, prompt, false, false, false, null, false, sacrificeUnlessPayGenericReduction,
                    false, null, null, false);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.CardIndexPick(validIndices, true);
        }
    }

    /** Mandatory hidden hand-card selection used by Stronghold Gambit's each-player choice. */
    record StrongholdGambitCardChoice(UUID playerId, java.util.List<Integer> validIndices,
                                      java.util.List<UUID> remainingPlayerIds,
                                      java.util.Map<UUID, UUID> chosenCardIds, String sourceName)
            implements PendingInteraction, HandChoice {

        public StrongholdGambitCardChoice {
            validIndices = java.util.List.copyOf(validIndices);
            remainingPlayerIds = java.util.List.copyOf(remainingPlayerIds);
            chosenCardIds = java.util.Collections.unmodifiableMap(
                    new java.util.LinkedHashMap<>(chosenCardIds));
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public String prompt() {
            return "Choose a card in your hand.";
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.CardIndexPick(validIndices, false);
        }
    }

    /**
     * Master of Predicaments: the controller separates one card from their hand before the
     * damaged player guesses its mana-value range. The selected card stays in the hand until it
     * is actually cast.
     */
    record MasterOfPredicamentsCardChoice(UUID playerId, java.util.List<Integer> validIndices,
                                          String prompt, UUID guessingPlayerId, Card sourceCard)
            implements PendingInteraction, HandChoice {

        public MasterOfPredicamentsCardChoice {
            validIndices = java.util.List.copyOf(validIndices);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.CardIndexPick(validIndices, false);
        }
    }

    /**
     * Carry-over state for "reveal the top N cards of target player's library, you may cast up to
     * {@code castsRemaining} instant or sorcery spells from among them without paying their mana
     * costs, then that player puts the rest into their graveyard" (Talent of the Telepath).
     *
     * <p>The revealed cards are held outside every zone while the casting decisions are made, so
     * they live here rather than in a library or graveyard; once the offers are exhausted the cards
     * still held are put into {@code ownerId}'s graveyard. Like
     * {@link PendingEachPlayerLibraryExile} this is a pure state carrier that never becomes the
     * active interaction, so its {@code decidingPlayerId()} stays {@code null}.
     *
     * @param ownerId        owner of the revealed library; the rest end up in their graveyard
     * @param casterId       the player who may cast the revealed instants/sorceries
     * @param heldCards      the revealed cards not yet cast, currently in no zone
     * @param castsRemaining how many more spells may still be cast from among them
     */
    record RevealedFreeCastGroup(UUID ownerId, UUID casterId, java.util.List<Card> heldCards,
                                 int castsRemaining) implements PendingInteraction {

        public RevealedFreeCastGroup {
            heldCards = java.util.List.copyOf(heldCards);
        }
    }

    /**
     * Put an Aura card from hand onto the battlefield attached to {@code targetId},
     * declinable (TARGETED_CARD_CHOICE).
     */
    record TargetedHandCardChoice(UUID playerId, java.util.List<Integer> validIndices,
                                  UUID targetId, String prompt, UUID exileSourceIfDeclinedId)
            implements PendingInteraction, HandChoice {

        /** Non-exiling variant (the common case): declining simply puts nothing onto the battlefield. */
        public TargetedHandCardChoice(UUID playerId, java.util.List<Integer> validIndices,
                                      UUID targetId, String prompt) {
            this(playerId, validIndices, targetId, prompt, null);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.CardIndexPick(validIndices, true);
        }
    }

    /**
     * Choose between {@code minCount} and {@code maxCount} cards from your own hand to put back on
     * your library. {@code validCardIds}/{@code cards} are the current hand snapshot; with
     * {@link HandToLibraryPlacement#PLAYER_CHOICE} the top/bottom destination is picked in the
     * follow-up {@link PutCardsFromHandOnLibraryDestinationChoice} (Dream Cache), while
     * {@code TOP} (Brainstorm) and {@code BOTTOM} (Amass the Components) place the chosen cards
     * immediately. The {@link #putRequiredCardOnLibrary} factory models a mandatory single-card
     * choice.
     *
     * <p>When {@code shuffleIn} is {@code true} the chosen cards are instead shuffled into the
     * library and the pick is mandatory (Lat-Nam's Legacy); {@code thenEffect} — if set — is pushed
     * onto the stack as a reflexive triggered ability from {@code thenEffectSourceCard} afterwards
     * ("Shuffle a card from your hand into your library. If you do, …"). {@code placement} is
     * unused on that path.
     *
     * <p>When {@code swapWithLibraryTop} is {@code true} the chosen cards are set aside instead,
     * that many cards are moved from the top of the library into the hand, and the set-aside cards
     * then go back on top through a {@link LibraryReorder} "in any order" prompt (Scroll Rack).
     * {@code placement} is unused on that path too.
     */
    record PutCardsFromHandOnLibraryCardChoice(UUID playerId, java.util.List<UUID> validCardIds,
                                               java.util.List<Card> cards, int minCount, int maxCount,
                                               HandToLibraryPlacement placement,
                                               boolean shuffleIn, Card thenEffectSourceCard,
                                               com.github.laxika.magicalvibes.model.effect.CardEffect thenEffect,
                                               boolean swapWithLibraryTop)
            implements PendingInteraction {

        public PutCardsFromHandOnLibraryCardChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
            cards = java.util.List.copyOf(cards);
        }

        /** "Put up to {@code maxCount} cards from your hand on your library" (Dream Cache, Brainstorm). */
        public static PutCardsFromHandOnLibraryCardChoice putOnLibrary(UUID playerId,
                java.util.List<UUID> validCardIds, java.util.List<Card> cards, int maxCount,
                HandToLibraryPlacement placement) {
            return new PutCardsFromHandOnLibraryCardChoice(playerId, validCardIds, cards, 0, maxCount, placement,
                    false, null, null, false);
        }

        /** "Put a card from your hand on top of your library" (mandatory when the hand is nonempty). */
        public static PutCardsFromHandOnLibraryCardChoice putRequiredCardOnLibrary(UUID playerId,
                java.util.List<UUID> validCardIds, java.util.List<Card> cards,
                HandToLibraryPlacement placement) {
            return new PutCardsFromHandOnLibraryCardChoice(playerId, validCardIds, cards, 1, 1, placement,
                    false, null, null, false);
        }

        /** "Shuffle {@code count} cards from your hand into your library. If you do, {@code thenEffect}." */
        public static PutCardsFromHandOnLibraryCardChoice shuffleIntoLibrary(UUID playerId,
                java.util.List<UUID> validCardIds, java.util.List<Card> cards, int count,
                Card sourceCard, com.github.laxika.magicalvibes.model.effect.CardEffect thenEffect) {
            return new PutCardsFromHandOnLibraryCardChoice(playerId, validCardIds, cards, count,
                    count,
                    HandToLibraryPlacement.TOP, true, sourceCard, thenEffect, false);
        }

        /**
         * "Exile any number of cards from your hand face down. Put that many cards from the top of
         * your library into your hand. Then look at the exiled cards and put them on top of your
         * library in any order." (Scroll Rack)
         */
        public static PutCardsFromHandOnLibraryCardChoice swapWithLibraryTop(UUID playerId,
                java.util.List<UUID> validCardIds, java.util.List<Card> cards) {
            return new PutCardsFromHandOnLibraryCardChoice(playerId, validCardIds, cards, 0, cards.size(),
                    HandToLibraryPlacement.TOP, false, null, null, true);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, minCount, maxCount);
        }
    }

    /**
     * Choose whether the previously-chosen hand cards go on top or the bottom of your library
     * (Dream Cache follow-up to {@link PutCardsFromHandOnLibraryCardChoice}). All chosen cards
     * go to the same destination.
     */
    record PutCardsFromHandOnLibraryDestinationChoice(UUID playerId, java.util.List<UUID> chosenCardIds)
            implements PendingInteraction {

        /** The exact option strings the handler's prompt offers and its answer parser matches. */
        public static final java.util.List<String> OPTIONS = java.util.List.of("Top", "Bottom");

        public PutCardsFromHandOnLibraryDestinationChoice {
            chosenCardIds = java.util.List.copyOf(chosenCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.ListPick(OPTIONS);
        }
    }

    /**
     * Choose whether a spell countered by {@code CounteredSpellDestination#LIBRARY_TOP_OR_BOTTOM}
     * goes on the top or the bottom of its owner's library (Hinder). The card is already on top of
     * {@code ownerId}'s library when this is asked; picking "Bottom" moves it to the other end.
     * {@code playerId} is the counter's controller, who makes the choice.
     */
    record CounteredSpellLibraryDestinationChoice(UUID playerId, UUID ownerId, UUID cardId, String cardName)
            implements PendingInteraction {

        /** The exact option strings the handler's prompt offers and its answer parser matches. */
        public static final java.util.List<String> OPTIONS = java.util.List.of("Top", "Bottom");

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.ListPick(OPTIONS);
        }
    }

    /**
     * Choose whether a targeted spell or creature stays on top of its owner's library or moves to
     * the bottom. The deciding player is the target card's owner.
     */
    record TargetLibraryDestinationChoice(UUID playerId, UUID cardId, String cardName,
                                          String firstOption)
            implements PendingInteraction {

        /** The exact option strings the handler's prompt offers and its answer parser matches. */
        public static final java.util.List<String> OPTIONS = java.util.List.of("Top", "Bottom");

        public TargetLibraryDestinationChoice(UUID playerId, UUID cardId, String cardName) {
            this(playerId, cardId, cardName, OPTIONS.getFirst());
        }

        public java.util.List<String> options() {
            return java.util.List.of(firstOption, OPTIONS.getLast());
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.ListPick(options());
        }
    }

    /**
     * Sylvan Library's "choose two cards drawn this turn; for each pay 4 life or put it on top of
     * your library" decision, collapsed into a single multi-select. {@code drawnThisTurnCardIds}
     * are the cards still in the player's hand that were drawn this turn (card views re-derived
     * from the hand at prompt time); {@code resolveCount} is how many of them must be resolved
     * ({@code min(2, drawnThisTurnCardIds.size())}). The player selects up to {@code resolveCount}
     * of those cards to put on top of their library; for each of the remaining resolved cards they
     * pay 4 life (forced to top a card instead when they cannot afford it).
     */
    record SylvanLibraryChoice(UUID playerId, java.util.List<UUID> drawnThisTurnCardIds, int resolveCount)
            implements PendingInteraction {

        public SylvanLibraryChoice {
            drawnThisTurnCardIds = java.util.List.copyOf(drawnThisTurnCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(drawnThisTurnCardIds, 0, resolveCount);
        }
    }

    /**
     * Discard a card from hand (DISCARD_CHOICE). {@code remainingCount} is the multi-pick
     * countdown including the upcoming pick; each answered pick begins a fresh record with
     * the decremented count (this replaces the old {@code InteractionState}
     * {@code discardRemainingCount} field for discards). {@code followUp} is the carry-over
     * work run when the whole sequence completes; re-begins pass it forward unchanged. When
     * {@code stopAfterDiscardingType} is set, a matching first pick makes the next pick optional;
     * {@code declinable} records that optional second pick.
     */
    record DiscardChoice(UUID playerId, java.util.List<Integer> validIndices,
                         int remainingCount, DiscardFollowUp followUp, String prompt,
                         CardType stopAfterDiscardingType, boolean declinable)
            implements PendingInteraction, HandChoice {

        public DiscardChoice(UUID playerId, java.util.List<Integer> validIndices,
                             int remainingCount, DiscardFollowUp followUp, String prompt) {
            this(playerId, validIndices, remainingCount, followUp, prompt, null, false);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.CardIndexPick(validIndices, declinable);
        }
    }

    /**
     * Exile a card from hand (EXILE_FROM_HAND_CHOICE), tracked with
     * {@code sourcePermanentId} when non-null (e.g. Karn Liberated). {@code remainingCount}
     * works as in {@link DiscardChoice}. A non-null {@code playPermissionControllerId}
     * grants that player permission to play each exiled card (e.g. Fiend of the Shadows).
     * {@code remainingChoosers} + {@code cardsPerPlayer} chain additional opponents after this
     * player's picks (Nicol Bolas, God-Pharaoh +1 "each opponent exiles two cards").
     * {@code faceDown} exiles the chosen card face down (CR 406.3), which also makes it visible
     * only to the source permanent's controller (Gustha's Scepter).
     */
    record ExileFromHandChoice(UUID playerId, java.util.List<Integer> validIndices,
                               UUID sourcePermanentId, UUID playPermissionControllerId,
                               int remainingCount, String prompt,
                               java.util.List<UUID> remainingChoosers, int cardsPerPlayer,
                               boolean faceDown, boolean returnOnSourceLeave)
            implements PendingInteraction, HandChoice {

        public ExileFromHandChoice(UUID playerId, java.util.List<Integer> validIndices,
                                   UUID sourcePermanentId, UUID playPermissionControllerId,
                                   int remainingCount, String prompt) {
            this(playerId, validIndices, sourcePermanentId, playPermissionControllerId,
                    remainingCount, prompt, java.util.List.of(), 0, false, false);
        }

        public ExileFromHandChoice(UUID playerId, java.util.List<Integer> validIndices,
                                   UUID sourcePermanentId, UUID playPermissionControllerId,
                                   int remainingCount, String prompt,
                                   java.util.List<UUID> remainingChoosers, int cardsPerPlayer) {
            this(playerId, validIndices, sourcePermanentId, playPermissionControllerId,
                    remainingCount, prompt, remainingChoosers, cardsPerPlayer, false, false);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.CardIndexPick(validIndices, false);
        }
    }

    /** Exile a card from hand and imprint it on {@code sourcePermanentId} (IMPRINT_FROM_HAND_CHOICE). */
    record ImprintFromHandChoice(UUID playerId, java.util.List<Integer> validIndices,
                                 UUID sourcePermanentId, String prompt, boolean grantCastPermission,
                                 boolean faceDown)
            implements PendingInteraction, HandChoice {

        public ImprintFromHandChoice(UUID playerId, java.util.List<Integer> validIndices,
                                     UUID sourcePermanentId, String prompt) {
            this(playerId, validIndices, sourcePermanentId, prompt, false, false);
        }

        public ImprintFromHandChoice(UUID playerId, java.util.List<Integer> validIndices,
                                     UUID sourcePermanentId, String prompt, boolean grantCastPermission) {
            this(playerId, validIndices, sourcePermanentId, prompt, grantCastPermission, false);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.CardIndexPick(validIndices, false);
        }
    }

    /**
     * Discard a card as an activation cost (ACTIVATED_ABILITY_DISCARD_COST_CHOICE). The
     * pending activation itself lives in {@link GameData#pendingAbilityActivation}.
     */
    record DiscardCostChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt)
            implements PendingInteraction, HandChoice {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.CardIndexPick(validIndices, false);
        }
    }

    /**
     * Select zero or more of the revealed/looked-at library cards (Lead the Stampede /
     * Genesis Wave battlefield picks, choose-N-to-hand looks, punisher reveals, Karn Scion
     * picks, ...). {@code allCards} are held out of the library; {@code validCardIds} keeps the
     * begin-time order (card views derive from it against {@code allCards} at prompt time).
     * {@code maxCount} and {@code prompt} are the exact begin-time message fields; a null
     * {@code prompt} means the begin site sent no choice message (the Karn Scion flows, which
     * prompt via the game-state broadcast alone) - nothing is sent on reconnect replay either,
     * matching begin. The boolean/punisher components drive the answer handling exactly as the
     * legacy context did.
     */
    record LibraryRevealChoice(UUID playerId, java.util.List<Card> allCards,
                               java.util.List<UUID> validCardIds, boolean remainingToGraveyard,
                               boolean selectedToHand, boolean reorderRemainingToBottom,
                               boolean randomRemainingToBottom, boolean remainingToExile,
                               int lifeCostPerSelection,
                               UUID beneficiaryPlayerId, int maxCount, String prompt,
                               boolean selectedToBattlefieldTapped, int minCount,
                               boolean gainLifeEqualToSelectedCardManaValue)
            implements PendingInteraction {

        public LibraryRevealChoice(UUID playerId, java.util.List<Card> allCards,
                                   java.util.List<UUID> validCardIds, boolean remainingToGraveyard,
                                   boolean selectedToHand, boolean reorderRemainingToBottom,
                                   boolean randomRemainingToBottom, boolean remainingToExile,
                                   int lifeCostPerSelection, UUID beneficiaryPlayerId, int maxCount,
                                   String prompt) {
            this(playerId, allCards, validCardIds, remainingToGraveyard, selectedToHand,
                    reorderRemainingToBottom, randomRemainingToBottom, remainingToExile,
                    lifeCostPerSelection, beneficiaryPlayerId, maxCount, prompt, false, 0, false);
        }

        public LibraryRevealChoice(UUID playerId, java.util.List<Card> allCards,
                                   java.util.List<UUID> validCardIds, boolean remainingToGraveyard,
                                   boolean selectedToHand, boolean reorderRemainingToBottom,
                                   boolean randomRemainingToBottom, boolean remainingToExile,
                                   int lifeCostPerSelection, UUID beneficiaryPlayerId, int maxCount,
                                   String prompt, boolean selectedToBattlefieldTapped) {
            this(playerId, allCards, validCardIds, remainingToGraveyard, selectedToHand,
                    reorderRemainingToBottom, randomRemainingToBottom, remainingToExile,
                    lifeCostPerSelection, beneficiaryPlayerId, maxCount, prompt,
                    selectedToBattlefieldTapped, 0, false);
        }

        public LibraryRevealChoice(UUID playerId, java.util.List<Card> allCards,
                                   java.util.List<UUID> validCardIds, boolean remainingToGraveyard,
                                   boolean selectedToHand, boolean reorderRemainingToBottom,
                                   boolean randomRemainingToBottom, boolean remainingToExile,
                                   int lifeCostPerSelection, UUID beneficiaryPlayerId, int maxCount,
                                   String prompt, int minCount, boolean gainLifeEqualToSelectedCardManaValue) {
            this(playerId, allCards, validCardIds, remainingToGraveyard, selectedToHand,
                    reorderRemainingToBottom, randomRemainingToBottom, remainingToExile,
                    lifeCostPerSelection, beneficiaryPlayerId, maxCount, prompt,
                    false, minCount, gainLifeEqualToSelectedCardManaValue);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, minCount, maxCount);
        }
    }

    /** Selects at most one revealed card for one color during Sanar's Vivid ability. */
    record VividCardChoice(UUID playerId, java.util.List<Card> revealedCards,
                           java.util.List<UUID> validCardIds, java.util.List<CardColor> colors,
                           int nextColorIndex, java.util.List<UUID> selectedCardIds,
                           String prompt) implements PendingInteraction {

        public VividCardChoice {
            revealedCards = java.util.List.copyOf(revealedCards);
            validCardIds = java.util.List.copyOf(validCardIds);
            colors = java.util.List.copyOf(colors);
            selectedCardIds = java.util.List.copyOf(selectedCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, 1);
        }
    }

    /**
     * Search-style pick of one card from a presented library subset (tutors, look-at-top-N
     * picks, Head Games, Sphinx Ambassador, ...). {@code params} is the immutable
     * {@link LibrarySearchParams} the begin site built - the multi-pick countdown
     * ({@code remainingCount}/{@code accumulatedCards}) advances by beginning a fresh record.
     * {@code messagePrompt} and {@code messageCanFailToFind} are the exact begin-time
     * {@code InteractionPromptMessage} library-pick fields (some sites word the message differently
     * from {@code params.prompt()}); both are re-sent verbatim on reconnect.
     */
    record LibrarySearch(LibrarySearchParams params, String messagePrompt,
                         boolean messageCanFailToFind) implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return params.playerId();
        }

        @Override
        public InteractionOptions legalOptions() {
            // The answer handler's decline rule reads params.canFailToFind(), not the message flag.
            return new InteractionOptions.LibraryIndexPick(
                    params.cards() != null ? params.cards().size() : 0, params.canFailToFind());
        }
    }

    /** Chooses at most one eligible card from outside the game or face-up exile. */
    record SearchOutsideGameOrExileCardChoice(UUID playerId, java.util.List<UUID> validCardIds,
                                               CardPredicate filter, String cardLabel) implements PendingInteraction {

        public SearchOutsideGameOrExileCardChoice {
            validCardIds = java.util.List.copyOf(validCardIds);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.MultiCardPick(validCardIds, 0, 1);
        }
    }

    /**
     * "Choose a permanent" / "choose any target" — the single-pick battlefield/player
     * targeting prompt serving the ~45 {@link PermanentChoiceContext} operations (trigger-slot
     * targets, sacrifices, clone copies, spell retargets, aura placement, ...). Carries the two
     * begin-time ordered ID lists exactly as the {@code InteractionPromptMessage} permanent pick sent them
     * ({@code validPlayerIds} is empty for the plain permanent variant) plus the begin-time
     * {@code prompt}; validation uses the merged {@link #validIds()} set, as legacy did.
     * {@code context} is the begin-time snapshot of the pre-seeded
     * {@code InteractionState.permanentChoiceContext} carrier field and drives the answer
     * dispatch (a null context falls through to the pending-aura placement path).
     */
    record PermanentChoice(UUID playerId, java.util.List<UUID> validPermanentIds,
                           java.util.List<UUID> validPlayerIds, PermanentChoiceContext context,
                           String prompt) implements PendingInteraction {

        /** Merged valid-target IDs (permanents + players), the legacy validation set. */
        public java.util.Set<UUID> validIds() {
            java.util.Set<UUID> all = new java.util.LinkedHashSet<>(validPermanentIds);
            all.addAll(validPlayerIds);
            return all;
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return new InteractionOptions.PermanentPick(java.util.List.copyOf(validIds()));
        }
    }

    /**
     * Ad Nauseam: after each mandatory reveal, {@code playerId} decides whether to repeat the
     * process (reveal the next top card, put it into hand, lose life equal to its mana value).
     * {@code sourceName} is the spell's name (for the life-loss source / log text). Accepting
     * performs another iteration and re-prompts (while the library is non-empty); declining ends
     * the resolution. Answered via the shared may-ability accept/decline wire payload.
     */
    record AdNauseamRepeatChoice(UUID playerId, String sourceName) implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.ACCEPT_DECLINE;
        }
    }

    /**
     * Forbidden Ritual: after each completed cycle (controller sacrificed a nontoken permanent and
     * the targeted opponent answered the three-way penalty), {@code playerId} decides whether to
     * sacrifice another nontoken permanent and repeat against the same opponent. Accepting starts
     * the next controller-sacrifice step while nontoken permanents remain; declining ends the
     * resolution. Answered via the shared may-ability accept/decline wire payload.
     */
    record ForbiddenRitualRepeatChoice(UUID playerId, String sourceName) implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.ACCEPT_DECLINE;
        }
    }

    /**
     * Primal Surge: {@code cardId} (name {@code cardName}) has just been exiled from the top of
     * {@code playerId}'s library and is a permanent card, so they may put it onto the battlefield.
     * Accepting puts it onto the battlefield and exiles the next card; declining ends the process
     * and leaves the card in exile. {@code sourceName} is the spell's name (log text). Answered via
     * the shared may-ability accept/decline wire payload.
     */
    record ExiledPermanentPutOntoBattlefieldChoice(UUID playerId, String sourceName, UUID cardId,
                                                   String cardName) implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.ACCEPT_DECLINE;
        }
    }

    /**
     * Lim-Dûl's Vault: after each look at the top five cards, {@code playerId} decides whether to
     * pay 1 life to bottom {@code lookedAt} and look at five more. The looked-at cards are held
     * out of the library while this is pending, and their names are spelled out in the prompt
     * text (the accept/decline wire shape carries no card list). Accepting is legal only while
     * the deciding player's life total is at least 1 (CR 119.4).
     */
    record LimDulsVaultRepeatChoice(UUID playerId, java.util.List<Card> lookedAt)
            implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.ACCEPT_DECLINE;
        }
    }

    /**
     * Lim-Dûl's Vault: the "in any order" ordering of the held-out {@code cards}, either onto the
     * bottom of the library after an accepted repeat ({@code toBottom}) or onto the top after the
     * final shuffle. A dedicated kind rather than {@link LibraryReorder} because answering it must
     * continue the vault loop instead of ending the resolution.
     */
    record LimDulsVaultOrderChoice(UUID playerId, java.util.List<Card> cards, boolean toBottom)
            implements PendingInteraction {

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.UNENUMERATED;
        }
    }

    /**
     * The active player's combat damage assignment for one attacker blocked by multiple
     * creatures (or with a trample/unblocked overflow target). Fired mid-damage-step by
     * {@code CombatDamageService}; answering feeds back into the damage-resolution loop,
     * which begins a fresh record for the next pending attacker. The answer is validated
     * against the combat state on {@code GameData} ({@code combatDamagePendingIndices}, the
     * assignment math), not this record — the record carries exactly the begin-time
     * notification content for the prompt and reconnect replay.
     */
    record CombatDamageAssignment(UUID playerId, int attackerIndex, UUID attackerPermanentId,
                                  String attackerName, int totalDamage,
                                  java.util.List<CombatDamageTarget> validTargets,
                                  boolean isTrample, boolean isDeathtouch, boolean singleRecipient)
            implements PendingInteraction {

        public CombatDamageAssignment {
            validTargets = java.util.List.copyOf(validTargets);
        }

        @Override
        public UUID decidingPlayerId() {
            return playerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.UNENUMERATED;
        }
    }

    /**
     * The active player's attacker declaration. All legality metadata is captured before the
     * decision event is emitted so initial delivery, invalid-answer retry, and reconnect replay
     * project the same finalized legal answer space.
     */
    record AttackerDeclaration(UUID activePlayerId,
                               java.util.List<Integer> attackerIndices,
                               java.util.List<Integer> mustAttackIndices,
                               java.util.List<CombatAttackTarget> availableTargets,
                               int taxPerCreature,
                               boolean mustAttackWithAtLeastOne) implements PendingInteraction {

        public AttackerDeclaration {
            attackerIndices = java.util.List.copyOf(attackerIndices);
            mustAttackIndices = java.util.List.copyOf(mustAttackIndices);
            availableTargets = java.util.List.copyOf(availableTargets);
        }

        public AttackerDeclaration(UUID activePlayerId) {
            this(activePlayerId, java.util.List.of(), java.util.List.of(),
                    java.util.List.of(), 0, false);
        }

        @Override
        public UUID decidingPlayerId() {
            return activePlayerId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.UNENUMERATED;
        }
    }

    /**
     * The blocker declaration. Legal pairs and requirement metadata are captured before the
     * decision event is emitted so no later projection can observe a partially-computed or changed
     * legal answer space.
     *
     * <p>{@code defenderId} always owns the blocking creatures the indices refer to.
     * {@code chooserId} is the player who actually makes the declaration — normally the defender,
     * but a "you choose which creatures block this combat" effect (Melee) hands it to someone else.
     */
    record BlockerDeclaration(UUID defenderId,
                              java.util.List<Integer> blockerIndices,
                              java.util.List<Integer> attackerIndices,
                              java.util.Map<Integer, java.util.List<Integer>> legalBlockPairs,
                              java.util.List<Integer> mustBeBlockedAttackerIndices,
                              java.util.List<Integer> menaceAttackerIndices,
                              java.util.Map<Integer, java.util.List<Integer>> mustBlockRequirements,
                              UUID chooserId)
            implements PendingInteraction {

        public BlockerDeclaration {
            if (chooserId == null) {
                chooserId = defenderId;
            }
            blockerIndices = java.util.List.copyOf(blockerIndices);
            attackerIndices = java.util.List.copyOf(attackerIndices);
            legalBlockPairs = copyIndexMap(legalBlockPairs);
            mustBeBlockedAttackerIndices = java.util.List.copyOf(mustBeBlockedAttackerIndices);
            menaceAttackerIndices = java.util.List.copyOf(menaceAttackerIndices);
            mustBlockRequirements = copyIndexMap(mustBlockRequirements);
        }

        public BlockerDeclaration(UUID defenderId) {
            this(defenderId, java.util.List.of(), java.util.List.of(), java.util.Map.of(),
                    java.util.List.of(), java.util.List.of(), java.util.Map.of(), defenderId);
        }

        /**
         * True when the declaring player is choosing blocks for an opponent's creatures (Melee),
         * i.e. the blocker indices refer to a battlefield the chooser does not control.
         */
        public boolean choosingForOpponent() {
            return !chooserId.equals(defenderId);
        }

        private static java.util.Map<Integer, java.util.List<Integer>> copyIndexMap(
                java.util.Map<Integer, java.util.List<Integer>> source) {
            java.util.Map<Integer, java.util.List<Integer>> copy = new java.util.LinkedHashMap<>();
            source.forEach((key, value) -> copy.put(key, java.util.List.copyOf(value)));
            return java.util.Collections.unmodifiableMap(copy);
        }

        @Override
        public UUID decidingPlayerId() {
            return chooserId;
        }

        @Override
        public InteractionOptions legalOptions() {
            return InteractionOptions.UNENUMERATED;
        }
    }
}
