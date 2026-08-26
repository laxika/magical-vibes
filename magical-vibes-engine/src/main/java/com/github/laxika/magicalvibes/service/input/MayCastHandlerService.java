package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetCardFromGraveyardIfNoSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastForMadnessCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastForMiracleCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayCastHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final ExileService exileService;
    private final ExileFreeCastSupport exileFreeCastSupport;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final com.github.laxika.magicalvibes.service.cast.PotentialManaService potentialManaService;
    private final SpellCastingService spellCastingService;
    private final TargetLegalityService targetLegalityService;
    private final CopySupport copySupport;

    public void handleCastFromLibraryChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();

        if (accepted) {
            List<Card> deck = gameData.playerDecks.get(player.getId());

            // Verify the card is still on top of the library
            if (deck.isEmpty() || !deck.getFirst().getId().equals(cardToCast.getId())) {
                
                gameLogService.append(gameData, GameLog.cardThen(cardToCast, " is no longer on top of the library."));
                log.info("Game {} - {} no longer on top of library for cast-from-library", gameData.id, cardToCast.getName());
            } else {
                if (cardToCast.isCastOnlyFromGraveyard()) {
                    gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                            " cannot be cast from the library."));
                    inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                    return;
                }
                deck.removeFirst();

                List<CardEffect> spellEffects = new ArrayList<>(cardToCast.getEffects(EffectSlot.SPELL));
                StackEntryType spellType = cardToCast.hasType(CardType.INSTANT)
                        ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;

                if (EffectResolution.needsTarget(cardToCast) || EffectResolution.needsSpellTarget(cardToCast)) {
                    // Targeted spell — need to choose target before putting on stack
                    List<UUID> validTargets = buildValidSpellTargets(gameData, cardToCast, spellEffects, player.getId());

                    if (validTargets.isEmpty()) {
                        // No valid targets — spell can't be cast, put card back on top of library
                        deck.addFirst(cardToCast);
                        
                        gameLogService.append(gameData, GameLog.cardThen(cardToCast, " has no valid targets."));
                        log.info("Game {} - {} cast-from-library has no valid targets", gameData.id, cardToCast.getName());
                    } else {
                        gameData.interaction.setPermanentChoiceContext(
                                new PermanentChoiceContext.LibraryCastSpellTarget(cardToCast, player.getId(), spellEffects, spellType));
                        playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                                "Choose a target for " + cardToCast.getName() + ".");

                        
                        gameLogService.append(gameData, GameLog.textCardText(playerName + " casts " , cardToCast, " without paying its mana cost — choosing target."));
                        log.info("Game {} - {} casts {} from library, choosing target", gameData.id, playerName, cardToCast.getName());
                        return; // Wait for target choice
                    }
                } else {
                    // Non-targeted spell — put directly on stack
                    gameData.stack.add(new StackEntry(
                            spellType, cardToCast, player.getId(), cardToCast.getName(),
                            spellEffects, 0, (UUID) null, null
                    ));

                    gameData.recordSpellCast(player.getId(), cardToCast);
                    gameData.priorityPassedBy.clear();

                    
                    gameLogService.append(gameData, GameLog.textCardText(playerName + " casts " , cardToCast, " without paying its mana cost."));
                    log.info("Game {} - {} casts {} from library without paying mana", gameData.id, playerName, cardToCast.getName());

                    triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, player.getId(), false);
                }
            }
        } else {
            gameLogService.append(gameData, GameLog.textCardText(playerName + " declines to cast " , cardToCast, "."));
            log.info("Game {} - {} declines to cast {} from library", gameData.id, playerName, cardToCast.getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Handles the "may play the revealed top card of your library" choice (e.g. Djinn of Wishes).
     * If accepted: play the card (land → battlefield, spell → stack without paying mana cost).
     * If declined: the card goes to the effect's not-played destination (hand, exile, bottom of
     * library, or stays on top).
     */
    public void handlePlayFromLibraryOrExileChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card cardToPlay = ability.sourceCard();
        String playerName = player.getUsername();
        List<Card> deck = gameData.playerDecks.get(player.getId());
        LookDestination notPlayedDestination = ability.effects().stream()
                .filter(e -> e instanceof RevealTopCardMayPlayFreeEffect)
                .map(e -> ((RevealTopCardMayPlayFreeEffect) e).notPlayedDestination())
                .findFirst().orElse(LookDestination.EXILE);

        if (!accepted) {
            switch (notPlayedDestination) {
                case HAND -> putTopCardIntoHand(gameData, player.getId(), deck, cardToPlay, playerName);
                case EXILE -> exileTopCardFromLibrary(gameData, player.getId(), deck, cardToPlay, playerName);
                case BOTTOM_OF_LIBRARY -> bottomTopCardOfLibrary(gameData, deck, cardToPlay, playerName);
                default -> {
                    // Declined — the card stays on top of the library
                    gameLogService.append(gameData, GameLog.textCardText(playerName + " declines to play ", cardToPlay, "."));
                    log.info("Game {} - {} declines to play {}, stays on top", gameData.id, playerName, cardToPlay.getName());
                }
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Verify the card is still on top of the library
        if (deck.isEmpty() || !deck.getFirst().getId().equals(cardToPlay.getId())) {
            gameLogService.append(gameData, GameLog.cardThen(cardToPlay, " is no longer on top of the library."));
            log.info("Game {} - {} no longer on top of library for play-from-library", gameData.id, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (cardToPlay.isCastOnlyFromGraveyard()) {
            gameLogService.append(gameData, GameLog.cardThen(cardToPlay,
                    " cannot be cast from the library."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (cardToPlay.hasType(CardType.LAND)) {
            // Play the land: put onto battlefield, increment land play count
            deck.removeFirst();
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), new Permanent(cardToPlay));
            gameData.landsPlayedThisTurn.merge(player.getId(), 1, Integer::sum);

            gameLogService.append(gameData,
                    GameLog.playerPlays(playerName, cardToPlay, " without paying its mana cost."));
            log.info("Game {} - {} plays {} (land) from library", gameData.id, playerName, cardToPlay.getName());

            battlefieldEntryService.processLandETBEffects(gameData, player.getId(), cardToPlay);
            if (!gameData.interaction.isAwaitingInput()) {
                triggerCollectionService.checkControllerPlaysLandTriggers(gameData, player.getId(), cardToPlay);
            }
        } else {
            // Cast the spell without paying its mana cost
            deck.removeFirst();

            StackEntryType spellType = switch (cardToPlay.getType()) {
                case CREATURE -> StackEntryType.CREATURE_SPELL;
                case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
                case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
                case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
                case BATTLE -> StackEntryType.BATTLE_SPELL;
                case SORCERY -> StackEntryType.SORCERY_SPELL;
                case INSTANT -> StackEntryType.INSTANT_SPELL;
                default -> throw new IllegalStateException("Unsupported card type: " + cardToPlay.getType());
            };

            // For permanent spells (creature/artifact/enchantment/planeswalker), effects are empty;
            // ETB effects are processed when the permanent enters the battlefield.
            // For instant/sorcery, use the SPELL slot effects.
            boolean isPermanentSpell = cardToPlay.hasType(CardType.CREATURE)
                    || cardToPlay.hasType(CardType.ARTIFACT)
                    || cardToPlay.hasType(CardType.ENCHANTMENT)
                    || cardToPlay.hasType(CardType.PLANESWALKER);
            List<CardEffect> spellEffects = isPermanentSpell
                    ? List.of()
                    : new ArrayList<>(cardToPlay.getEffects(EffectSlot.SPELL));

            if (EffectResolution.needsTarget(cardToPlay) || EffectResolution.needsSpellTarget(cardToPlay)) {
                // Targeted spell — need to choose target before putting on stack
                List<UUID> validTargets = buildValidSpellTargets(gameData, cardToPlay, spellEffects, player.getId());

                if (validTargets.isEmpty()) {
                    switch (notPlayedDestination) {
                        case HAND -> {
                            gameData.playerHands.get(player.getId()).add(cardToPlay);
                            gameLogService.append(gameData, GameLog.cardThen(cardToPlay,
                                    " can't be cast and is put into " + playerName + "'s hand."));
                        }
                        case EXILE -> {
                            // No valid targets — exile the card instead
                            exileService.exileCard(gameData, player.getId(), cardToPlay);
                            gameLogService.append(gameData, GameLog.cardThen(cardToPlay, " has no valid targets and is exiled."));
                            log.info("Game {} - {} play-from-library has no valid targets, exiled", gameData.id, cardToPlay.getName());
                        }
                        case BOTTOM_OF_LIBRARY -> {
                            // No valid targets — the card goes to the bottom of the library
                            deck.add(cardToPlay);
                            gameLogService.append(gameData, GameLog.cardThen(cardToPlay, " has no valid targets and is put on the bottom of the library."));
                            log.info("Game {} - {} play-from-library has no valid targets, bottomed", gameData.id, cardToPlay.getName());
                        }
                        default -> {
                            // No valid targets — return the card to the top of the library
                            deck.addFirst(cardToPlay);
                            gameLogService.append(gameData, GameLog.cardThen(cardToPlay, " has no valid targets and stays on top of the library."));
                            log.info("Game {} - {} play-from-library has no valid targets, stays on top", gameData.id, cardToPlay.getName());
                        }
                    }
                } else {
                    gameData.interaction.setPermanentChoiceContext(
                            new PermanentChoiceContext.LibraryCastSpellTarget(cardToPlay, player.getId(), spellEffects, spellType));
                    playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                            "Choose a target for " + cardToPlay.getName() + ".");

                    
                    gameLogService.append(gameData, GameLog.textCardText(playerName + " casts " , cardToPlay, " without paying its mana cost — choosing target."));
                    log.info("Game {} - {} casts {} from library, choosing target", gameData.id, playerName, cardToPlay.getName());
                    return; // Wait for target choice
                }
            } else {
                // Non-targeted spell — put directly on stack
                gameData.stack.add(new StackEntry(
                        spellType, cardToPlay, player.getId(), cardToPlay.getName(),
                        spellEffects, 0, (UUID) null, null
                ));

                gameData.recordSpellCast(player.getId(), cardToPlay);
                gameData.priorityPassedBy.clear();

                gameLogService.append(gameData, GameLog.textCardText(playerName + " casts " , cardToPlay, " without paying its mana cost."));
                log.info("Game {} - {} casts {} from library without paying mana", gameData.id, playerName, cardToPlay.getName());

                triggerCollectionService.checkSpellCastTriggers(gameData, cardToPlay, player.getId(), false);
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Casts a revealed card that is currently held outside every zone (Talent of the Telepath)
     * without paying its mana cost. A targeted spell pauses for target selection; a spell with no
     * legal target can't be cast (CR 601.2c) and joins the rest of the revealed cards in
     * {@code ownerId}'s graveyard, since the held cards have already left the library.
     */
    public void castRevealedCardWithoutPaying(GameData gameData, Player player, Card card, UUID ownerId) {
        castRevealedCardWithoutPaying(gameData, player, card, ownerId, null, false);
    }

    public void castRevealedCardWithoutPaying(GameData gameData, Player player, Card card, UUID ownerId,
                                              List<Card> cardsToBottom, boolean deferCompletion) {
        String playerName = player.getUsername();
        StackEntryType spellType = switch (card.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case BATTLE -> StackEntryType.BATTLE_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            default -> throw new IllegalStateException("Unsupported card type: " + card.getType());
        };
        boolean permanentSpell = card.hasType(CardType.CREATURE)
                || card.hasType(CardType.ARTIFACT)
                || card.hasType(CardType.ENCHANTMENT)
                || card.hasType(CardType.PLANESWALKER);
        List<CardEffect> spellEffects = permanentSpell
                ? List.of()
                : new ArrayList<>(card.getEffects(EffectSlot.SPELL));

        if (EffectResolution.needsTarget(card) || EffectResolution.needsSpellTarget(card)) {
            List<UUID> validTargets = buildValidSpellTargets(gameData, card, spellEffects, player.getId());
            if (validTargets.isEmpty()) {
                if (cardsToBottom == null) {
                    graveyardService.addCardToGraveyard(gameData, ownerId, card);
                    gameLogService.append(gameData, GameLog.cardThen(card,
                            " has no legal targets, so it can't be cast. It is put into the graveyard."));
                } else {
                    List<Card> uncastCards = new ArrayList<>(cardsToBottom);
                    uncastCards.add(card);
                    beginBottomReorder(gameData, ownerId, uncastCards);
                }
                log.info("Game {} - {} revealed free cast has no legal targets", gameData.id, card.getName());
            } else {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.LibraryCastSpellTarget(
                                card, player.getId(), spellEffects, spellType, cardsToBottom));
                playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                        "Choose a target for " + card.getName() + ".");
                gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", card,
                        " without paying its mana cost — choosing target."));
                return;
            }
        } else {
            gameData.stack.add(new StackEntry(
                    spellType, card, player.getId(), card.getName(), spellEffects, 0, (UUID) null, null));

            gameData.recordSpellCast(player.getId(), card);
            gameData.priorityPassedBy.clear();

            gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", card,
                    " without paying its mana cost."));
            log.info("Game {} - {} casts revealed {} without paying mana", gameData.id, playerName, card.getName());

            triggerCollectionService.checkSpellCastTriggers(gameData, card, player.getId(), false);
            if (cardsToBottom != null) {
                beginBottomReorder(gameData, ownerId, cardsToBottom);
            }
        }

        if (!deferCompletion && !gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    private void beginBottomReorder(GameData gameData, UUID ownerId, List<Card> cards) {
        if (cards.isEmpty()) return;
        if (cards.size() == 1) {
            gameData.playerDecks.get(ownerId).add(cards.getFirst());
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                ownerId,
                new ArrayList<>(cards),
                true,
                ownerId,
                "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
    }

    /**
     * Builds a list of valid target UUIDs for a targeted spell, including permanents, players, and
     * spells on the stack as appropriate based on the spell's effects and target filter.
     */
    List<UUID> buildValidSpellTargets(GameData gameData, Card card, List<CardEffect> spellEffects) {
        return buildValidSpellTargets(gameData, card, spellEffects, card.getOwnerId());
    }

    List<UUID> buildValidSpellTargets(GameData gameData, Card card, List<CardEffect> spellEffects,
                                      UUID controllerId) {
        return buildValidSpellTargets(gameData, card, spellEffects, controllerId, 0, false);
    }

    List<UUID> buildValidSpellTargets(GameData gameData, Card card, List<CardEffect> spellEffects,
                                      UUID controllerId, int xValue, boolean castForMadnessCost) {
        List<UUID> validTargets = new ArrayList<>();
        boolean canTargetPermanent = spellEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                || card.getTargetFilter() instanceof PermanentPredicateTargetFilter;
        if (canTargetPermanent) {
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (card.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                        FilterContext filterContext = FilterContext.of(gameData)
                                .withSourceCardId(card.getId())
                                .withSourceControllerId(controllerId)
                                .withXValue(xValue)
                                .withMadness(castForMadnessCost);
                        if (predicateEvaluationService.matchesPermanentPredicate(p, filter.predicate(), filterContext)) {
                            validTargets.add(p.getId());
                        }
                    } else if (gameQueryService.isCreature(gameData, p)) {
                        validTargets.add(p.getId());
                    }
                }
            }
        }
        boolean canTargetPlayer = spellEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        if (canTargetPlayer) {
            validTargets.addAll(gameData.orderedPlayerIds);
        }
        boolean canTargetSpell = spellEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.SPELL));
        if (canTargetSpell) {
            for (StackEntry stackEntry : gameData.stack) {
                UUID targetId = stackEntry.getCard().getId();
                if (targetLegalityService.checkSpellTargetOnStack(
                        gameData, targetId, card.getTargetFilter(), controllerId, null).isEmpty()) {
                    validTargets.add(targetId);
                }
            }
        }
        return validTargets;
    }

    private void bottomTopCardOfLibrary(GameData gameData, List<Card> deck, Card card, String playerName) {
        if (deck != null && !deck.isEmpty() && deck.getFirst().getId().equals(card.getId())) {
            deck.removeFirst();
            deck.add(card);
        }
        gameLogService.append(gameData, GameLog.textCardText(playerName + " puts ", card, " on the bottom of their library."));
        log.info("Game {} - {} puts {} on the bottom of their library", gameData.id, playerName, card.getName());
    }

    private void putTopCardIntoHand(GameData gameData, UUID playerId, List<Card> deck,
                                    Card card, String playerName) {
        if (deck != null && !deck.isEmpty() && deck.getFirst().getId().equals(card.getId())) {
            deck.removeFirst();
            gameData.playerHands.get(playerId).add(card);
        }
        gameLogService.append(gameData, GameLog.cardThen(card,
                " is put into " + playerName + "'s hand."));
        log.info("Game {} - {} puts {} into hand", gameData.id, playerName, card.getName());
    }

    private void exileTopCardFromLibrary(GameData gameData, UUID playerId, List<Card> deck, Card card, String playerName) {
        if (!deck.isEmpty() && deck.getFirst().getId().equals(card.getId())) {
            deck.removeFirst();
        }
        exileService.exileCard(gameData, playerId, card);
        
        gameLogService.append(gameData, GameLog.textCardText(playerName + " exiles " , card, "."));
        log.info("Game {} - {} exiles {} from library", gameData.id, playerName, card.getName());
    }

    public void handleCastFromGraveyardChoice(GameData gameData, Player player, boolean accepted,
                                               PendingMayAbility ability,
                                               CastTargetInstantOrSorceryFromGraveyardEffect castEffect) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();
        GraveyardSearchScope scope = castEffect.scope();
        String castLabel = castEffect.withoutPayingManaCost() ? " without paying its mana cost" : "";

        // Ashes of the Abhorrent etc.: players can't cast spells from graveyards
        if (accepted && !gameQueryService.canCastSpellFromZone(gameData, cardToCast, Zone.GRAVEYARD)) {
            
            gameLogService.append(gameData, GameLog.cardThen(cardToCast, " can't be cast from the graveyard."));
            accepted = false;
        }

        if (accepted) {
            // Verify the card is still in a graveyard matching the scope
            Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, cardToCast.getId());
            if (graveyardCard == null) {
                
                gameLogService.append(gameData, GameLog.cardThen(cardToCast, " is no longer in the graveyard."));
                log.info("Game {} - {} no longer in graveyard for cast-from-graveyard", gameData.id, cardToCast.getName());
            } else {
                UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardToCast.getId());
                boolean validScope = graveyardOwnerId != null && switch (scope) {
                    case OPPONENT_GRAVEYARD -> !graveyardOwnerId.equals(player.getId());
                    case CONTROLLERS_GRAVEYARD -> graveyardOwnerId.equals(player.getId());
                    case ALL_GRAVEYARDS -> true;
                };
                boolean matchesFilter = castEffect.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        graveyardCard,
                        castEffect.filter(),
                        cardToCast.getId(),
                        gameData,
                        graveyardOwnerId,
                        ability.sourcePermanentId(),
                        ability.sourcePowerAtTrigger(),
                        ability.xValue());
                if (!validScope || !matchesFilter) {
                    
                    gameLogService.append(gameData, GameLog.cardThen(cardToCast, " is no longer in a valid graveyard."));
                    log.info("Game {} - {} not in valid graveyard (scope={})", gameData.id, cardToCast.getName(), scope);
                } else {
                    List<CardEffect> spellEffects = new ArrayList<>(cardToCast.getEffects(EffectSlot.SPELL));
                    StackEntryType spellType = cardToCast.hasType(CardType.INSTANT)
                            ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;

                    if (EffectResolution.needsTarget(cardToCast) || EffectResolution.needsSpellTarget(cardToCast)) {
                        // Targeted spell — need to choose target before putting on stack
                        List<UUID> validTargets = buildValidSpellTargets(gameData, cardToCast, spellEffects, player.getId());

                        if (validTargets.isEmpty()) {
                            // No valid targets — card goes to owner's graveyard
                            gameLogService.append(gameData, GameLog.cardThen(cardToCast, " has no valid targets."));
                            log.info("Game {} - {} cast-from-graveyard has no valid targets", gameData.id, cardToCast.getName());
                        } else {
                            permanentRemovalService.removeCardFromGraveyardById(gameData, cardToCast.getId());
                            gameData.interaction.setPermanentChoiceContext(
                            new PermanentChoiceContext.GraveyardCastSpellTarget(cardToCast, player.getId(),
                                            spellEffects, spellType, castEffect.exileInsteadOfGraveyard(),
                                            castEffect.withoutPayingManaCost(), graveyardOwnerId,
                                            castEffect.copyCount()));
                            playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                                    "Choose a target for " + cardToCast.getName() + ".");

                            
                            gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                                    " is being cast from the graveyard, choosing a target."));
                            log.info("Game {} - {} casts {} from graveyard, choosing target", gameData.id, playerName, cardToCast.getName());
                            return; // Wait for target choice
                        }
                    } else {
                        // Non-targeted spell — put directly on stack
                        if (!castEffect.withoutPayingManaCost()) {
                            try {
                                spellCastingService.paySpellManaCostFromNonHandZone(gameData, player.getId(), cardToCast, 0,
                                        Zone.GRAVEYARD);
                            } catch (IllegalStateException ex) {
                                gameLogService.append(gameData, GameLog.cardThen(cardToCast, " can't be cast because its mana cost can't be paid."));
                                log.info("Game {} - {} cannot pay to cast {} from graveyard", gameData.id, playerName, cardToCast.getName());
                                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                                return;
                            }
                        }
                        permanentRemovalService.removeCardFromGraveyardById(gameData, cardToCast.getId());
                        StackEntry freeCast = new StackEntry(
                                spellType, cardToCast, player.getId(), cardToCast.getName(),
                                spellEffects, 0, (UUID) null, null
                        );
                        freeCast.setExileInsteadOfGraveyard(castEffect.exileInsteadOfGraveyard());
                        freeCast.setOwnerIdOverride(graveyardOwnerId);
                        freeCast.setSourceZone(Zone.GRAVEYARD);
                        gameData.stack.add(freeCast);

                        for (int i = 0; i < castEffect.copyCount(); i++) {
                            Card copyCard = copySupport.createCopyCard(cardToCast);
                            StackEntry copyEntry = copySupport.createCopyStackEntry(
                                    freeCast, copyCard, player.getId(), freeCast.getTargetId());
                            gameData.stack.add(copyEntry);
                            copySupport.checkSpellCopyTriggers(gameData, copyEntry);
                            if (copyEntry.getTargetId() != null) {
                                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                                        cardToCast, player.getId(), List.of(new CopySpellEffect()),
                                        "Choose new targets for the copy of " + cardToCast.getName() + "?",
                                        copyCard.getId()));
                            }
                        }

                        gameData.recordSpellCast(player.getId(), cardToCast);
                        gameData.priorityPassedBy.clear();

                        
                        gameLogService.append(gameData, GameLog.builder().text(playerName + " casts ").card(cardToCast).text(castLabel + ".").build());
                        log.info("Game {} - {} casts {} from graveyard", gameData.id, playerName, cardToCast.getName());

                        triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, player.getId(), false);
                    }
                }
            }
        } else {
            
            gameLogService.append(gameData, GameLog.textCardText(playerName + " declines to cast " , cardToCast, "."));
            log.info("Game {} - {} declines to cast {} from graveyard", gameData.id, playerName, cardToCast.getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleCastCardFromGraveyardIfNoSpellThisTurnChoice(
            GameData gameData, Player player, boolean accepted, PendingMayAbility ability,
            CastTargetCardFromGraveyardIfNoSpellThisTurnEffect castEffect) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();

        if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(playerName + " declines to cast ", cardToCast, "."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (!gameQueryService.canCastSpellFromZone(gameData, cardToCast, Zone.GRAVEYARD)) {
            gameLogService.append(gameData, GameLog.cardThen(cardToCast, " can't be cast from the graveyard."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, cardToCast.getId());
        UUID graveyardOwnerId = graveyardCard == null
                ? null : gameQueryService.findGraveyardOwnerById(gameData, cardToCast.getId());
        boolean validScope = graveyardOwnerId != null && switch (castEffect.scope()) {
            case OPPONENT_GRAVEYARD -> !graveyardOwnerId.equals(player.getId());
            case CONTROLLERS_GRAVEYARD -> graveyardOwnerId.equals(player.getId());
            case ALL_GRAVEYARDS -> true;
        };
        boolean matchesFilter = graveyardCard != null
                && (castEffect.filter() == null
                || predicateEvaluationService.matchesCardPredicate(graveyardCard, castEffect.filter(), cardToCast.getId()));
        if (!validScope || !matchesFilter) {
            gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                    " is no longer a legal card to cast from the graveyard."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntryType spellType = switch (cardToCast.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case BATTLE -> StackEntryType.BATTLE_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            default -> throw new IllegalStateException("Unsupported card type: " + cardToCast.getType());
        };
        boolean isPermanentSpell = cardToCast.hasType(CardType.CREATURE)
                || cardToCast.hasType(CardType.ARTIFACT)
                || cardToCast.hasType(CardType.ENCHANTMENT)
                || cardToCast.hasType(CardType.PLANESWALKER)
                || cardToCast.hasType(CardType.BATTLE);
        List<CardEffect> spellEffects = isPermanentSpell
                ? List.of()
                : new ArrayList<>(cardToCast.getEffects(EffectSlot.SPELL));

        if (EffectResolution.needsTarget(cardToCast)) {
            List<UUID> validTargets = buildValidSpellTargets(gameData, cardToCast, spellEffects);
            if (validTargets.isEmpty()) {
                gameLogService.append(gameData, GameLog.cardThen(cardToCast, " has no valid targets."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            permanentRemovalService.removeCardFromGraveyardById(gameData, cardToCast.getId());
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.GraveyardCastSpellTarget(cardToCast, player.getId(),
                            spellEffects, spellType, false, false, graveyardOwnerId, true));
            playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                    "Choose a target for " + cardToCast.getName() + ".");
            gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                    " is being cast from the graveyard, choosing a target."));
            return;
        }

        try {
            spellCastingService.paySpellManaCostFromNonHandZone(gameData, player.getId(), cardToCast, 0,
                    Zone.GRAVEYARD);
        } catch (IllegalStateException ex) {
            gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                    " can't be cast because its mana cost can't be paid."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardToCast.getId());
        StackEntry stackEntry = new StackEntry(
                spellType, cardToCast, player.getId(), cardToCast.getName(),
                spellEffects, 0, (UUID) null, null);
        stackEntry.setOwnerIdOverride(graveyardOwnerId);
        stackEntry.setSourceZone(Zone.GRAVEYARD);
        gameData.stack.add(stackEntry);
        gameData.recordSpellCast(player.getId(), cardToCast);
        gameData.preventAdditionalSpellCastsThisTurn(player.getId());
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData, GameLog.builder().text(playerName + " casts ").card(cardToCast)
                .text(" from the graveyard.").build());
        triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, player.getId(), false);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Handles the "you may play target [type] card from your graveyard without paying its mana cost"
     * choice (e.g. Horde of Notions). If accepted: a land is put onto the battlefield, any other card
     * is cast without paying its mana cost. Restricted to the controller's own graveyard.
     */
    public void handlePlayFromGraveyardChoice(GameData gameData, Player player, boolean accepted,
                                              PendingMayAbility ability,
                                              PlayTargetCardFromGraveyardWithoutPayingManaCostEffect effect) {
        handlePlayFromGraveyardChoice(gameData, player, accepted, ability, effect.filter(),
                player.getId(), false);
    }

    public void handleCastFromSpecificGraveyardChoice(GameData gameData, Player player, boolean accepted,
                                                      PendingMayAbility ability, UUID graveyardOwnerId) {
        handlePlayFromGraveyardChoice(gameData, player, accepted, ability, null,
                graveyardOwnerId, true);
    }

    private void handlePlayFromGraveyardChoice(GameData gameData, Player player, boolean accepted,
                                               PendingMayAbility ability, CardPredicate filter,
                                               UUID expectedGraveyardOwnerId,
                                               boolean exileInsteadOfGraveyard) {
        Card cardToPlay = ability.sourceCard();
        String playerName = player.getUsername();

        if (!accepted) {
            
            gameLogService.append(gameData, GameLog.textCardText(playerName + " declines to play " , cardToPlay, "."));
            log.info("Game {} - {} declines to play {} from graveyard", gameData.id, playerName, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Non-land cards can't be cast from graveyards if a permanent forbids it (e.g. Ashes of the Abhorrent).
        if (!cardToPlay.hasType(CardType.LAND)
                && !gameQueryService.canCastSpellFromZone(gameData, cardToPlay, Zone.GRAVEYARD)) {
            
            gameLogService.append(gameData, GameLog.cardThen(cardToPlay, " can't be cast from the graveyard."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Verify the card is still in the controller's own graveyard and matches the filter.
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, cardToPlay.getId());
        UUID graveyardOwnerId = graveyardCard == null
                ? null : gameQueryService.findGraveyardOwnerById(gameData, cardToPlay.getId());
        if (graveyardCard == null || graveyardOwnerId == null
                || !graveyardOwnerId.equals(expectedGraveyardOwnerId)
                || !predicateEvaluationService.matchesCardPredicate(graveyardCard, filter, null)) {
            gameLogService.append(gameData, GameLog.cardThen(cardToPlay, " is no longer a legal target in your graveyard."));
            log.info("Game {} - {} no longer a legal graveyard target for play-from-graveyard", gameData.id, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardToPlay.getId());

        if (cardToPlay.hasType(CardType.LAND)) {
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), new Permanent(cardToPlay));
            gameData.landsPlayedThisTurn.merge(player.getId(), 1, Integer::sum);

            gameLogService.append(gameData, GameLog.playerPlays(playerName, cardToPlay,
                    " from their graveyard without paying its mana cost."));
            log.info("Game {} - {} plays {} (land) from graveyard", gameData.id, playerName, cardToPlay.getName());

            battlefieldEntryService.processLandETBEffects(gameData, player.getId(), cardToPlay);
            if (!gameData.interaction.isAwaitingInput()) {
                triggerCollectionService.checkControllerPlaysLandTriggers(gameData, player.getId(), cardToPlay);
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntryType spellType = switch (cardToPlay.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case BATTLE -> StackEntryType.BATTLE_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            default -> throw new IllegalStateException("Unsupported card type: " + cardToPlay.getType());
        };

        // Permanent spells have empty SPELL effects — ETB is processed on battlefield entry.
        boolean isPermanentSpell = cardToPlay.hasType(CardType.CREATURE)
                || cardToPlay.hasType(CardType.ARTIFACT)
                || cardToPlay.hasType(CardType.ENCHANTMENT)
                || cardToPlay.hasType(CardType.PLANESWALKER)
                || cardToPlay.hasType(CardType.BATTLE);
        List<CardEffect> spellEffects = isPermanentSpell
                ? List.of()
                : new ArrayList<>(cardToPlay.getEffects(EffectSlot.SPELL));

        if (EffectResolution.needsTarget(cardToPlay) || EffectResolution.needsSpellTarget(cardToPlay)) {
            List<UUID> validTargets = buildValidSpellTargets(gameData, cardToPlay, spellEffects, player.getId());

            if (validTargets.isEmpty()) {
                // No valid targets — card goes back to owner's graveyard.
                graveyardService.addCardToGraveyard(gameData, graveyardOwnerId, cardToPlay);
                gameLogService.append(gameData, GameLog.cardThen(cardToPlay, " has no valid targets."));
                log.info("Game {} - {} play-from-graveyard has no valid targets", gameData.id, cardToPlay.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.GraveyardCastSpellTarget(cardToPlay, player.getId(), spellEffects,
                            spellType, exileInsteadOfGraveyard, true, graveyardOwnerId));
            playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                    "Choose a target for " + cardToPlay.getName() + ".");

            gameLogService.append(gameData, GameLog.playerPlays(playerName, cardToPlay,
                    " from their graveyard without paying its mana cost — choosing target."));
            log.info("Game {} - {} casts {} from graveyard, choosing target", gameData.id, playerName, cardToPlay.getName());
            return; // Wait for target choice
        }

        StackEntry stackEntry = new StackEntry(
                spellType, cardToPlay, player.getId(), cardToPlay.getName(),
                spellEffects, 0, (UUID) null, null
        );
        stackEntry.setExileInsteadOfGraveyard(exileInsteadOfGraveyard);
        stackEntry.setOwnerIdOverride(graveyardOwnerId);
        stackEntry.setSourceZone(Zone.GRAVEYARD);
        gameData.stack.add(stackEntry);
        gameData.recordSpellCast(player.getId(), cardToPlay);
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData, GameLog.playerPlays(playerName, cardToPlay,
                " from their graveyard without paying its mana cost."));
        log.info("Game {} - {} casts {} from graveyard without paying mana", gameData.id, playerName, cardToPlay.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, cardToPlay, player.getId(), false);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Handles the "you may play the exiled card without paying its mana cost" choice for the
     * Hideaway lands (e.g. Howltooth Hollow). The imprinted card leaves exile as it's played, so
     * the imprint pointer is cleared. A land is put onto the battlefield and counts as the land
     * play for the turn (per the hideaway ruling); any other card is cast from exile.
     */
    public void handlePlayImprintedCardChoice(GameData gameData, Player player, boolean accepted,
                                              PendingMayAbility ability) {
        Card cardToPlay = ability.sourceCard();
        String playerName = player.getUsername();

        if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(playerName + " declines to play ", cardToPlay, "."));
            log.info("Game {} - {} declines to play imprinted {}", gameData.id, playerName, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Verify the card is still in exile (nothing else has moved it out).
        if (gameData.findExiledCard(cardToPlay.getId()) == null) {
            gameLogService.append(gameData, GameLog.cardThen(cardToPlay, " is no longer in exile."));
            log.info("Game {} - imprinted {} no longer in exile", gameData.id, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (cardToPlay.isCastOnlyFromGraveyard()) {
            gameLogService.append(gameData, GameLog.cardThen(cardToPlay,
                    " cannot be cast from exile."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // The card leaves exile as it's played — clear the source's imprint pointer.
        if (ability.sourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
            if (source != null) {
                gameData.setImprintedCard(source.getCard(), null);
            }
        }

        gameLogService.append(gameData, GameLog.textCardText(playerName + " turns the exiled card face up: ", cardToPlay, "."));

        if (cardToPlay.hasType(CardType.LAND)) {
            gameData.removeFromExile(cardToPlay.getId());
            gameData.recordCardPlayedFromExile(player.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), new Permanent(cardToPlay));
            gameData.landsPlayedThisTurn.merge(player.getId(), 1, Integer::sum);
            gameLogService.append(gameData,
                    GameLog.playerPlays(playerName, cardToPlay, " without paying its mana cost."));
            battlefieldEntryService.processLandETBEffects(gameData, player.getId(), cardToPlay);
            if (!gameData.interaction.isAwaitingInput()) {
                triggerCollectionService.checkControllerPlaysLandTriggers(gameData, player.getId(), cardToPlay);
            }
            log.info("Game {} - {} plays imprinted land {} from exile", gameData.id, playerName, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Non-land: cast from exile without paying (handles targeting, the stack, and cast triggers).
        exileFreeCastSupport.castFromExileWithoutPaying(gameData, player, cardToPlay.getId());
    }

    /**
     * Handles the miracle "you may cast this for its miracle cost" choice (CR 702.94a).
     * Pays {@link PendingMayAbility#manaCost()} then casts from hand, ignoring type timing.
     */
    public void handleMayCastForMiracleCost(GameData gameData, Player player, boolean accepted,
                                            PendingMayAbility ability) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();

        if (!accepted) {
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " declines to cast ", cardToCast, " for its miracle cost."));
            log.info("Game {} - {} declines miracle cast of {}", gameData.id, playerName, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        List<Card> hand = gameData.playerHands.get(player.getId());
        int cardIndex = -1;
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).getId().equals(cardToCast.getId())) {
                    cardIndex = i;
                    break;
                }
            }
        }

        if (cardIndex == -1) {
            gameLogService.append(gameData, GameLog.cardThen(cardToCast, " is no longer in hand."));
            log.info("Game {} - {} no longer in hand for miracle cast", gameData.id, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (cardToCast.isCastOnlyFromGraveyard()) {
            gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                    " cannot be cast from hand."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        String costStr = ability.manaCost();
        if (costStr == null) {
            log.warn("Game {} - miracle cast of {} has no cost on pending ability", gameData.id, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        ManaCost cost = new ManaCost(costStr);
        ManaPool pool = gameData.playerManaPools.get(player.getId());

        // An {X} in the alternative cost still has to be announced (CR 601.2b), so the actual
        // payment waits for the X prompt. Entreat the Angels' miracle cost {X}{W}{W}.
        if (cost.hasX()) {
            beginAlternateCastXChoice(gameData, player, cardToCast, costStr, "miracle");
            return;
        }

        if (!cost.canPay(pool)) {
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " cannot pay " + costStr + " to cast ", cardToCast, " for its miracle cost."));
            log.info("Game {} - {} can't pay miracle cost {} for {}", gameData.id, playerName, costStr, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        cost.pay(pool);

        hand.remove(cardIndex);
        castCardFromHandPayingAlternateCost(gameData, player, cardToCast, costStr, "miracle", 0);
    }

    /**
     * Opens the "choose a value for X" prompt for a cast for an alternative cost containing {X}.
     * The cap is computed from potential mana so an untapped board still opens the prompt
     * (CR 605.3a — mana abilities may be activated while the payment is being made); the real
     * pool is re-checked in {@link #completeAlternateCastXChoice}.
     */
    private void beginAlternateCastXChoice(GameData gameData, Player player, Card cardToCast,
                                           String costStr, String costLabel) {
        ManaCost cost = new ManaCost(costStr);
        int maxX = cost.calculateMaxX(potentialManaService.buildVirtualManaPool(gameData, player.getId()));

        if (maxX <= 0 && !cost.canPay(gameData.playerManaPools.get(player.getId()), 0)) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " cannot pay " + costStr + " to cast ", cardToCast,
                    " for its " + costLabel + " cost."));
            log.info("Game {} - {} can't pay {} cost {} for {}",
                    gameData.id, player.getUsername(), costLabel, costStr, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        String prompt = "Choose a value for X to cast " + cardToCast.getName()
                + " for its " + costLabel + " cost (" + costStr + ").";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.AlternateCastXValueChoice(
                player.getId(), cardToCast.getId(), costStr, maxX, prompt, cardToCast.getName(), costLabel));
    }

    /**
     * Applies the announced X for an alternative-cost cast: charges the cost with that X and
     * puts the spell on the stack carrying X as the entry's numeric context. Re-prompts when the
     * pool still can't cover the chosen X (the cap was based on untapped sources).
     */
    public void completeAlternateCastXChoice(GameData gameData, Player player,
                                             PendingInteraction.AlternateCastXValueChoice interaction,
                                             int chosenX) {
        String playerName = player.getUsername();
        boolean castFromExile = "madness".equals(interaction.costLabel());
        List<Card> hand = castFromExile ? null : gameData.playerHands.get(player.getId());
        int cardIndex = -1;
        if (castFromExile) {
            if (gameData.findExiledCard(interaction.cardId()) == null) {
                gameLogService.append(gameData, GameLog.text(interaction.cardName() + " is no longer in exile."));
                log.info("Game {} - {} no longer in exile for madness cast", gameData.id, interaction.cardName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
        } else if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).getId().equals(interaction.cardId())) {
                    cardIndex = i;
                    break;
                }
            }
        }

        if (!castFromExile && cardIndex == -1) {
            gameLogService.append(gameData, GameLog.text(interaction.cardName() + " is no longer in hand."));
            log.info("Game {} - {} no longer in hand for {} cast", gameData.id, interaction.cardName(), interaction.costLabel());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card cardToCast = castFromExile
                ? gameData.findExiledCard(interaction.cardId()).card()
                : hand.get(cardIndex);
        if (cardToCast.isCastOnlyFromGraveyard()) {
            gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                    castFromExile ? " cannot be cast from exile." : " cannot be cast from hand."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        ManaCost cost = new ManaCost(interaction.manaCost());
        ManaPool pool = gameData.playerManaPools.get(player.getId());

        if (!cost.canPay(pool, chosenX)) {
            // Only worth re-prompting while untapped sources could still cover the choice —
            // otherwise the cast is simply abandoned instead of looping on the prompt.
            ManaPool potential = potentialManaService.buildVirtualManaPool(gameData, player.getId());
            if (!cost.canPay(potential, chosenX)) {
                gameLogService.append(gameData, GameLog.textCardText(
                        playerName + " cannot pay " + interaction.manaCost() + " to cast ",
                        cardToCast, " for its " + interaction.costLabel() + " cost."));
                log.info("Game {} - {} can't pay {} cost {} for {}", gameData.id, playerName,
                        interaction.costLabel(), interaction.manaCost(), cardToCast.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " can't pay " + interaction.manaCost() + " with X=" + chosenX + " for ",
                    cardToCast, " yet (tap mana sources, then choose X again)."));
            log.info("Game {} - {} cannot yet pay {} with X={} for {} — re-prompting",
                    gameData.id, playerName, interaction.manaCost(), chosenX, cardToCast.getName());
            beginAlternateCastXChoice(gameData, player, cardToCast, interaction.manaCost(), interaction.costLabel());
            return;
        }
        cost.pay(pool, chosenX);

        if (castFromExile) {
            gameData.removeFromExile(cardToCast.getId());
        } else {
            hand.remove(cardIndex);
        }
        castCardFromHandPayingAlternateCost(gameData, player, cardToCast, interaction.manaCost(),
                interaction.costLabel(), chosenX);
    }

    /**
     * Handles the madness "you may cast this for its madness cost" choice (CR 702.34b).
     * Pays {@link PendingMayAbility#manaCost()} then casts from exile, ignoring type timing.
     * Declining puts the card into its owner's graveyard.
     */
    public void handleMayCastForMadnessCost(GameData gameData, Player player, boolean accepted,
                                            PendingMayAbility ability) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();

        if (!accepted) {
            if (gameData.findExiledCard(cardToCast.getId()) != null) {
                gameData.removeFromExile(cardToCast.getId());
                graveyardService.addCardToGraveyard(gameData, player.getId(), cardToCast);
            }
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " declines to cast ", cardToCast, " for its madness cost."));
            log.info("Game {} - {} declines madness cast of {}", gameData.id, playerName, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.findExiledCard(cardToCast.getId()) == null) {
            gameLogService.append(gameData, GameLog.cardThen(cardToCast, " is no longer in exile."));
            log.info("Game {} - {} no longer in exile for madness cast", gameData.id, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (cardToCast.isCastOnlyFromGraveyard()) {
            gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                    " cannot be cast from exile."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        String costStr = ability.manaCost();
        if (costStr == null) {
            log.warn("Game {} - madness cast of {} has no cost on pending ability", gameData.id, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        ManaCost cost = new ManaCost(costStr);
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        if (cost.hasX()) {
            beginAlternateCastXChoice(gameData, player, cardToCast, costStr, "madness");
            return;
        }
        if (!cost.canPay(pool)) {
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " cannot pay " + costStr + " to cast ", cardToCast, " for its madness cost."));
            log.info("Game {} - {} can't pay madness cost {} for {}", gameData.id, playerName, costStr, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        cost.pay(pool);

        gameData.removeFromExile(cardToCast.getId());
        castCardFromHandPayingAlternateCost(gameData, player, cardToCast, costStr, "madness", 0);
    }

    /** Handles casting one Eldrazi spell from the controller's outside-the-game card pool. */
    public void handleCastFromOutsideGameChoice(GameData gameData, Player player, boolean accepted,
                                                PendingMayAbility ability) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();
        if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " declines to cast ", cardToCast, "."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        List<Card> sideboard = gameData.playerSideboards.get(player.getId());
        int cardIndex = -1;
        if (sideboard != null) {
            for (int i = 0; i < sideboard.size(); i++) {
                if (sideboard.get(i).getId().equals(cardToCast.getId())) {
                    cardIndex = i;
                    break;
                }
            }
        }
        if (cardIndex == -1) {
            gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                    " is no longer outside the game."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntryType spellType = switch (cardToCast.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case BATTLE -> StackEntryType.BATTLE_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            default -> throw new IllegalStateException("Unsupported card type: " + cardToCast.getType());
        };
        boolean isPermanentSpell = cardToCast.hasType(CardType.CREATURE)
                || cardToCast.hasType(CardType.ARTIFACT)
                || cardToCast.hasType(CardType.ENCHANTMENT)
                || cardToCast.hasType(CardType.PLANESWALKER);
        List<CardEffect> spellEffects = isPermanentSpell
                ? List.of()
                : new ArrayList<>(cardToCast.getEffects(EffectSlot.SPELL));

        if (EffectResolution.needsTarget(cardToCast)) {
            List<UUID> validTargets = buildValidSpellTargets(gameData, cardToCast, spellEffects);
            if (validTargets.isEmpty()) {
                gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                        " has no valid targets and stays outside the game."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            sideboard.remove(cardIndex);
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.LibraryCastSpellTarget(
                            cardToCast, player.getId(), spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                    "Choose a target for " + cardToCast.getName() + ".");
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " casts ", cardToCast, " without paying its mana cost — choosing target."));
            return;
        }

        sideboard.remove(cardIndex);
        gameData.stack.add(new StackEntry(
                spellType, cardToCast, player.getId(), cardToCast.getName(),
                spellEffects, 0, (UUID) null, null));
        gameData.recordSpellCast(player.getId(), cardToCast);
        gameData.priorityPassedBy.clear();
        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " casts ", cardToCast, " without paying its mana cost."));
        triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, player.getId(), false);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Handles the "may cast from hand without paying mana cost" choice (e.g. Counterlash).
     * Each eligible card gets its own PendingMayAbility; accepting one removes the rest.
     */
    public void handleMayCastFromHandWithoutPaying(GameData gameData, Player player, boolean accepted,
                                                    PendingMayAbility ability) {
        boolean revealCardOnDecline = ability.effects().stream()
                .filter(MayCastFromHandWithoutPayingManaCostEffect.class::isInstance)
                .map(MayCastFromHandWithoutPayingManaCostEffect.class::cast)
                .findFirst()
                .map(MayCastFromHandWithoutPayingManaCostEffect::revealCardOnDecline)
                .orElse(true);
        handleMayCastFromHandWithoutPaying(gameData, player, accepted, ability,
                MayCastFromHandWithoutPayingManaCostEffect.class, revealCardOnDecline, false);
    }

    public void handleMayCastFromHandWithoutPaying(GameData gameData, Player player, boolean accepted,
                                                    PendingMayAbility ability,
                                                    Class<? extends CardEffect> pendingEffectType,
                                                    boolean revealCardOnDecline,
                                                    boolean scryIfDeclined) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();

        if (!accepted) {
            if (scryIfDeclined) {
                queueScryFallback(gameData);
                gameLogService.append(gameData,
                        GameLog.text(playerName + " declines to cast a permanent spell from hand."));
                log.info("Game {} - {} declines all eligible permanent spells from hand", gameData.id,
                        playerName);
            } else if (revealCardOnDecline) {
                gameLogService.append(gameData,
                        GameLog.textCardText(playerName + " declines to cast ", cardToCast, "."));
                log.info("Game {} - {} declines to cast {} from hand", gameData.id,
                        playerName, cardToCast.getName());
            } else {
                gameLogService.append(gameData,
                        GameLog.text(playerName + " declines to cast the chosen card."));
                log.info("Game {} - {} declines to cast the chosen card from hand", gameData.id,
                        playerName);
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Verify the card is still in hand. Mindclaw Shaman casts from the targeted player's hand,
        // so the card is not necessarily in the choosing player's own hand.
        List<Card> hand = null;
        int cardIndex = -1;
        for (List<Card> candidate : gameData.playerHands.values()) {
            for (int i = 0; i < candidate.size(); i++) {
                if (candidate.get(i).getId().equals(cardToCast.getId())) {
                    hand = candidate;
                    cardIndex = i;
                    break;
                }
            }
            if (cardIndex != -1) break;
        }

        if (cardIndex == -1) {
            if (scryIfDeclined) {
                queueScryFallback(gameData);
            }
            gameLogService.append(gameData, GameLog.cardThen(cardToCast, " is no longer in hand."));
            log.info("Game {} - {} no longer in hand for cast-from-hand", gameData.id, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (cardToCast.isCastOnlyFromGraveyard()) {
            gameLogService.append(gameData, GameLog.cardThen(cardToCast,
                    " cannot be cast from hand."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Remove remaining may-cast-from-hand abilities (only cast one spell)
        gameData.pendingMayAbilities.removeIf(pma ->
                pma.effects().stream().anyMatch(pendingEffectType::isInstance));

        // Remove from hand and cast
        hand.remove(cardIndex);
        castCardFromHandWithoutPaying(gameData, player, cardToCast);
    }

    private void queueScryFallback(GameData gameData) {
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry != null) {
            pendingEntry.insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex, List.of(new ScryEffect(1)));
        }
    }

    private void castCardFromHandWithoutPaying(GameData gameData, Player player, Card card) {
        castCardFromHandPayingAlternateCost(gameData, player, card, null, null, 0);
    }

    private void castCardFromHandPayingAlternateCost(GameData gameData, Player player, Card card,
                                                     String paidCostDescription, String costLabel) {
        castCardFromHandPayingAlternateCost(gameData, player, card, paidCostDescription, costLabel, 0);
    }

    /**
     * Puts a card onto the stack as a cast spell, ignoring type-based timing.
     * {@code paidCostDescription} null means free ("without paying its mana cost"); otherwise
     * logs that the alternate cost was paid. {@code costLabel} is "miracle" / "madness" (or null for free).
     * {@code xValue} is the X announced for an alternative cost containing {X} (0 otherwise).
     */
    private void castCardFromHandPayingAlternateCost(GameData gameData, Player player, Card card,
                                                     String paidCostDescription, String costLabel,
                                                     int xValue) {
        UUID playerId = player.getId();
        String playerName = player.getUsername();
        String costPhrase;
        if (paidCostDescription == null) {
            costPhrase = " without paying its mana cost";
        } else {
            String label = costLabel != null ? costLabel : "alternate";
            costPhrase = " for its " + label + " cost (" + paidCostDescription + ")";
        }

        StackEntryType spellType = switch (card.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case BATTLE -> StackEntryType.BATTLE_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            default -> throw new IllegalStateException("Unsupported card type: " + card.getType());
        };

        // Permanent spells have empty SPELL effects — ETB is processed on battlefield entry
        boolean isPermanentSpell = card.hasType(CardType.CREATURE)
                || card.hasType(CardType.ARTIFACT)
                || card.hasType(CardType.ENCHANTMENT)
                || card.hasType(CardType.PLANESWALKER);
        List<CardEffect> spellEffects = isPermanentSpell
                ? List.of()
                : new ArrayList<>(card.getEffects(EffectSlot.SPELL));

        if (EffectResolution.needsTarget(card) || EffectResolution.needsSpellTarget(card)) {
            boolean castForMadnessCost = "madness".equals(costLabel);
            List<UUID> validTargets = buildValidSpellTargets(gameData, card, spellEffects, player.getId(),
                    xValue, castForMadnessCost);

            if (validTargets.isEmpty()) {
                // No valid targets — card goes to its owner's graveyard
                UUID ownerId = card.getOwnerId() != null ? card.getOwnerId() : playerId;
                graveyardService.addCardToGraveyard(gameData, ownerId, card);
                gameLogService.append(gameData, GameLog.cardThen(card, " has no valid targets."));
                log.info("Game {} - {} cast-from-hand has no valid targets", gameData.id, card.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.HandCastSpellTarget(card, playerId, spellEffects, spellType, xValue,
                            castForMadnessCost));
            playerInputService.beginPermanentChoice(gameData, playerId, validTargets,
                    "Choose a target for " + card.getName() + ".");

            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " casts ", card, costPhrase + " — choosing target."));
            log.info("Game {} - {} casts {} from hand, choosing target", gameData.id, playerName, card.getName());
            return; // Wait for target choice
        }

        // Non-targeted spell — put directly on stack
        StackEntry entry = new StackEntry(
                spellType, card, playerId, card.getName(),
                spellEffects, xValue, (UUID) null, null
        );
        entry.setMadness("madness".equals(costLabel));
        gameData.stack.add(entry);

        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " casts ", card, costPhrase + "."));
        log.info("Game {} - {} casts {} from hand{}", gameData.id, playerName, card.getName(), costPhrase);

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, false);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
