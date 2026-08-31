package com.github.laxika.magicalvibes.service.graveyard;

import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.OpponentGraveyardLifeLossWatcher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerOpponentMillBonusEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardToTopOfLibraryInsteadEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureLibraryReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureCardAwareEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInstantSorceryCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.OwnGraveyardExileReplacement;
import com.github.laxika.magicalvibes.model.effect.OpponentCreatureCardExileReplacement;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MadnessMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.DestructionReplacement;
import com.github.laxika.magicalvibes.model.effect.DestructionReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardPutIntoGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAndPutOnBottomOfLibraryInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileWithEggCountersInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAndTakeExtraTurnReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GraveyardService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PermanentRemovalService permanentRemovalService;
    // @Lazy to break indirect circular dependency:
    // GraveyardService → TriggerCollectionService → PermanentRemovalService → GraveyardService
    private TriggerCollectionService triggerCollectionService;

    public GraveyardService(GameQueryService gameQueryService,
                            GameLogService gameLogService,
                            ExileService exileService,
                            PredicateEvaluationService predicateEvaluationService,
                            PermanentCounterSupport permanentCounterSupport,
                            @Lazy TriggerCollectionService triggerCollectionService) {
        this(gameQueryService, gameLogService, exileService, predicateEvaluationService,
                permanentCounterSupport, null, triggerCollectionService);
    }

    @Autowired
    public GraveyardService(GameQueryService gameQueryService,
                            GameLogService gameLogService,
                            ExileService exileService,
                            PredicateEvaluationService predicateEvaluationService,
                            PermanentCounterSupport permanentCounterSupport,
                            @Lazy PermanentRemovalService permanentRemovalService,
                            @Lazy TriggerCollectionService triggerCollectionService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.exileService = exileService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.permanentCounterSupport = permanentCounterSupport;
        this.permanentRemovalService = permanentRemovalService;
        this.triggerCollectionService = triggerCollectionService;
    }

    /**
     * Sets the TriggerCollectionService for manual (non-Spring) construction where
     * the circular dependency prevents passing it in the constructor.
     */
    public void setTriggerCollectionService(TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
    }


    /**
     * Mills {@code count} cards from the target player's library, returning the cards that actually
     * reached the graveyard (a replacement effect can divert one). Callers that need to act on the
     * milled cards — e.g. Jace's Mindseeker's "cast an instant or sorcery from among them" — use the
     * returned list; everyone else ignores it.
     */
    public List<Card> resolveMillPlayer(GameData gameData, UUID targetPlayerId, int count) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        gameData.lastMilledCardColorSymbols.clear();
        int additionalCards = 0;
        if (count > 0) {
            int[] bonus = {0};
            gameData.forEachPermanent((controllerId, permanent) -> {
                if (controllerId.equals(targetPlayerId)
                        || permanent.isFaceDown()
                        || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
                    return;
                }
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ControllerOpponentMillBonusEffect millBonus) {
                        bonus[0] += millBonus.amount();
                    }
                }
            });
            additionalCards = bonus[0];
        }
        int cardsToMill = Math.min(count + additionalCards, deck.size());
        List<Card> milledCards = new ArrayList<>(deck.subList(0, cardsToMill));
        deck.subList(0, cardsToMill).clear();
        List<Card> cardsEnteredGraveyard = new ArrayList<>();
        for (Card card : milledCards) {
            boolean entered = addCardToGraveyard(gameData, targetPlayerId, card, Zone.LIBRARY, true);
            if (entered) {
                cardsEnteredGraveyard.add(card);
            }
        }
        if (!cardsEnteredGraveyard.isEmpty()) {
            ManaCost manaCost = cardsEnteredGraveyard.getLast().getParsedManaCost();
            if (manaCost != null) {
                for (ManaColor color : ManaColor.COLORS) {
                    int symbols = manaCost.countColorSymbols(color);
                    if (symbols > 0) {
                        gameData.lastMilledCardColorSymbols.put(color, symbols);
                    }
                }
            }
        }
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = playerName + " mills " + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ".";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} mills {} cards", gameData.id, playerName, cardsToMill);

        int creatureCardsEntered = (int) cardsEnteredGraveyard.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .count();
        triggerCollectionService.checkCreatureCardsPutIntoGraveyardFromLibraryTriggers(
                gameData, targetPlayerId, creatureCardsEntered);

        // Fire creature-card-milled triggers (e.g. Undead Alchemist)
        for (Card card : cardsEnteredGraveyard) {
            if (card.hasType(CardType.CREATURE)) {
                triggerCollectionService.checkCreatureCardMilledTriggers(gameData, targetPlayerId, card);
            }
        }

        // Fire self-mill triggers after the cards have entered the graveyard.
        for (Card card : cardsEnteredGraveyard) {
            for (CardEffect effect : card.getEffects(EffectSlot.ON_SELF_MILLED)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        targetPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        (UUID) null
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} triggers on being milled", gameData.id, card.getName());
            }
        }
        return cardsEnteredGraveyard;
    }

    /**
     * Moves cards from the top of a player's library into that player's graveyard without treating
     * the event as milling.
     */
    public List<Card> resolvePutTopCardsIntoGraveyard(GameData gameData, UUID targetPlayerId, int count) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        int cardsToMove = Math.min(count, deck.size());
        List<Card> movedCards = new ArrayList<>(deck.subList(0, cardsToMove));
        deck.subList(0, cardsToMove).clear();
        List<Card> cardsEnteredGraveyard = new ArrayList<>();
        for (Card card : movedCards) {
            if (addCardToGraveyard(gameData, targetPlayerId, card, Zone.LIBRARY,
                    false, null, null, null, false, true, false)) {
                cardsEnteredGraveyard.add(card);
            }
        }
        return cardsEnteredGraveyard;
    }

    /**
     * Adds a card to its owner's graveyard, or applies a replacement effect (e.g. shuffle into library).
     * Returns true if the card was actually put into the graveyard, false if a replacement effect was applied.
     * Callers should skip "dies" / graveyard triggers when this returns false (CR 614.6).
     */
    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card) {
        return addCardToGraveyard(gameData, ownerId, card, null);
    }

    /**
     * Moves a discarded card to its owner's graveyard, applying Library of Leng's replacement first:
     * if the owner controls a permanent with {@link DiscardToTopOfLibraryInsteadEffect}, the card is
     * put on top of their library instead. Returns true if the card actually entered the graveyard,
     * false if the library-top replacement was applied (callers should skip graveyard-entry triggers,
     * but discard triggers still fire — the card was still discarded). Tokens cease to exist and never
     * see the replacement (they still go through the normal path).
     */
    public boolean discardCard(GameData gameData, UUID ownerId, Card card) {
        if (!card.isToken() && ownerHasDiscardToLibraryReplacement(gameData, ownerId)) {
            gameData.playerDecks.get(ownerId).add(0, card);
            gameLogService.append(gameData, GameLog.cardThen(card, " is put on top of its owner's library instead of into the graveyard."));
            log.info("Game {} - {} discard replacement: put on top of library instead of graveyard", gameData.id, card.getName());
            return false;
        }
        // Madness (CR 702.34a): discard into exile; triggered ability offers cast for madness cost.
        // Cost is snapshotted now so granted madness still works if the grant source leaves the BF
        // before the trigger resolves (Falkenrath Gorger ruling). Native MadnessCast preferred over grant.
        String madnessCost = null;
        if (!card.isToken()) {
            madnessCost = card.getCastingOption(MadnessCast.class)
                    .map(MadnessCast::manaCostString)
                    .orElseGet(() -> gameQueryService.findGrantedMadnessCost(gameData, ownerId, card).orElse(null));
        }
        if (madnessCost != null) {
            exileService.exileCard(gameData, ownerId, card);
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is discarded into exile (madness)."));
            log.info("Game {} - {} discarded into exile for madness", gameData.id, card.getName());
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    ownerId,
                    card.getName() + "'s madness",
                    new ArrayList<>(List.of(new MadnessMayCastEffect(madnessCost)))
            ));
            gameData.priorityPassedBy.clear();
            return false;
        }
        return addCardToGraveyard(gameData, ownerId, card);
    }

    private boolean ownerHasDiscardToLibraryReplacement(GameData gameData, UUID ownerId) {
        List<Permanent> bf = gameData.playerBattlefields.get(ownerId);
        if (bf == null) {
            return false;
        }
        for (Permanent p : bf) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof DiscardToTopOfLibraryInsteadEffect replacement
                        && (!replacement.opponentCausedOnly() || gameData.discardCausedByOpponent)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone) {
        return addCardToGraveyard(gameData, ownerId, card, sourceZone, false,
                null, null, null, false, false, false);
    }

    /**
     * Moves a permanent card to its owner's graveyard while preserving its last battlefield
     * controller for self-replacement effects that grant an extra turn.
     */
    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone,
                                      UUID battlefieldControllerId) {
        return addCardToGraveyard(gameData, ownerId, card, sourceZone, false,
                battlefieldControllerId, null, null, false, false, false);
    }

    /**
     * Moves a permanent card to its owner's graveyard while preserving the battlefield permanent's
     * ID for triggered abilities that refer to the permanent after it leaves the battlefield.
     */
    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone,
                                      UUID battlefieldControllerId, UUID battlefieldPermanentId) {
        return addCardToGraveyard(gameData, ownerId, card, sourceZone, false,
                battlefieldControllerId, battlefieldPermanentId, null, false, false, false);
    }

    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone,
                                      UUID battlefieldControllerId, UUID battlefieldPermanentId,
                                      boolean selfGraveyardTriggerSuppressed) {
        return addCardToGraveyard(gameData, ownerId, card, sourceZone, false,
                battlefieldControllerId, battlefieldPermanentId, null,
                selfGraveyardTriggerSuppressed, false, false);
    }

    /**
     * Moves a permanent card to its owner's graveyard while preserving its last battlefield state
     * for triggers that resolve after the permanent has left the battlefield.
     */
    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone,
                                      UUID battlefieldControllerId, Permanent battlefieldSnapshot) {
        return addCardToGraveyard(gameData, ownerId, card, sourceZone, false,
                battlefieldControllerId, battlefieldSnapshot == null ? null : battlefieldSnapshot.getId(),
                battlefieldSnapshot, false, false, false);
    }

    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone,
                                      UUID battlefieldControllerId, Permanent battlefieldSnapshot,
                                      boolean selfGraveyardTriggerSuppressed) {
        return addCardToGraveyard(gameData, ownerId, card, sourceZone, false,
                battlefieldControllerId, battlefieldSnapshot == null ? null : battlefieldSnapshot.getId(),
                battlefieldSnapshot, selfGraveyardTriggerSuppressed, false, false);
    }

    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone,
                                      UUID battlefieldControllerId, Permanent battlefieldSnapshot,
                                      boolean selfGraveyardTriggerSuppressed,
                                      boolean creatureDeathTriggersSuppressed) {
        return addCardToGraveyard(gameData, ownerId, card, sourceZone, false,
                battlefieldControllerId, battlefieldSnapshot == null ? null : battlefieldSnapshot.getId(),
                battlefieldSnapshot, selfGraveyardTriggerSuppressed, false, creatureDeathTriggersSuppressed);
    }

    private boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone,
                                       boolean suppressLibraryCreatureCardsTrigger) {
        return addCardToGraveyard(gameData, ownerId, card, sourceZone,
                suppressLibraryCreatureCardsTrigger, null, null, null, false, false, false);
    }

    private boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone,
                                       boolean suppressLibraryCreatureCardsTrigger,
                                       UUID battlefieldControllerId) {
        return addCardToGraveyard(gameData, ownerId, card, sourceZone,
                suppressLibraryCreatureCardsTrigger, battlefieldControllerId, null, null, false, false, false);
    }

    private boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone,
                                       boolean suppressLibraryCreatureCardsTrigger,
                                       UUID battlefieldControllerId, UUID battlefieldPermanentId,
                                       Permanent battlefieldSnapshot, boolean selfGraveyardTriggerSuppressed) {
        return addCardToGraveyard(gameData, ownerId, card, sourceZone, suppressLibraryCreatureCardsTrigger,
                battlefieldControllerId, battlefieldPermanentId, battlefieldSnapshot,
                selfGraveyardTriggerSuppressed, false, false);
    }

    private boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone,
                                       boolean suppressLibraryCreatureCardsTrigger,
                                       UUID battlefieldControllerId, UUID battlefieldPermanentId,
                                       Permanent battlefieldSnapshot, boolean selfGraveyardTriggerSuppressed,
                                       boolean suppressLibraryMillTriggers,
                                       boolean creatureDeathTriggersSuppressed) {
        gameData.spellsWithDreamCounterOnResolution.remove(card.getId());
        gameData.spellsWithPlotOnResolution.remove(card.getId());
        // CR 614.7 — self-replacement effects apply first

        if (sourceZone == Zone.BATTLEFIELD && hasExileAndTakeExtraTurnReplacementEffect(card)) {
            UUID extraTurnControllerId = battlefieldControllerId != null ? battlefieldControllerId : ownerId;
            exileService.exileCard(gameData, ownerId, card);
            gameData.extraTurns.addFirst(extraTurnControllerId);
            gameData.extraTurnSkipsUntap.addFirst(false);
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is exiled instead of being put into a graveyard; its controller takes an extra turn."));
            log.info("Game {} - {} replacement effect: exiled and granted an extra turn to {}",
                    gameData.id, card.getName(), gameData.playerIdToName.get(extraTurnControllerId));
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
            return false;
        }

        // "If [this] would die, instead exile it with N egg counters" (e.g. Darigaaz Reincarnated)
        // "Die" = move from battlefield to graveyard, so only applies when sourceZone is BATTLEFIELD.
        if (sourceZone == Zone.BATTLEFIELD && hasExileWithEggCountersReplacementEffect(card)) {
            ExileWithEggCountersInsteadOfDyingEffect eggEffect = getExileWithEggCountersReplacementEffect(card);
            exileService.exileCard(gameData, ownerId, card);
            gameData.exiledCardEggCounters.put(card.getId(), eggEffect.count());
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is exiled with " + eggEffect.count() + " egg counters instead of dying."));
            log.info("Game {} - {} replacement effect: exiled with {} egg counters instead of dying",
                    gameData.id, card.getName(), eggEffect.count());
            return false;
        }

        // "If [this creature] would die, put it on top or bottom of its owner's library instead"
        // "Die" = move from battlefield to graveyard, so only applies when sourceZone is BATTLEFIELD.
        DyingCreatureLibraryReplacementEffect libraryReplacement = sourceZone == Zone.BATTLEFIELD
                ? getDyingCreatureLibraryReplacementEffect(card)
                : null;
        if (libraryReplacement != null) {
            List<Card> deck = gameData.playerDecks.get(ownerId);
            String position;
            String positionPhrase;
            if (libraryReplacement.putOnBottom()) {
                deck.add(card);
                position = "bottom";
                positionPhrase = "the bottom";
            } else {
                deck.add(0, card);
                position = "top";
                positionPhrase = "top";
            }
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is put on " + positionPhrase + " of its owner's library instead of dying."));
            log.info("Game {} - {} replacement effect: put on {} of library instead of dying",
                    gameData.id, card.getName(), position);
            return false;
        }

        if (hasShuffleIntoLibraryReplacementEffect(card)) {
            List<Card> deck = gameData.playerDecks.get(ownerId);
            deck.add(card);
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
            gameLogService.append(gameData, GameLog.cardThen(card, " is revealed and shuffled into its owner's library instead."));
            log.info("Game {} - {} replacement effect: shuffled into library instead of graveyard", gameData.id, card.getName());
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
            return false;
        }

        if (appliesExileInsteadOfGraveyard(card, sourceZone)) {
            exileService.exileCard(gameData, ownerId, card);
            gameLogService.append(gameData, GameLog.cardThen(card, " is exiled instead of being put into a graveyard."));
            log.info("Game {} - {} replacement effect: exiled instead of graveyard", gameData.id, card.getName());
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
            return false;
        }

        if (!card.isToken() && (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))) {
            if (anyPlayerHasInstantSorceryExileReplacement(gameData)) {
                exileService.exileCard(gameData, ownerId, card);
                gameLogService.append(gameData, GameLog.cardThen(card,
                        " is exiled instead of being put into a graveyard."));
                log.info("Game {} - {} replacement effect: instant or sorcery exiled instead of graveyard",
                        gameData.id, card.getName());
                updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
                return false;
            }
        }

        // Wheel of Sun and Moon — if the graveyard's owner is enchanted by a player aura with this
        // replacement, cards headed to their graveyard from anywhere are revealed and put on the
        // bottom of their library instead. Tokens are not cards, so they still hit the graveyard.
        if (!card.isToken() && enchantedPlayerHasBottomOfLibraryReplacement(gameData, ownerId)) {
            gameData.playerDecks.get(ownerId).add(card);
            gameLogService.append(gameData, GameLog.cardThen(card, " is revealed and put on the bottom of its owner's library instead."));
            log.info("Game {} - {} replacement effect: put on bottom of library instead of graveyard", gameData.id, card.getName());
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
            return false;
        }

        // Per-card "if that spell would be put into a graveyard, exile it instead" replacement
        // (e.g. a spell cast via Nita, Forum Conciliator). Tracked for the specific card until cleanup.
        if (gameData.exileInsteadOfGraveyard.remove(card.getId())) {
            exileService.exileCard(gameData, ownerId, card);
            
            gameLogService.append(gameData, GameLog.cardThen(card, " is exiled instead of being put into a graveyard."));
            log.info("Game {} - {} replacement effect: exiled instead of graveyard (cast permission)",
                    gameData.id, card.getName());
            return false;
        }

        if (!card.isToken() && gameData.playersExilingCardsInsteadOfGraveyardThisTurn.contains(ownerId)) {
            exileService.exileCard(gameData, ownerId, card);
            gameLogService.append(gameData, GameLog.cardThen(card, " is exiled instead of being put into a graveyard."));
            log.info("Game {} - {} replacement effect: exiled instead of graveyard (turn effect)",
                    gameData.id, card.getName());
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
            return false;
        }

        if (sourceZone == Zone.BATTLEFIELD
                && tryApplyBattlefieldExilePermanentsReplacement(gameData, ownerId, card, battlefieldSnapshot)) {
            return false;
        }

        // Leyline of the Void — if an opponent controls a permanent with
        // ExileOpponentCardsInsteadOfGraveyardEffect, exile the card instead
        if (opponentHasCreatureCardExileReplacement(gameData, ownerId, card, sourceZone)) {
            exileService.exileCard(gameData, ownerId, card);
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is exiled instead of being put into a graveyard."));
            log.info("Game {} - {} replacement effect: opponent creature card exiled instead of graveyard",
                    gameData.id, card.getName());
            return false;
        }

        if (opponentHasExileReplacementEffect(gameData, ownerId)) {
            exileService.exileCard(gameData, ownerId, card);
            
            gameLogService.append(gameData, GameLog.cardThen(card, " is exiled instead of being put into a graveyard."));
            log.info("Game {} - {} replacement effect: exiled instead of graveyard", gameData.id, card.getName());
            return false;
        }

        // Forbidden Crypt / Abandoned Sarcophagus — controller's own cards matching a replacement
        if (shouldExileOwnCardInsteadOfGraveyard(gameData, ownerId, card)) {
            exileService.exileCard(gameData, ownerId, card);
            gameLogService.append(gameData, GameLog.cardThen(card, " is exiled instead of being put into a graveyard."));
            log.info("Game {} - {} replacement effect: exiled instead of graveyard (own)", gameData.id, card.getName());
            return false;
        }

        gameData.playerGraveyards.get(ownerId).add(card);
        if (sourceZone == Zone.BATTLEFIELD && card.hasType(CardType.ARTIFACT)) {
            gameData.artifactsPutIntoGraveyardFromBattlefieldThisTurn++;
        }
        gameData.markGraveyardEntry(card);
        if (!card.isToken() && isPermanentCard(card)) {
            gameData.playersWhoDescendedThisTurn.add(ownerId);
            gameData.descentsThisTurn.merge(ownerId, 1, Integer::sum);
        }
        updateThisCombatGraveyardTracking(gameData, ownerId, card);
        updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, sourceZone,
                battlefieldSnapshot, creatureDeathTriggersSuppressed);
        updateFromAnywhereThisTurnTracking(gameData, ownerId, card);
        collectPutIntoGraveyardFromAnywhereTriggers(gameData, ownerId, card);
        collectEmblemPutIntoGraveyardTriggers(gameData, ownerId, card);
        collectOpponentGraveyardLifeLossTriggers(gameData, ownerId);
        if (sourceZone == Zone.BATTLEFIELD && !selfGraveyardTriggerSuppressed
                && !creatureDeathTriggersSuppressed) {
            collectPutIntoGraveyardFromBattlefieldTriggers(
                    gameData, ownerId, card, battlefieldPermanentId, battlefieldSnapshot);
        }
        if (!card.isToken() && isPermanentCard(card)) {
            triggerCollectionService.checkPermanentCardPutIntoGraveyardFromAnywhereTriggers(gameData, ownerId, card);
        }
        if (!card.isToken() && card.hasType(CardType.LAND)) {
            triggerCollectionService.checkLandPutIntoGraveyardFromAnywhereTriggers(gameData, ownerId, card);
            if (sourceZone == Zone.LIBRARY && !suppressLibraryMillTriggers) {
                triggerCollectionService.checkLandCardMilledTriggers(gameData, ownerId, card);
            }
        }
        if (!card.isToken()) {
            triggerCollectionService.checkCardPutIntoGraveyardFromAnywhereTriggers(gameData, ownerId, card);
        }
        if (!card.isToken() && card.hasType(CardType.CREATURE)) {
            triggerCollectionService.checkCreatureCardPutIntoGraveyardFromAnywhereTriggers(gameData, ownerId, card);
            if (sourceZone != Zone.BATTLEFIELD) {
                triggerCollectionService.checkCreatureCardPutIntoGraveyardFromNonBattlefieldTriggers(
                        gameData, ownerId, card);
            }
            if (sourceZone == Zone.LIBRARY) {
                triggerCollectionService.checkAnyCreatureCardPutIntoGraveyardFromLibraryTriggers(
                        gameData, ownerId, card);
                if (!suppressLibraryCreatureCardsTrigger) {
                    triggerCollectionService.checkCreatureCardsPutIntoGraveyardFromLibraryTriggers(
                            gameData, ownerId, 1);
                }
            }
        }
        if (!card.isToken()) {
            triggerCollectionService.checkCardPutIntoOpponentGraveyardFromAnywhereTriggers(gameData, ownerId, card);
        }
        triggerCollectionService.checkBlackCardPutIntoOpponentGraveyardFromAnywhereTriggers(gameData, ownerId, card);
        return true;
    }

    private boolean isPermanentCard(Card card) {
        return card.hasType(CardType.LAND)
                || card.hasType(CardType.CREATURE)
                || card.hasType(CardType.ENCHANTMENT)
                || card.hasType(CardType.ARTIFACT)
                || card.hasType(CardType.PLANESWALKER)
                || card.hasType(CardType.BATTLE);
    }

    /**
     * Exiles up to {@code count} cards from the given player's graveyard (from the front of the
     * list) and returns the number actually exiled. Used by prevention effects that exile a card
     * from the graveyard for each 1 damage prevented (Immortal Coil). Exiling from one's own
     * graveyard is a shortcut choice, so the front cards are taken deterministically.
     */
    public int exileCardsFromGraveyard(GameData gameData, UUID playerId, int count) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null || graveyard.isEmpty() || count <= 0) {
            return 0;
        }
        int toExile = Math.min(count, graveyard.size());
        List<Card> exiled = new ArrayList<>(graveyard.subList(0, toExile));
        graveyard.subList(0, toExile).clear();
        notifyCardsExiledFromGraveyard(gameData, playerId, exiled);
        for (Card card : exiled) {
            exileService.exileCard(gameData, playerId, card);
        }
        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " exiles " + toExile + " card" + (toExile != 1 ? "s" : "")
                + " from their graveyard.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} exiles {} cards from graveyard", gameData.id, playerName, toExile);
        return toExile;
    }

    /**
     * Exiles exactly {@code count} actual cards from the front of a player's graveyard when that
     * many cards are available. Tokens in the graveyard do not count and remain in place for the
     * engine's token-cleanup handling.
     */
    public boolean exileExactlyCardsFromGraveyard(GameData gameData, UUID playerId, int count) {
        if (count <= 0) return true;

        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) return false;

        List<Card> exiled = graveyard.stream()
                .filter(card -> !card.isToken())
                .limit(count)
                .toList();
        if (exiled.size() < count) return false;

        for (Card card : exiled) {
            graveyard.remove(card);
        }
        notifyCardsExiledFromGraveyard(gameData, playerId, exiled);
        for (Card card : exiled) {
            exileService.exileCard(gameData, playerId, card);
        }
        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " exiles " + count + " card" + (count != 1 ? "s" : "")
                + " from their graveyard.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} exiles {} cards from graveyard", gameData.id, playerName, count);
        return true;
    }

    /**
     * Fires "when this card is put into a graveyard from anywhere" triggered abilities
     * (EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, e.g. Purity). The card has already
     * entered the graveyard; the trigger goes on the stack under its owner's control.
     */
    private void collectPutIntoGraveyardFromAnywhereTriggers(GameData gameData, UUID ownerId, Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    ownerId,
                    card.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    (UUID) null
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            log.info("Game {} - {} triggers (put into graveyard from anywhere)", gameData.id, card.getName());
        }
    }

    /**
     * Fires emblem-borne "whenever a card is put into your graveyard from anywhere, you may return it
     * to your hand" triggers (Tamiyo, the Moon Sage's emblem). The trigger goes on the stack under the
     * graveyard owner's control with the newly-arrived card as its source, so the resolution-time
     * {@code MayEffect} prompt and {@code ReturnSourceCardFromGraveyardToOwnerHandEffect} both act on
     * that exact card. Tokens are skipped: they cease to exist and can never be returned.
     */
    private void collectEmblemPutIntoGraveyardTriggers(GameData gameData, UUID ownerId, Card card) {
        if (card.isToken()) {
            return;
        }
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            if (!emblem.controllerId().equals(ownerId)) {
                continue;
            }
            for (CardEffect emblemEffect : emblem.staticEffects()) {
                if (!(emblemEffect instanceof ReturnCardPutIntoGraveyardToHandEffect)) {
                    continue;
                }
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        ownerId,
                        emblem.sourceCard().getName() + "'s emblem",
                        new ArrayList<>(List.of(new MayEffect(
                                new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                                "Return " + card.getName() + " to your hand?"))),
                        null,
                        (UUID) null
                ));
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(ownerId)
                        + "'s emblem triggers."));
                log.info("Game {} - emblem graveyard-return trigger for {}", gameData.id, card.getName());
            }
        }
    }

    /**
     * Fires the turn-scoped "whenever a card is put into an opponent's graveyard from anywhere this
     * turn, that player loses 1 life" delayed triggers (Duskmantle Guildmage). One trigger per active
     * watcher whose controller is an opponent of the graveyard's owner, so repeated activations
     * stack. Tokens count too — they are cards put into a graveyard before they cease to exist.
     */
    private void collectOpponentGraveyardLifeLossTriggers(GameData gameData, UUID ownerId) {
        for (OpponentGraveyardLifeLossWatcher watcher : List.copyOf(gameData.opponentGraveyardLifeLossWatchers)) {
            if (watcher.controllerId().equals(ownerId)) {
                continue;
            }
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER))),
                    ownerId,
                    (UUID) null
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers (card put into an opponent's graveyard)",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /**
     * Fires "when this card is put into a graveyard from the battlefield" triggered abilities
     * (EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, e.g. Spreading Algae). Only called when
     * the source zone is the battlefield. The card has already entered the graveyard; the trigger goes
     * on the stack under its owner's control.
     */
    private void collectPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID ownerId, Card card,
                                                                 UUID battlefieldPermanentId,
                                                                 Permanent battlefieldSnapshot) {
        for (CardEffect effect : card.getEffects(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)) {
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        card, ownerId, new ArrayList<>(List.of(effect))));
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    ownerId,
                    card.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    battlefieldPermanentId
            );
            if (battlefieldSnapshot != null) {
                entry.setSourcePermanentSnapshot(new Permanent(battlefieldSnapshot));
            }
            gameData.stack.add(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            log.info("Game {} - {} triggers (put into graveyard from battlefield)", gameData.id, card.getName());
        }
    }


    public boolean tryRegenerate(GameData gameData, Permanent perm) {
        return tryReplaceDestruction(gameData, perm, true, false);
    }

    public boolean tryReplaceDestruction(GameData gameData, Permanent perm, boolean allowRegeneration) {
        return tryReplaceDestruction(gameData, perm, allowRegeneration, true);
    }

    private boolean tryReplaceDestruction(GameData gameData, Permanent perm, boolean allowRegeneration,
                                          boolean allowShieldCounter) {
        Permanent umbraArmor = findDestructionReplacementSource(gameData, perm, DestructionReplacement.UMBRA_ARMOR);
        if (umbraArmor != null) {
            performUmbraArmorReplacement(gameData, perm, umbraArmor);
            return true;
        }
        if (allowShieldCounter && perm.getCounterCount(CounterType.SHIELD) > 0) {
            int shields = perm.getCounterCount(CounterType.SHIELD);
            perm.setCounterCount(CounterType.SHIELD, shields - 1);
            gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), " loses a shield counter instead of being destroyed."));
            log.info("Game {} - {} loses a shield counter instead of being destroyed", gameData.id, perm.getCard().getName());
            return true;
        }
        if (!allowRegeneration || perm.isCantRegenerateThisTurn()
                || damagedByRegenerationDenyingSource(gameData, perm)) {
            return false;
        }
        Permanent intrinsicRegen = findDestructionReplacementSource(gameData, perm, DestructionReplacement.REGENERATE);
        if (intrinsicRegen != null) {
            performRegeneration(gameData, perm);
            return true;
        }
        if (perm.getRegenerationShield() > 0) {
            perm.setRegenerationShield(perm.getRegenerationShield() - 1);
            performRegeneration(gameData, perm);
            spendOpponentDrawRegenerationShield(gameData, perm);
            spendMinusOneCounterRegenerationShield(gameData, perm);
            spendPlusOnePlusOneCounterRegenerationShield(gameData, perm);
            spendGainControlRegenerationShield(gameData, perm);
            return true;
        }
        return false;
    }

    private Permanent findDestructionReplacementSource(GameData gameData, Permanent destroyedPermanent,
                                                       DestructionReplacement replacement) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent source : battlefield) {
                boolean applies = source.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(DestructionReplacementEffect.class::isInstance)
                        .map(DestructionReplacementEffect.class::cast)
                        .anyMatch(effect -> effect.replacement() == replacement
                                && effect.appliesTo(source, destroyedPermanent));
                if (!applies) {
                    applies = gameQueryService.computeStaticBonus(gameData, source).grantedEffects().stream()
                            .filter(DestructionReplacementEffect.class::isInstance)
                            .map(DestructionReplacementEffect.class::cast)
                            .anyMatch(effect -> effect.replacement() == replacement
                                    && effect.appliesTo(source, destroyedPermanent));
                }
                if (applies) {
                    return source;
                }
            }
        }
        return null;
    }

    private void performUmbraArmorReplacement(GameData gameData, Permanent protectedPermanent, Permanent aura) {
        protectedPermanent.setMarkedDamage(0);
        protectedPermanent.setDamagedByDeathtouch(false);
        permanentRemovalService.tryDestroyPermanent(gameData, aura);
    }

    /**
     * True when some permanent on the battlefield currently has "creatures dealt damage by this creature
     * this turn can't be regenerated this turn" (Bone Shaman) and damaged this creature this turn.
     */
    private boolean damagedByRegenerationDenyingSource(GameData gameData, Permanent perm) {
        UUID cardId = perm.getCard().getId();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent source : battlefield) {
                if (source.isDamagedCreaturesCantRegenerateThisTurn()
                        && gameData.creatureCardsDamagedThisTurnBySourcePermanent
                        .getOrDefault(source.getId(), Set.of()).contains(cardId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Soldevi Sentry: a regeneration shield granted by its ability owes the opponent an optional draw
     * once it is actually spent. Plain shields are consumed first — a rider shield is only spent when
     * the remaining shields are all rider shields.
     */
    private void spendOpponentDrawRegenerationShield(GameData gameData, Permanent perm) {
        if (perm.getOpponentDrawRegenerationShield() <= perm.getRegenerationShield()) {
            return;
        }
        perm.setOpponentDrawRegenerationShield(perm.getOpponentDrawRegenerationShield() - 1);

        UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
        UUID opponentId = controllerId == null ? null : gameQueryService.getOpponentId(gameData, controllerId);
        if (opponentId == null) {
            return;
        }

        gameData.queueMayAbility(perm.getCard(), opponentId, new MayEffect(new DrawCardEffect(), "Draw a card?"));
        gameLogService.append(gameData, GameLog.textCardText(
                "", perm.getCard(), " offers " + gameData.playerIdToName.get(opponentId) + " a card."));
    }

    /**
     * Matopi Golem: a regeneration shield granted by its ability puts a -1/-1 counter on the
     * permanent once it is actually spent. Plain shields are consumed first — a rider shield is
     * only spent when the remaining shields are all rider shields.
     */
    private void spendMinusOneCounterRegenerationShield(GameData gameData, Permanent perm) {
        if (perm.getMinusOneCounterRegenerationShield() <= perm.getRegenerationShield()) {
            return;
        }
        perm.setMinusOneCounterRegenerationShield(perm.getMinusOneCounterRegenerationShield() - 1);

        if (gameQueryService.cantHaveCounters(gameData, perm)
                || gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, perm)) {
            return;
        }
        int amount = gameQueryService.reduceMinusOneMinusOneCounters(gameData, perm, 1);
        if (amount <= 0) {
            return;
        }
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE,
                perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + amount);
        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), " gets a -1/-1 counter."));
        log.info("Game {} - {} gets a -1/-1 counter (Matopi Golem regenerate rider)",
                gameData.id, perm.getCard().getName());

        UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
        permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(
                gameData, perm, amount, controllerId);
    }

    /**
     * Skeleton Scavengers: a regeneration shield granted by its ability puts a +1/+1 counter on the
     * permanent once it is actually spent. Plain shields are consumed first — a rider shield is only
     * spent once it is all that is left.
     */
    private void spendPlusOnePlusOneCounterRegenerationShield(GameData gameData, Permanent perm) {
        if (perm.getPlusOnePlusOneCounterRegenerationShield() <= perm.getRegenerationShield()) {
            return;
        }
        perm.setPlusOnePlusOneCounterRegenerationShield(perm.getPlusOnePlusOneCounterRegenerationShield() - 1);

        permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, null, perm, 1);
    }

    /**
     * Debt of Loyalty: a regeneration shield granted by that spell hands its controller control of the
     * creature once the shield is actually spent. Plain shields are consumed first, so a rider shield is
     * only spent when the remaining shields are all rider shields.
     */
    private void spendGainControlRegenerationShield(GameData gameData, Permanent perm) {
        List<UUID> shields = perm.getGainControlRegenerationShields();
        if (shields.size() <= perm.getRegenerationShield()) {
            return;
        }
        UUID newControllerId = shields.remove(shields.size() - 1);
        if (newControllerId == null) {
            return;
        }
        // Queued rather than applied here: regeneration usually happens inside the state-based-action
        // sweep, which is iterating the battlefield lists this would move the permanent between.
        gameData.pendingRegenerationControlChanges.put(perm.getId(), newControllerId);
        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                " comes under " + gameData.playerIdToName.get(newControllerId) + "'s control."));
    }

    private void performRegeneration(GameData gameData, Permanent perm) {
        perm.setTimesRegeneratedThisTurn(perm.getTimesRegeneratedThisTurn() + 1);
        perm.tap();
        triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
        perm.setAttacking(false);
        perm.setBlocking(false);
        perm.getBlockingTargets().clear();
        // CR 701.15a — regeneration removes all damage marked on the permanent
        perm.setMarkedDamage(0);
        perm.setDamagedByDeathtouch(false);

        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), " regenerates."));
        log.info("Game {} - {} regenerates", gameData.id, perm.getCard().getName());
    }


    public void recordCreatureDamagedByPermanent(GameData gameData, UUID sourcePermanentId, Permanent damagedCreature, int damage) {
        if (sourcePermanentId == null || damagedCreature == null || damage <= 0) {
            return;
        }
        if (!gameQueryService.isCreature(gameData, damagedCreature)) {
            return;
        }

        gameData.creatureCardsDamagedThisTurnBySourcePermanent
                .computeIfAbsent(sourcePermanentId, ignored -> ConcurrentHashMap.newKeySet())
                .add(damagedCreature.getCard().getId());
    }


    private boolean hasExileWithEggCountersReplacementEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof ExileWithEggCountersInsteadOfDyingEffect);
    }

    private ExileWithEggCountersInsteadOfDyingEffect getExileWithEggCountersReplacementEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof ExileWithEggCountersInsteadOfDyingEffect)
                .map(e -> (ExileWithEggCountersInsteadOfDyingEffect) e)
                .findFirst()
                .orElseThrow();
    }

    private DyingCreatureLibraryReplacementEffect getDyingCreatureLibraryReplacementEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .filter(DyingCreatureLibraryReplacementEffect.class::isInstance)
                .map(DyingCreatureLibraryReplacementEffect.class::cast)
                .filter(effect -> !effect.mayChoose())
                .findFirst()
                .orElse(null);
    }

    private boolean hasShuffleIntoLibraryReplacementEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof ShuffleIntoLibraryReplacementEffect);
    }

    private boolean hasExileAndTakeExtraTurnReplacementEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(ExileAndTakeExtraTurnReplacementEffect.class::isInstance);
    }

    /**
     * True when the card's own exile-instead replacement applies to a move from the
     * battlefield to a graveyard. Every variant covers dying, so no zone check is needed.
     */
    public static boolean hasExileInsteadOfGraveyardReplacementEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof ExileInsteadOfGraveyardReplacementEffect);
    }

    /**
     * True when the card's exile-instead replacement applies to a move from {@code sourceZone}.
     * A {@code dyingOnly} variant ("if this creature would die, exile it instead") applies only
     * from the battlefield; the plain variant applies from anywhere.
     */
    private static boolean appliesExileInsteadOfGraveyard(Card card, Zone sourceZone) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .filter(ExileInsteadOfGraveyardReplacementEffect.class::isInstance)
                .map(ExileInsteadOfGraveyardReplacementEffect.class::cast)
                .anyMatch(e -> !e.dyingOnly() || sourceZone == Zone.BATTLEFIELD);
    }

    public static boolean hasExilePermanentsInsteadOfGraveyardReplacementEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof ExilePermanentsInsteadOfGraveyardEffect replacement
                        && replacement.filter() == null && !replacement.excludeSource());
    }

    private boolean enchantedPlayerHasBottomOfLibraryReplacement(GameData gameData, UUID ownerId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.isAttached() && ownerId.equals(p.getAttachedTo())
                        && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(RevealAndPutOnBottomOfLibraryInsteadOfGraveyardEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentHasExileReplacementEffect(GameData gameData, UUID ownerId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ownerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(ExileOpponentCardsInsteadOfGraveyardEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentHasCreatureCardExileReplacement(GameData gameData, UUID ownerId,
                                                            Card card, Zone sourceZone) {
        if (sourceZone == Zone.BATTLEFIELD || card.isToken() || !card.hasType(CardType.CREATURE)) {
            return false;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ownerId)) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(OpponentCreatureCardExileReplacement.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean anyPlayerHasInstantSorceryExileReplacement(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(ExileInstantSorceryCardsInsteadOfGraveyardEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryApplyBattlefieldExilePermanentsReplacement(GameData gameData, UUID ownerId, Card card,
                                                                   Permanent battlefieldSnapshot) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof ExilePermanentsInsteadOfGraveyardEffect replacement)) {
                        continue;
                    }
                    if (replacement.excludeSource() && battlefieldSnapshot != null
                            && p.getId().equals(battlefieldSnapshot.getId())) {
                        continue;
                    }
                    PermanentPredicate filter = replacement.filter();
                    if (filter != null && (battlefieldSnapshot == null
                            || !predicateEvaluationService.matchesPermanentPredicate(
                            gameData, battlefieldSnapshot, filter))) {
                        continue;
                    }
                    if (replacement.trackWithSource()) {
                        exileService.exileCard(gameData, ownerId, card, p.getId());
                        gameLogService.append(gameData, GameLog.textCardText(
                                card.getName() + " is exiled with ", p.getCard(),
                                " instead of being put into a graveyard."));
                        log.info("Game {} - {} is exiled with {} instead of graveyard",
                                gameData.id, card.getName(), p.getCard().getName());
                    } else {
                        exileService.exileCard(gameData, ownerId, card);
                        gameLogService.append(gameData, GameLog.cardThen(card,
                                " is exiled instead of being put into a graveyard."));
                        log.info("Game {} - {} replacement effect: permanent exiled instead of graveyard",
                                gameData.id, card.getName());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldExileOwnCardInsteadOfGraveyard(GameData gameData, UUID ownerId, Card card) {
        List<Permanent> bf = gameData.playerBattlefields.get(ownerId);
        if (bf == null) {
            return false;
        }
        boolean beingCycled = card.getId().equals(gameData.cardEnteringGraveyardByCycling);
        for (Permanent p : bf) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (!(effect instanceof OwnGraveyardExileReplacement replacement)) {
                    continue;
                }
                if (card.isToken() && !replacement.appliesToTokens()) {
                    continue;
                }
                if (replacement.exemptWhenCycled() && beingCycled) {
                    continue;
                }
                if (replacement.filter() != null
                        && !predicateEvaluationService.matchesCardPredicate(card, replacement.filter(), null)) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private void updateFromAnywhereThisTurnTracking(GameData gameData, UUID ownerId, Card card) {
        if (!card.isToken()) {
            gameData.cardsPutIntoGraveyardFromAnywhereThisTurn
                    .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(card.getId());
            if (card.hasType(CardType.CREATURE)) {
                gameData.creatureCardsPutIntoGraveyardFromAnywhereThisTurn
                        .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet())
                        .add(card.getId());
            }
        }
    }

    private void updateThisCombatGraveyardTracking(GameData gameData, UUID ownerId, Card card) {
        if (gameData.currentStep != null && gameData.currentStep.isCombatPhase() && !card.isToken()) {
            gameData.cardsPutIntoGraveyardThisCombat
                    .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(card.getId());
        }
    }

    private void updateThisTurnBattlefieldToGraveyardTracking(GameData gameData, UUID ownerId, Card card,
                                                              Zone sourceZone) {
        updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, sourceZone, null, false);
    }

    private void updateThisTurnBattlefieldToGraveyardTracking(GameData gameData, UUID ownerId, Card card,
                                                              Zone sourceZone,
                                                              Permanent battlefieldSnapshot,
                                                              boolean creatureDeathTriggersSuppressed) {
        if (sourceZone == Zone.BATTLEFIELD) {
            gameData.permanentPutIntoGraveyardFromBattlefieldThisTurn = true;
        }
        if (sourceZone == Zone.BATTLEFIELD
                && (card.hasType(CardType.ARTIFACT) || card.hasType(CardType.CREATURE))) {
            gameData.artifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn = true;
        }
        Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn
                .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet());
        // Tracks all non-token cards (any type) put into the graveyard from the battlefield this turn.
        Set<UUID> allTracked = gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn
                .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet());
        if (sourceZone == Zone.BATTLEFIELD && !card.isToken()) {
            allTracked.add(card.getId());
            if (card.hasType(CardType.CREATURE)) {
                tracked.add(card.getId());
                if (!creatureDeathTriggersSuppressed) {
                    triggerDamagedCreatureDiesAbilities(gameData, card, ownerId, battlefieldSnapshot);
                }
            } else {
                tracked.remove(card.getId());
            }
        } else {
            tracked.remove(card.getId());
            allTracked.remove(card.getId());
        }
    }

    private void triggerDamagedCreatureDiesAbilities(GameData gameData, Card dyingCreatureCard, UUID ownerId,
                                                     Permanent battlefieldSnapshot) {
        if (dyingCreatureCard == null) {
            return;
        }

        UUID dyingCreatureCardId = dyingCreatureCard.getId();
        Permanent dyingPermanent = battlefieldSnapshot;
        if (dyingPermanent == null) {
            dyingPermanent = gameData.simultaneousDyingCreatures.values().stream()
                    .filter(permanent -> permanent.getCard() != null
                            && dyingCreatureCardId.equals(permanent.getCard().getId()))
                    .findFirst()
                    .orElse(null);
        }
        UUID dyingControllerId = findLastKnownController(gameData, dyingCreatureCardId, ownerId);

        for (Map.Entry<UUID, Set<UUID>> entry : gameData.creatureCardsDamagedThisTurnBySourcePermanent.entrySet()) {
            UUID sourcePermanentId = entry.getKey();
            Set<UUID> damagedCreatureIds = entry.getValue();
            if (!damagedCreatureIds.contains(dyingCreatureCardId)) {
                continue;
            }

            // Krovikan Vampire end-step intervening-if / return tracking (survives ON_DAMAGED_CREATURE_DIES
            // cleanup below). Returnable card ids are pruned when the card leaves a graveyard.
            gameData.sourcesWhoseDamagedCreaturesDiedThisTurn.add(sourcePermanentId);
            gameData.creatureCardsDamagedBySourceThatDiedThisTurn
                    .computeIfAbsent(sourcePermanentId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(dyingCreatureCardId);

            Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
            if (source == null) {
                source = gameData.simultaneousDyingCreatures.get(sourcePermanentId);
            }
            if (source == null) {
                continue;
            }

            UUID controllerId = findPermanentController(gameData, sourcePermanentId);
            if (controllerId == null) {
                controllerId = gameData.simultaneousDyingControllers.get(sourcePermanentId);
            }
            if (controllerId != null) {
                // The damaging permanent's own ON_DAMAGED_CREATURE_DIES abilities (e.g. Seraph, Vein Drinker).
                enqueueDamagedCreatureDiesTriggers(gameData, dyingCreatureCard, dyingControllerId, source,
                        controllerId, sourcePermanentId, dyingPermanent);
            }

            // Equipment attached to the damaging creature carries the ability keyed off the creature it
            // is CURRENTLY attached to (e.g. Unscythe, Killer of Kings). Per the rulings, only the
            // currently-equipped creature is checked — even if the Equipment wasn't attached when the
            // damage was dealt — so we look at what is attached to the source right now. The Equipment's
            // own controller (not the creature's) is "you", since an Equipment may be attached to a
            // creature another player controls.
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent equipment : battlefield) {
                    if (!equipment.isAttached() || !sourcePermanentId.equals(equipment.getAttachedTo())) {
                        continue;
                    }
                    enqueueDamagedCreatureDiesTriggers(gameData, dyingCreatureCard, dyingControllerId, equipment,
                            playerId, equipment.getId(), dyingPermanent);
                }
            }
        }

        for (Set<UUID> damagedCreatureIds : gameData.creatureCardsDamagedThisTurnBySourcePermanent.values()) {
            damagedCreatureIds.remove(dyingCreatureCardId);
        }
    }

    /**
     * Puts each {@code ON_DAMAGED_CREATURE_DIES} ability of {@code sourcePermanent} onto the stack for
     * the given controller, materialising any last-known-information the effect needs about the dying
     * creature (its toughness, its card id for a "may exile that card" ability, or its controller for
     * "its controller loses 2 life"). Abilities granted only until end of turn (Touch of Moonglove)
     * live on the permanent, not the card, so both sources are collected.
     */
    private void enqueueDamagedCreatureDiesTriggers(GameData gameData, Card dyingCreatureCard,
                                                    UUID dyingControllerId, Permanent sourcePermanent,
                                                    UUID controllerId, UUID sourcePermanentId,
                                                    Permanent dyingPermanent) {
        Card sourceCard = sourcePermanent.getCard();
        List<CardEffect> cardEffects = sourceCard.getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES);
        List<CardEffect> effects = new ArrayList<>(cardEffects == null ? List.of() : cardEffects);
        effects.addAll(sourcePermanent.getTemporaryTriggeredEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES));
        effects.addAll(triggerCollectionService.grantedTriggeredEffects(
                gameData, sourcePermanent, EffectSlot.ON_DAMAGED_CREATURE_DIES));
        if (effects.isEmpty()) {
            return;
        }
        UUID dyingCreatureCardId = dyingCreatureCard.getId();
        for (CardEffect effect : effects) {
            if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                if (dyingPermanent == null || !predicateEvaluationService.matchesPermanentPredicate(
                        dyingPermanent,
                        conditional.predicate(),
                        FilterContext.of(gameData)
                                .withSourceCardId(sourceCard.getId())
                                .withSourceControllerId(controllerId)
                                .withSourcePermanentId(sourcePermanentId)
                                .withSourcePermanentSnapshot(sourcePermanent))) {
                    continue;
                }
                effect = conditional.wrapped();
            }
            CardEffect materializedEffect = materializeDamagedCreatureDiesEffect(effect, dyingCreatureCard);
            if (materializedEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || materializedEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || materializedEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        sourceCard, controllerId, List.of(materializedEffect),
                        "damaged creature dies", sourcePermanentId));
                continue;
            }
            StackEntry triggerEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    controllerId,
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(materializedEffect)),
                    null,
                    sourcePermanentId
            );
            // The dying creature's card id as last-known information, for effects that act on it
            // (e.g. Seraph returns "that card" at the next end step).
            triggerEntry.setTriggeringCardId(dyingCreatureCardId);
            // "its controller loses N life" reads the dying creature's last-known controller from
            // the entry's targetId; the ability itself chooses no target. Other abilities in this
            // slot use targetId for their own purposes, so it is only bound for that recipient.
            if (materializedEffect instanceof LoseLifeEffect lose
                    && lose.recipient() == LoseLifeRecipient.DYING_CREATURE_CONTROLLER) {
                triggerEntry.setTargetId(dyingControllerId);
            }
            gameData.stack.add(triggerEntry);
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers (damaged creature died this turn)", gameData.id, sourceCard.getName());
        }
    }

    /**
     * Binds the dying creature's last-known information into effects that need it. The "you may exile
     * that card" ability (Unscythe) loses the stack entry's triggering-card id once wrapped as a may
     * ability, so the dying card id is bound onto the wrapped effect here instead.
     */
    private CardEffect materializeDamagedCreatureDiesEffect(CardEffect effect, Card dyingCreatureCard) {
        if (effect instanceof GainLifeEqualToToughnessEffect) {
            return new GainLifeEffect(dyingCreatureCard.getToughness());
        }
        if (effect instanceof MayEffect may && may.wrapped() instanceof DyingCreatureCardAwareEffect aware) {
            return new MayEffect(aware.boundToDyingCard(dyingCreatureCard.getId()), may.prompt());
        }
        if (effect instanceof DyingCreatureCardAwareEffect aware) {
            return aware.boundToDyingCard(dyingCreatureCard.getId());
        }
        return effect;
    }

    /**
     * Last-known controller of a creature card that just left the battlefield. The permanent is gone
     * by now, so the simultaneous-death batch (populated by the SBA lethal pass and the destroy
     * pipeline) is consulted first; it is the only place a stolen creature's controller survives.
     * Falls back to the owner, who controlled the permanent in every ordinary case.
     */
    private UUID findLastKnownController(GameData gameData, UUID dyingCreatureCardId, UUID ownerId) {
        for (Permanent dying : gameData.simultaneousDyingCreatures.values()) {
            if (dying.getCard() != null && dyingCreatureCardId.equals(dying.getCard().getId())) {
                UUID controllerId = gameData.simultaneousDyingControllers.get(dying.getId());
                if (controllerId != null) {
                    return controllerId;
                }
            }
        }
        return ownerId;
    }

    private UUID findPermanentController(GameData gameData, UUID permanentId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getId().equals(permanentId)) {
                    return playerId;
                }
            }
        }
        return null;
    }


    /**
     * Begins a batch of graveyard removals that should produce a single
     * "one or more cards leave your graveyard" trigger event.
     */
    public void beginGraveyardLeaveBatch(GameData gameData) {
        gameData.graveyardLeaveNotificationDepth++;
    }

    /**
     * Ends a graveyard-leave batch and fires any deferred triggers.
     */
    public void endGraveyardLeaveBatch(GameData gameData) {
        if (gameData.graveyardLeaveNotificationDepth <= 0) {
            return;
        }
        gameData.graveyardLeaveNotificationDepth--;
        if (gameData.graveyardLeaveNotificationDepth == 0) {
            for (UUID ownerId : gameData.graveyardLeaveNotificationPendingOwners) {
                triggerCollectionService.checkControllerCardsLeaveGraveyardTriggers(gameData, ownerId);
            }
            for (var pending : gameData.graveyardExileNotificationPendingCounts.entrySet()) {
                triggerCollectionService.checkControllerCardsExiledFromGraveyardTriggers(
                        gameData, pending.getKey(), pending.getValue());
            }
            for (var pending : gameData.kayaExileNotificationPendingCounts.entrySet()) {
                triggerCollectionService.checkControllerCreaturesOrCreatureCardsExiledTriggers(
                        gameData, pending.getKey(), pending.getValue(),
                        gameData.kayaExileNotificationPendingCreatureCards
                                .getOrDefault(pending.getKey(), List.of()));
            }
            if (gameData.graveyardOrBattlefieldExileNotificationPending) {
                triggerCollectionService.checkCardsExiledFromGraveyardsOrBattlefieldDuringYourTurnTriggers(
                        gameData, 1);
            }
            for (UUID ownerId : gameData.graveyardLeaveNotificationPendingCreatureOwners) {
                triggerCollectionService.checkControllerCreatureCardsLeaveGraveyardTriggers(gameData, ownerId);
            }
            for (var pending : gameData.graveyardLeaveNotificationPendingCreatureCardCounts.entrySet()) {
                for (int i = 0; i < pending.getValue(); i++) {
                    triggerCollectionService.checkControllerCreatureCardLeavesGraveyardTriggers(
                            gameData, pending.getKey());
                }
            }
            for (UUID ownerId : gameData.graveyardLeaveNotificationPendingArtifactOrCreatureOwners) {
                triggerCollectionService.checkControllerArtifactOrCreatureCardsLeaveGraveyardTriggers(gameData, ownerId);
            }
            gameData.graveyardLeaveNotificationPendingOwners.clear();
            gameData.graveyardExileNotificationPendingCounts.clear();
            gameData.kayaExileNotificationPendingCreatureCards.clear();
            gameData.kayaExileNotificationPendingCounts.clear();
            gameData.graveyardOrBattlefieldExileNotificationPending = false;
            gameData.graveyardLeaveNotificationPendingCreatureOwners.clear();
            gameData.graveyardLeaveNotificationPendingCreatureCardCounts.clear();
            gameData.graveyardLeaveNotificationPendingArtifactOrCreatureOwners.clear();
        }
    }

    /**
     * Notifies that one or more cards left the given player's graveyard.
     * When inside a batch ({@link #beginGraveyardLeaveBatch}), defers until the batch ends.
     */
    public void notifyCardsLeftGraveyard(GameData gameData, UUID ownerId) {
        // Record that one or more cards left this player's graveyard this turn (regardless of
        // batching), for "if one or more cards left your graveyard this turn" effects.
        gameData.playersWhoseCardsLeftGraveyardThisTurn.add(ownerId);
        // Krovikan Vampire: a card that leaves the graveyard is no longer returnable even if it
        // re-enters the graveyard later this turn (loses track).
        pruneDamagedCreatureDiedTrackingNotInGraveyard(gameData);
        if (gameData.graveyardLeaveNotificationDepth > 0) {
            gameData.graveyardLeaveNotificationPendingOwners.add(ownerId);
            return;
        }
        triggerCollectionService.checkControllerCardsLeaveGraveyardTriggers(gameData, ownerId);
    }

    public void notifyCardsLeftGraveyard(GameData gameData, UUID ownerId, Card leavingCard) {
        if (leavingCard != null) {
            gameData.oncePerTurnTriggersFiredThisTurn.remove(leavingCard.getId());
        }
        notifyCardsLeftGraveyard(gameData, ownerId);
        if (leavingCard != null && !leavingCard.isToken() && leavingCard.hasType(CardType.CREATURE)) {
            notifyCreatureCardsLeftGraveyard(gameData, ownerId, 1);
        }
        if (isArtifactOrCreatureCard(leavingCard)) {
            notifyArtifactOrCreatureCardsLeftGraveyard(gameData, ownerId);
        }
    }

    public void notifyCardsLeftGraveyard(GameData gameData, UUID ownerId, List<Card> leavingCards) {
        if (leavingCards == null || leavingCards.isEmpty()) {
            return;
        }
        leavingCards.forEach(card -> gameData.oncePerTurnTriggersFiredThisTurn.remove(card.getId()));
        notifyCardsLeftGraveyard(gameData, ownerId);
        int creatureCardCount = (int) leavingCards.stream()
                .filter(card -> !card.isToken() && card.hasType(CardType.CREATURE))
                .count();
        if (creatureCardCount > 0) {
            notifyCreatureCardsLeftGraveyard(gameData, ownerId, creatureCardCount);
        }
        if (leavingCards.stream().anyMatch(this::isArtifactOrCreatureCard)) {
            notifyArtifactOrCreatureCardsLeftGraveyard(gameData, ownerId);
        }
    }

    /** Notifies the graveyard departure watchers that the cards left by this event were exiled. */
    public void notifyCardsExiledFromGraveyard(GameData gameData, UUID ownerId, Card exiledCard) {
        notifyCardsLeftGraveyard(gameData, ownerId, exiledCard);
        notifyCardsExiledFromGraveyard(gameData, ownerId, 1,
                exiledCard != null && !exiledCard.isToken()
                        && exiledCard.hasType(CardType.CREATURE) ? List.of(exiledCard) : List.of());
    }

    /** Notifies the graveyard departure watchers that the cards left by this event were exiled. */
    public void notifyCardsExiledFromGraveyard(GameData gameData, UUID ownerId, List<Card> exiledCards) {
        if (exiledCards == null || exiledCards.isEmpty()) {
            return;
        }
        notifyCardsLeftGraveyard(gameData, ownerId, exiledCards);
        List<Card> creatureCards = exiledCards.stream()
                .filter(card -> card != null && !card.isToken() && card.hasType(CardType.CREATURE))
                .toList();
        notifyCardsExiledFromGraveyard(gameData, ownerId, exiledCards.size(), creatureCards);
    }

    private void notifyCardsExiledFromGraveyard(GameData gameData, UUID ownerId, int count) {
        notifyCardsExiledFromGraveyard(gameData, ownerId, count, List.of());
    }

    private void notifyCardsExiledFromGraveyard(GameData gameData, UUID ownerId, int count,
                                                List<Card> creatureCards) {
        if (gameData.graveyardLeaveNotificationDepth > 0) {
            gameData.graveyardExileNotificationPendingCounts.merge(ownerId, count, Integer::sum);
            if (!creatureCards.isEmpty()) {
                gameData.kayaExileNotificationPendingCounts.merge(
                        ownerId, creatureCards.size(), Integer::sum);
                gameData.kayaExileNotificationPendingCreatureCards
                        .computeIfAbsent(ownerId, ignored -> new ArrayList<>())
                        .addAll(creatureCards);
            }
            gameData.graveyardOrBattlefieldExileNotificationPending = true;
            return;
        }
        triggerCollectionService.checkControllerCardsExiledFromGraveyardTriggers(gameData, ownerId, count);
        triggerCollectionService.checkCardsExiledFromGraveyardsOrBattlefieldDuringYourTurnTriggers(gameData, count);
        triggerCollectionService.checkControllerCreaturesOrCreatureCardsExiledTriggers(
                gameData, ownerId, creatureCards.size(), creatureCards);
    }

    /** Notifies the event watcher that one or more cards were exiled from the battlefield. */
    public void notifyCardsExiledFromBattlefield(GameData gameData, int count) {
        if (count <= 0) return;
        if (gameData.graveyardLeaveNotificationDepth > 0) {
            gameData.graveyardOrBattlefieldExileNotificationPending = true;
            return;
        }
        triggerCollectionService.checkCardsExiledFromGraveyardsOrBattlefieldDuringYourTurnTriggers(gameData, count);
    }

    /** Notifies the creature-specific exile watcher in addition to the ordinary exile watchers. */
    public void notifyCardsExiledFromBattlefield(GameData gameData, int count, UUID controllerId,
                                                 boolean creatureExiled, List<Card> creatureCards) {
        notifyCardsExiledFromBattlefield(gameData, count);
        if (!creatureExiled || controllerId == null) return;
        if (gameData.graveyardLeaveNotificationDepth > 0) {
            gameData.kayaExileNotificationPendingCounts.merge(controllerId, 1, Integer::sum);
            if (!creatureCards.isEmpty()) {
                gameData.kayaExileNotificationPendingCreatureCards
                        .computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                        .addAll(creatureCards);
            }
            return;
        }
        triggerCollectionService.checkControllerCreaturesOrCreatureCardsExiledTriggers(
                gameData, controllerId, 1, creatureCards);
    }

    private void notifyCreatureCardsLeftGraveyard(GameData gameData, UUID ownerId, int count) {
        if (gameData.graveyardLeaveNotificationDepth > 0) {
            gameData.graveyardLeaveNotificationPendingCreatureOwners.add(ownerId);
            gameData.graveyardLeaveNotificationPendingCreatureCardCounts.merge(ownerId, count, Integer::sum);
            return;
        }
        triggerCollectionService.checkControllerCreatureCardsLeaveGraveyardTriggers(gameData, ownerId);
        for (int i = 0; i < count; i++) {
            triggerCollectionService.checkControllerCreatureCardLeavesGraveyardTriggers(gameData, ownerId);
        }
    }

    private void notifyArtifactOrCreatureCardsLeftGraveyard(GameData gameData, UUID ownerId) {
        if (gameData.graveyardLeaveNotificationDepth > 0) {
            gameData.graveyardLeaveNotificationPendingArtifactOrCreatureOwners.add(ownerId);
            return;
        }
        triggerCollectionService.checkControllerArtifactOrCreatureCardsLeaveGraveyardTriggers(
                gameData, ownerId);
    }

    private boolean isArtifactOrCreatureCard(Card card) {
        return card != null
                && !card.isToken()
                && (card.hasType(CardType.ARTIFACT) || card.hasType(CardType.CREATURE));
    }

    /**
     * Notifies that a single, known card left the given player's graveyard: the batched
     * "one or more cards left your graveyard" event plus the per-card
     * {@link EffectSlot#GRAVEYARD_ON_CREATURE_CARD_LEAVES_OPPONENT_GRAVEYARD} trigger, which fires
     * once per creature card and is never batched.
     */
    public void notifyCardLeftGraveyard(GameData gameData, UUID ownerId, Card leavingCard) {
        notifyCardsLeftGraveyard(gameData, ownerId, leavingCard);
        triggerCollectionService.checkCreatureCardLeavesOpponentGraveyardTriggers(gameData, ownerId, leavingCard);
    }

    /** Adds a card from a graveyard to a hand and fires the card's self-return trigger, if any. */
    public void addCardToHandFromGraveyard(GameData gameData, UUID graveyardOwnerId, UUID handOwnerId,
                                           Card card) {
        gameData.addCardToHand(handOwnerId, card);
        if (graveyardOwnerId != null && graveyardOwnerId.equals(handOwnerId)) {
            triggerCollectionService.checkCardReturnedToHandFromGraveyardTriggers(
                    gameData, graveyardOwnerId, card);
        }
    }

    /**
     * Drops returnable "damaged by source and died" card ids that are no longer in any graveyard.
     */
    private void pruneDamagedCreatureDiedTrackingNotInGraveyard(GameData gameData) {
        if (gameData.creatureCardsDamagedBySourceThatDiedThisTurn.isEmpty()) {
            return;
        }
        for (Set<UUID> cardIds : gameData.creatureCardsDamagedBySourceThatDiedThisTurn.values()) {
            cardIds.removeIf(id -> gameQueryService.findCardInGraveyardById(gameData, id) == null);
        }
    }

    /**
     * Clears a player's graveyard and fires a single leave-graveyard trigger if it was non-empty.
     * The cards must already have been moved to their destination zone (exile, etc.) by the caller —
     * this only empties the graveyard list and fires the trigger.
     */
    public void clearGraveyard(GameData gameData, UUID ownerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }
        List<Card> leavingCards = new ArrayList<>(graveyard);
        List<Card> leavingCreatureCards = graveyard.stream().filter(c -> c.hasType(CardType.CREATURE)).toList();
        graveyard.clear();
        Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.get(ownerId);
        if (tracked != null) {
            tracked.clear();
        }
        Set<UUID> allTracked = gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.get(ownerId);
        if (allTracked != null) {
            allTracked.clear();
        }
        notifyCardsLeftGraveyard(gameData, ownerId, leavingCards);
        for (Card leaving : leavingCreatureCards) {
            triggerCollectionService.checkCreatureCardLeavesOpponentGraveyardTriggers(gameData, ownerId, leaving);
        }
    }

    /**
     * Empties a player's graveyard for a bulk move into another zone (shuffle-graveyard-into-library
     * effects) and returns the cards that actually make the trip, leaving the graveyard cleared and
     * the leave-graveyard trigger fired exactly as {@link #clearGraveyard} does.
     *
     * <p>Token cards never make the trip: a token in a zone other than the battlefield ceases to
     * exist (CR 111.7), so a graveyard shuffled into a library must leave its dead tokens behind
     * rather than turn them into phantom cards their owner can draw. The engine keeps dead tokens
     * sitting in the graveyard and filters them at read sites, so every bulk mover must come
     * through here.
     */
    public List<Card> takeGraveyardCardsForZoneChange(GameData gameData, UUID ownerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return List.of();
        }
        List<Card> moving = graveyard.stream().filter(card -> !card.isToken()).toList();
        clearGraveyard(gameData, ownerId);
        return moving;
    }

    /**
     * Moves a batch of cards from a player's library to their graveyard and fires the aggregate
     * library-to-graveyard creature trigger once for the batch.
     *
     * @return the cards that actually entered the graveyard after replacement effects
     */
    public List<Card> addCardsFromLibraryToGraveyard(GameData gameData, UUID ownerId,
                                                     List<Card> cards) {
        List<Card> entered = new ArrayList<>();
        for (Card card : cards) {
            if (addCardToGraveyard(gameData, ownerId, card, Zone.LIBRARY, true)) {
                entered.add(card);
            }
        }
        int creatureCardsEntered = (int) entered.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .count();
        triggerCollectionService.checkCreatureCardsPutIntoGraveyardFromLibraryTriggers(
                gameData, ownerId, creatureCardsEntered);
        return entered;
    }

    /**
     * Filtered sibling of {@link #takeGraveyardCardsForZoneChange(GameData, UUID)}: only the
     * non-token cards matching {@code filter} leave the graveyard, everything else stays put.
     * Used by partial shuffle-back effects such as Barishi's "shuffle all creature cards from your
     * graveyard into your library".
     */
    public List<Card> takeMatchingGraveyardCardsForZoneChange(GameData gameData, UUID ownerId,
                                                              CardPredicate filter, UUID sourceCardId) {
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return List.of();
        }
        List<Card> moving = graveyard.stream()
                .filter(card -> !card.isToken())
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, filter, sourceCardId))
                .toList();
        if (moving.isEmpty()) {
            return List.of();
        }
        graveyard.removeAll(moving);
        for (Card card : moving) {
            Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.get(ownerId);
            if (tracked != null) {
                tracked.remove(card.getId());
            }
            Set<UUID> allTracked = gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.get(ownerId);
            if (allTracked != null) {
                allTracked.remove(card.getId());
            }
        }
        notifyCardsLeftGraveyard(gameData, ownerId, moving);
        return moving;
    }
}
