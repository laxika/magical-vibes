package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * A queued player decision: everything the engine needs to prompt the deciding player and
 * apply their answer later. Instances wait in {@link GameData#pendingInteractions} until
 * serviced; consumers scan the queue for the first entry of the kind they handle (see the
 * type-filtered helpers on {@link GameData}), which preserves FIFO order per kind.
 *
 * <p>This is the unification point for the legacy pending-choice subsystem: the
 * {@link PermanentChoiceContext} records are the first members, and the remaining
 * {@code Pending*} / {@code ChoiceContext} shapes migrate here incrementally
 * (see {@code REFACTOR-NOTES.md} at the repository root).
 */
public sealed interface PendingInteraction permits PermanentChoiceContext,
        PendingSphinxAmbassadorChoice, PendingCapriciousEfreetState,
        PendingKarnScionRevealChoice, PendingKarnScionExileReturn,
        PendingKarnRestart, PendingKnowledgePoolCast, PendingPileSeparation,
        PendingInteraction.XValueChoice, PendingInteraction.Scry,
        PendingInteraction.HandTopBottomChoice, PendingInteraction.LibraryReorder,
        PendingInteraction.MayAbilityChoice, PendingInteraction.KnowledgePoolCastChoice,
        PendingInteraction.ImprovisationCapstoneCastChoice,
        PendingInteraction.MirrorOfFateChoice, PendingInteraction.MultiZoneExileChoice,
        PendingInteraction.MultiPermanentChoice, PendingInteraction.MultiGraveyardChoice,
        PendingInteraction.ColorChoice, PendingInteraction.RevealedHandChoice,
        PendingInteraction.RevealCardsFromHandChoice,
        PendingInteraction.ChooseRevealedCardToDiscardChoice,
        PendingInteraction.RevealCardsDiscardChoice,
        PendingInteraction.GraveyardChoice, PendingInteraction.GraveyardExileCostChoice,
        PendingInteraction.HandCardChoice, PendingInteraction.TargetedHandCardChoice,
        PendingInteraction.DiscardChoice, PendingInteraction.ExileFromHandChoice,
        PendingInteraction.ImprintFromHandChoice, PendingInteraction.DiscardCostChoice,
        PendingInteraction.LibraryRevealChoice,
        PendingInteraction.LibrarySearch,
        PendingInteraction.PermanentChoice,
        PendingInteraction.CombatDamageAssignment,
        PendingInteraction.AttackerDeclaration,
        PendingInteraction.BlockerDeclaration {

    // ------------------------------------------------------------------
    // Generic interaction kinds. Each record carries everything needed to
    // prompt the deciding player and apply the answer (dispatched via the
    // engine's InteractionHandlerRegistry).
    // ------------------------------------------------------------------

    /** "Choose a value for X" (e.g. Vigil for the Lost's ETB payment, Jaya's rummage count). */
    record XValueChoice(UUID playerId, int maxValue, String prompt, String cardName)
            implements PendingInteraction {
    }

    /** Scry N: {@code cards} are held out of the library while the player splits them top/bottom. */
    record Scry(UUID playerId, java.util.List<Card> cards) implements PendingInteraction {
    }

    /** "Look at the top N cards: one to hand, one on top, rest on the bottom" (e.g. Anticipate-style picks). */
    record HandTopBottomChoice(UUID playerId, java.util.List<Card> cards) implements PendingInteraction {
    }

    /**
     * Put the given cards on the top (or bottom) of {@code deckOwnerId}'s library in an order
     * of the deciding player's choosing. {@code prompt} is the exact text shown at begin time
     * (also re-sent on reconnect).
     */
    record LibraryReorder(UUID playerId, java.util.List<Card> cards, boolean toBottom,
                          UUID deckOwnerId, String prompt) implements PendingInteraction {
    }

    /**
     * Accept/decline prompt for the head of {@link GameData#pendingMayAbilities}.
     * {@code description} and {@code manaCost} mirror that head entry; whether the player
     * can currently pay {@code manaCost} is computed at prompt time from their mana pool.
     */
    record MayAbilityChoice(UUID playerId, String description, String manaCost)
            implements PendingInteraction {
    }

    /**
     * Knowledge Pool: the caster may cast one of the pool's other nonland exiled cards without
     * paying its cost (or decline with an empty selection). {@code validCardIds} keeps the
     * begin-time order; the card views are re-derived from the pool at prompt time (the pool
     * permanent is found via the queued {@link PendingKnowledgePoolCast}).
     */
    record KnowledgePoolCastChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount)
            implements PendingInteraction {
    }

    /**
     * Improvisation Capstone: choose any number of exiled spells to cast without paying their mana costs.
     */
    record ImprovisationCapstoneCastChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount)
            implements PendingInteraction {
    }

    /**
     * Mirror of Fate: choose up to seven face-up exiled cards to put on top of the library.
     * {@code validCardIds} keeps the begin-time order; views are re-derived from the player's
     * exile zone at prompt time.
     */
    record MirrorOfFateChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount)
            implements PendingInteraction {
    }

    /**
     * "Exile any number of cards named X" from {@code targetPlayerId}'s hand, graveyard, and
     * library (e.g. Memoricide-style effects). {@code validCardIds} keeps the begin-time
     * hand → graveyard → library scan order; views are re-derived by the same scan at prompt
     * time. {@code controllerId} is the effect's controller (same as the deciding player).
     */
    record MultiZoneExileChoice(UUID playerId, java.util.List<UUID> validCardIds, int maxCount,
                                UUID targetPlayerId, UUID controllerId, String cardName)
            implements PendingInteraction {
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
    }

    /**
     * Select zero or more cards from a graveyard-sourced list (graveyard-targeting spells and
     * triggers, plus pile separation over just-exiled cards). {@code cards} keeps the begin-time
     * order — IDs and card views are derived from it at prompt time. {@code prompt} is the
     * exact begin-time text.
     */
    record MultiGraveyardChoice(UUID playerId, java.util.List<Card> cards, int maxCount,
                                String prompt) implements PendingInteraction {

        /** The selectable card IDs, in begin-time order (derived from {@link #cards}). */
        public java.util.List<UUID> validCardIds() {
            return cards.stream().map(Card::getId).toList();
        }
    }

    /**
     * A single-value "choose from a list" decision covering the whole legacy COLOR_CHOICE
     * family (mana color, protection color, keyword/subtype/permanent-type/basic-land-type,
     * card name, text-change word, Abundance land/nonland, …). The specific variant is carried
     * in {@code context} (a {@link ChoiceContext}) and drives answer handling. {@code options}
     * and {@code prompt} are the exact begin-time list options and prompt text — they are
     * carried so reconnect replay re-sends byte-identical content (the legacy replay re-derived
     * them from {@code context} and diverged for several variants). {@code permanentId} /
     * {@code etbTargetId} carry the plain ETB color-choice permanent context.
     */
    record ColorChoice(UUID playerId, UUID permanentId, UUID etbTargetId, ChoiceContext context,
                       java.util.List<String> options, String prompt) implements PendingInteraction {
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
     * Clique); {@code optional} lets the caster decline (answer {@code cardIndex == -1}) even
     * when a legal choice exists.
     */
    record RevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId,
                              java.util.List<Integer> validIndices, int remainingCount,
                              boolean discardMode, boolean exileMode,
                              java.util.List<Card> chosenCards, UUID sourcePermanentId,
                              String prompt, boolean bottomThenDrawMode, boolean optional)
            implements PendingInteraction {
    }

    /**
     * Phase 1 of Thieving Sprite ({@link com.github.laxika.magicalvibes.model.effect.RevealCardsChooseOneToDiscardEffect}):
     * the target ({@code playerId}) chooses which cards from their own hand to reveal, one pick at a time.
     * {@code validIndices} are the still-selectable hand indices (cards stay in hand, so indices are stable
     * across picks); {@code remainingCount} counts the picks left including the upcoming one; {@code revealedCards}
     * accumulates the cards revealed so far. When the countdown ends the caster ({@code choosingPlayerId})
     * is prompted with a {@link ChooseRevealedCardToDiscardChoice} over the revealed cards.
     */
    record RevealCardsFromHandChoice(UUID playerId, UUID choosingPlayerId,
                                     java.util.List<Integer> validIndices, int remainingCount,
                                     java.util.List<Card> revealedCards, String prompt)
            implements PendingInteraction {
    }

    /**
     * Phase 2 of Thieving Sprite: the caster ({@code choosingPlayerId}) chooses one of the target's
     * ({@code targetPlayerId}) {@code revealedCards} for the target to discard. The answer's card index
     * is into {@code revealedCards} (only the revealed subset is shown to the caster, keeping the rest of
     * the hand hidden).
     */
    record ChooseRevealedCardToDiscardChoice(UUID choosingPlayerId, UUID targetPlayerId,
                                             java.util.List<Card> revealedCards, String prompt)
            implements PendingInteraction {
    }

    /**
     * The two-stage Blackmail flow ("Target player reveals N cards from their hand and you choose
     * one of them. That player discards that card."). In the reveal stage {@code decidingPlayerId}
     * is the {@code targetPlayerId}, who picks which cards to reveal: {@code validIndices} are the
     * still-selectable indices into their hand, {@code remainingCount} counts down the reveals, and
     * {@code revealedCardIds} accumulates the chosen (now public) card ids. When the countdown ends
     * a fresh record begins the discard stage, where {@code decidingPlayerId} is the
     * {@code controllerId}: {@code revealedCardIds} is the fixed revealed set shown to the
     * controller and {@code validIndices} are the indices into that set. Answers are
     * {@link com.github.laxika.magicalvibes.model.effect.CardEffect}-agnostic {@code CardIndexChosen}
     * picks, dispatched by the deciding player.
     */
    record RevealCardsDiscardChoice(UUID decidingPlayerId, UUID targetPlayerId, UUID controllerId,
                                    boolean revealStage, java.util.List<Integer> validIndices,
                                    int remainingCount, java.util.List<UUID> revealedCardIds,
                                    String prompt, int discardCount) implements PendingInteraction {
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
                           String prompt)
            implements PendingInteraction {

        public static Builder builder(UUID playerId, java.util.List<Integer> validIndices,
                                      GraveyardChoiceDestination destination, String prompt) {
            return new Builder(playerId, validIndices, destination, prompt);
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

            public GraveyardChoice build() {
                return new GraveyardChoice(playerId, validIndices, destination, cardPool,
                        gainLifeEqualToManaValue, attachToSourcePermanentId, grantColor, grantSubtype,
                        exileRemainingCount, gainLifeIfCreatureAmount, gainLifeIfCreaturePlayerId,
                        trackWithSourcePermanentId, mayAbilitySourceCard, mayAbilityControllerId,
                        mayAbilityEffects, mayAbilitySourcePermanentId,
                        grantSourceHasteIfSubtype, grantSourceHasteSourcePermanentId, prompt);
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
    }

    /**
     * Common surface of the six hand-card choice kinds (the legacy shared
     * {@code InteractionContext.CardChoice} family): the deciding player, the selectable hand
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
                          boolean enterAttacking)
            implements PendingInteraction, HandChoice {

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt) {
            this(playerId, validIndices, prompt, false, false, false, null, false);
        }

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped) {
            this(playerId, validIndices, prompt, enterTapped, false, false, null, false);
        }

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped,
                              boolean grantHaste, boolean sacrificeAtEndStep) {
            this(playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, null, false);
        }

        public HandCardChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt, boolean enterTapped,
                              boolean grantHaste, boolean sacrificeAtEndStep, UUID attachEquipmentCardId) {
            this(playerId, validIndices, prompt, enterTapped, grantHaste, sacrificeAtEndStep, attachEquipmentCardId, false);
        }
    }

    /**
     * Put an Aura card from hand onto the battlefield attached to {@code targetId},
     * declinable (TARGETED_CARD_CHOICE).
     */
    record TargetedHandCardChoice(UUID playerId, java.util.List<Integer> validIndices,
                                  UUID targetId, String prompt)
            implements PendingInteraction, HandChoice {
    }

    /**
     * Discard a card from hand (DISCARD_CHOICE). {@code remainingCount} is the multi-pick
     * countdown including the upcoming pick; each answered pick begins a fresh record with
     * the decremented count (this replaces the old {@code InteractionState}
     * {@code discardRemainingCount} field for discards). {@code followUp} is the carry-over
     * work run when the whole sequence completes; re-begins pass it forward unchanged.
     */
    record DiscardChoice(UUID playerId, java.util.List<Integer> validIndices,
                         int remainingCount, DiscardFollowUp followUp, String prompt)
            implements PendingInteraction, HandChoice {
    }

    /**
     * Exile a card from hand (EXILE_FROM_HAND_CHOICE), tracked with
     * {@code sourcePermanentId} when non-null (e.g. Karn Liberated). {@code remainingCount}
     * works as in {@link DiscardChoice}. A non-null {@code playPermissionControllerId}
     * grants that player permission to play each exiled card (e.g. Fiend of the Shadows).
     */
    record ExileFromHandChoice(UUID playerId, java.util.List<Integer> validIndices,
                               UUID sourcePermanentId, UUID playPermissionControllerId,
                               int remainingCount, String prompt)
            implements PendingInteraction, HandChoice {
    }

    /** Exile a card from hand and imprint it on {@code sourcePermanentId} (IMPRINT_FROM_HAND_CHOICE). */
    record ImprintFromHandChoice(UUID playerId, java.util.List<Integer> validIndices,
                                 UUID sourcePermanentId, String prompt)
            implements PendingInteraction, HandChoice {
    }

    /**
     * Discard a card as an activation cost (ACTIVATED_ABILITY_DISCARD_COST_CHOICE). The
     * pending activation itself lives in {@link GameData#pendingAbilityActivation}.
     */
    record DiscardCostChoice(UUID playerId, java.util.List<Integer> validIndices, String prompt)
            implements PendingInteraction, HandChoice {
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
                               boolean randomRemainingToBottom, int lifeCostPerSelection,
                               UUID beneficiaryPlayerId, int maxCount, String prompt)
            implements PendingInteraction {
    }

    /**
     * Search-style pick of one card from a presented library subset (tutors, look-at-top-N
     * picks, Head Games, Sphinx Ambassador, ...). {@code params} is the immutable
     * {@link LibrarySearchParams} the begin site built - the multi-pick countdown
     * ({@code remainingCount}/{@code accumulatedCards}) advances by beginning a fresh record.
     * {@code messagePrompt} and {@code messageCanFailToFind} are the exact begin-time
     * {@code ChooseCardFromLibraryMessage} fields (some sites word the message differently
     * from {@code params.prompt()}); both are re-sent verbatim on reconnect.
     */
    record LibrarySearch(LibrarySearchParams params, String messagePrompt,
                         boolean messageCanFailToFind) implements PendingInteraction {
    }

    /**
     * "Choose a permanent" / "choose any target" — the single-pick battlefield/player
     * targeting prompt serving the ~45 {@link PermanentChoiceContext} operations (trigger-slot
     * targets, sacrifices, clone copies, spell retargets, aura placement, ...). Carries the two
     * begin-time ordered ID lists exactly as the {@code ChoosePermanentMessage} sent them
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
    }

    /**
     * The active player's attacker declaration. The available-attackers notification is
     * re-derived from live combat state at prompt time (the legacy begin site and reconnect
     * replay both did the same re-derivation), so the record carries only the decider.
     */
    record AttackerDeclaration(UUID activePlayerId) implements PendingInteraction {
    }

    /**
     * The defending player's blocker declaration. The available-blockers notification is
     * re-derived from live combat state at prompt time, so the record carries only the decider.
     */
    record BlockerDeclaration(UUID defenderId) implements PendingInteraction {
    }
}
