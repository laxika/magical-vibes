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
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastForMadnessCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastForMiracleCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
                deck.removeFirst();

                List<CardEffect> spellEffects = new ArrayList<>(cardToCast.getEffects(EffectSlot.SPELL));
                StackEntryType spellType = cardToCast.hasType(CardType.INSTANT)
                        ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;

                if (EffectResolution.needsTarget(cardToCast)) {
                    // Targeted spell — need to choose target before putting on stack
                    List<UUID> validTargets = buildValidSpellTargets(gameData, cardToCast, spellEffects);

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
     * If declined: the card goes to the effect's not-played destination (exile, bottom of library,
     * or stays on top).
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

        if (cardToPlay.hasType(CardType.LAND)) {
            // Play the land: put onto battlefield, increment land play count
            deck.removeFirst();
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), new Permanent(cardToPlay));
            gameData.landsPlayedThisTurn.merge(player.getId(), 1, Integer::sum);

            gameLogService.append(gameData,
                    GameLog.playerPlays(playerName, cardToPlay, " without paying its mana cost."));
            log.info("Game {} - {} plays {} (land) from library", gameData.id, playerName, cardToPlay.getName());

            battlefieldEntryService.processCreatureETBEffects(gameData, player.getId(), cardToPlay, null, false);
            triggerCollectionService.checkControllerPlaysLandTriggers(gameData, player.getId(), cardToPlay);
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

            if (EffectResolution.needsTarget(cardToPlay)) {
                // Targeted spell — need to choose target before putting on stack
                List<UUID> validTargets = buildValidSpellTargets(gameData, cardToPlay, spellEffects);

                if (validTargets.isEmpty()) {
                    switch (notPlayedDestination) {
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
     * Builds a list of valid target UUIDs for a targeted spell, including both permanents and players
     * as appropriate based on the spell's effects and target filter.
     */
    List<UUID> buildValidSpellTargets(GameData gameData, Card card, List<CardEffect> spellEffects) {
        List<UUID> validTargets = new ArrayList<>();
        boolean canTargetPermanent = spellEffects.stream().anyMatch(e -> e.targetSpec().category().includesPermanents())
                || card.getTargetFilter() instanceof PermanentPredicateTargetFilter;
        if (canTargetPermanent) {
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (card.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                        if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                            validTargets.add(p.getId());
                        }
                    } else if (gameQueryService.isCreature(gameData, p)) {
                        validTargets.add(p.getId());
                    }
                }
            }
        }
        boolean canTargetPlayer = spellEffects.stream().anyMatch(e -> e.targetSpec().category().includesPlayers());
        if (canTargetPlayer) {
            validTargets.addAll(gameData.orderedPlayerIds);
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
        if (accepted && !gameQueryService.canPlayersCastSpellsFromZone(gameData, Zone.GRAVEYARD)) {
            
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
                if (!validScope) {
                    
                    gameLogService.append(gameData, GameLog.cardThen(cardToCast, " is no longer in a valid graveyard."));
                    log.info("Game {} - {} not in valid graveyard (scope={})", gameData.id, cardToCast.getName(), scope);
                } else {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, cardToCast.getId());

                    List<CardEffect> spellEffects = new ArrayList<>(cardToCast.getEffects(EffectSlot.SPELL));
                    StackEntryType spellType = cardToCast.hasType(CardType.INSTANT)
                            ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;

                    if (EffectResolution.needsTarget(cardToCast)) {
                        // Targeted spell — need to choose target before putting on stack
                        List<UUID> validTargets = buildValidSpellTargets(gameData, cardToCast, spellEffects);

                        if (validTargets.isEmpty()) {
                            // No valid targets — card goes to owner's graveyard
                            graveyardService.addCardToGraveyard(gameData, graveyardOwnerId, cardToCast);
                            
                            gameLogService.append(gameData, GameLog.cardThen(cardToCast, " has no valid targets."));
                            log.info("Game {} - {} cast-from-graveyard has no valid targets", gameData.id, cardToCast.getName());
                        } else {
                            gameData.interaction.setPermanentChoiceContext(
                                    new PermanentChoiceContext.GraveyardCastSpellTarget(cardToCast, player.getId(),
                                            spellEffects, spellType, castEffect.exileInsteadOfGraveyard()));
                            playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                                    "Choose a target for " + cardToCast.getName() + ".");

                            
                            gameLogService.append(gameData, GameLog.cardThen(cardToCast, " is no longer in a valid graveyard."));
                            log.info("Game {} - {} casts {} from graveyard, choosing target", gameData.id, playerName, cardToCast.getName());
                            return; // Wait for target choice
                        }
                    } else {
                        // Non-targeted spell — put directly on stack
                        StackEntry freeCast = new StackEntry(
                                spellType, cardToCast, player.getId(), cardToCast.getName(),
                                spellEffects, 0, (UUID) null, null
                        );
                        freeCast.setExileInsteadOfGraveyard(castEffect.exileInsteadOfGraveyard());
                        gameData.stack.add(freeCast);

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

    /**
     * Handles the "you may play target [type] card from your graveyard without paying its mana cost"
     * choice (e.g. Horde of Notions). If accepted: a land is put onto the battlefield, any other card
     * is cast without paying its mana cost. Restricted to the controller's own graveyard.
     */
    public void handlePlayFromGraveyardChoice(GameData gameData, Player player, boolean accepted,
                                              PendingMayAbility ability,
                                              PlayTargetCardFromGraveyardWithoutPayingManaCostEffect effect) {
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
                && !gameQueryService.canPlayersCastSpellsFromZone(gameData, Zone.GRAVEYARD)) {
            
            gameLogService.append(gameData, GameLog.cardThen(cardToPlay, " can't be cast from the graveyard."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Verify the card is still in the controller's own graveyard and matches the filter.
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, cardToPlay.getId());
        UUID graveyardOwnerId = graveyardCard == null
                ? null : gameQueryService.findGraveyardOwnerById(gameData, cardToPlay.getId());
        if (graveyardCard == null || graveyardOwnerId == null || !graveyardOwnerId.equals(player.getId())
                || !predicateEvaluationService.matchesCardPredicate(graveyardCard, effect.filter(), null)) {
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

            battlefieldEntryService.processCreatureETBEffects(gameData, player.getId(), cardToPlay, null, false);
            triggerCollectionService.checkControllerPlaysLandTriggers(gameData, player.getId(), cardToPlay);
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
                || cardToPlay.hasType(CardType.PLANESWALKER);
        List<CardEffect> spellEffects = isPermanentSpell
                ? List.of()
                : new ArrayList<>(cardToPlay.getEffects(EffectSlot.SPELL));

        if (EffectResolution.needsTarget(cardToPlay)) {
            List<UUID> validTargets = buildValidSpellTargets(gameData, cardToPlay, spellEffects);

            if (validTargets.isEmpty()) {
                // No valid targets — card goes back to owner's graveyard.
                graveyardService.addCardToGraveyard(gameData, player.getId(), cardToPlay);
                gameLogService.append(gameData, GameLog.cardThen(cardToPlay, " has no valid targets."));
                log.info("Game {} - {} play-from-graveyard has no valid targets", gameData.id, cardToPlay.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.GraveyardCastSpellTarget(cardToPlay, player.getId(), spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                    "Choose a target for " + cardToPlay.getName() + ".");

            gameLogService.append(gameData, GameLog.playerPlays(playerName, cardToPlay,
                    " from their graveyard without paying its mana cost — choosing target."));
            log.info("Game {} - {} casts {} from graveyard, choosing target", gameData.id, playerName, cardToPlay.getName());
            return; // Wait for target choice
        }

        gameData.stack.add(new StackEntry(
                spellType, cardToPlay, player.getId(), cardToPlay.getName(),
                spellEffects, 0, (UUID) null, null
        ));
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
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), new Permanent(cardToPlay));
            gameData.landsPlayedThisTurn.merge(player.getId(), 1, Integer::sum);
            gameLogService.append(gameData,
                    GameLog.playerPlays(playerName, cardToPlay, " without paying its mana cost."));
            battlefieldEntryService.processCreatureETBEffects(gameData, player.getId(), cardToPlay, null, false);
            triggerCollectionService.checkControllerPlaysLandTriggers(gameData, player.getId(), cardToPlay);
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
        List<Card> hand = gameData.playerHands.get(player.getId());
        int cardIndex = -1;
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).getId().equals(interaction.cardId())) {
                    cardIndex = i;
                    break;
                }
            }
        }

        if (cardIndex == -1) {
            gameLogService.append(gameData, GameLog.text(interaction.cardName() + " is no longer in hand."));
            log.info("Game {} - {} no longer in hand for {} cast", gameData.id, interaction.cardName(), interaction.costLabel());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card cardToCast = hand.get(cardIndex);
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

        hand.remove(cardIndex);
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

        String costStr = ability.manaCost();
        if (costStr == null) {
            log.warn("Game {} - madness cast of {} has no cost on pending ability", gameData.id, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        ManaCost cost = new ManaCost(costStr);
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        if (!cost.canPay(pool)) {
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " cannot pay " + costStr + " to cast ", cardToCast, " for its madness cost."));
            log.info("Game {} - {} can't pay madness cost {} for {}", gameData.id, playerName, costStr, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        cost.pay(pool);

        gameData.removeFromExile(cardToCast.getId());
        castCardFromHandPayingAlternateCost(gameData, player, cardToCast, costStr, "madness");
    }

    /**
     * Handles the "may cast from hand without paying mana cost" choice (e.g. Counterlash).
     * Each eligible card gets its own PendingMayAbility; accepting one removes the rest.
     */
    public void handleMayCastFromHandWithoutPaying(GameData gameData, Player player, boolean accepted,
                                                    PendingMayAbility ability) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();

        if (!accepted) {
            
            gameLogService.append(gameData, GameLog.textCardText(playerName + " declines to cast " , cardToCast, "."));
            log.info("Game {} - {} declines to cast {} from hand (Counterlash)", gameData.id, playerName, cardToCast.getName());
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
            gameLogService.append(gameData, GameLog.cardThen(cardToCast, " is no longer in hand."));
            log.info("Game {} - {} no longer in hand for cast-from-hand", gameData.id, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Remove remaining may-cast-from-hand abilities (only cast one spell)
        gameData.pendingMayAbilities.removeIf(pma ->
                pma.effects().stream().anyMatch(e -> e instanceof MayCastFromHandWithoutPayingManaCostEffect));

        // Remove from hand and cast
        hand.remove(cardIndex);
        castCardFromHandWithoutPaying(gameData, player, cardToCast);
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

        if (EffectResolution.needsTarget(card)) {
            List<UUID> validTargets = buildValidSpellTargets(gameData, card, spellEffects);

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
                    new PermanentChoiceContext.HandCastSpellTarget(card, playerId, spellEffects, spellType, xValue));
            playerInputService.beginPermanentChoice(gameData, playerId, validTargets,
                    "Choose a target for " + card.getName() + ".");

            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " casts ", card, costPhrase + " — choosing target."));
            log.info("Game {} - {} casts {} from hand, choosing target", gameData.id, playerName, card.getName());
            return; // Wait for target choice
        }

        // Non-targeted spell — put directly on stack
        gameData.stack.add(new StackEntry(
                spellType, card, playerId, card.getName(),
                spellEffects, xValue, (UUID) null, null
        ));

        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " casts ", card, costPhrase + "."));
        log.info("Game {} - {} casts {} from hand{}", gameData.id, playerName, card.getName(), costPhrase);

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, false);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
