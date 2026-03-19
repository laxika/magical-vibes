package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayCastHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final ExileService exileService;

    public void handleCastFromLibraryChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();

        if (accepted) {
            List<Card> deck = gameData.playerDecks.get(player.getId());

            // Verify the card is still on top of the library
            if (deck.isEmpty() || !deck.getFirst().getId().equals(cardToCast.getId())) {
                String logEntry = cardToCast.getName() + " is no longer on top of the library.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} no longer on top of library for cast-from-library", gameData.id, cardToCast.getName());
            } else {
                deck.removeFirst();

                List<CardEffect> spellEffects = new ArrayList<>(cardToCast.getEffects(EffectSlot.SPELL));
                StackEntryType spellType = cardToCast.hasType(CardType.INSTANT)
                        ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;

                if (cardToCast.isNeedsTarget()) {
                    // Targeted spell — need to choose target before putting on stack
                    List<UUID> validTargets = new ArrayList<>();
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                        if (battlefield == null) continue;
                        for (Permanent p : battlefield) {
                            if (cardToCast.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                                if (gameQueryService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                                    validTargets.add(p.getId());
                                }
                            } else if (gameQueryService.isCreature(gameData, p)) {
                                validTargets.add(p.getId());
                            }
                        }
                    }
                    boolean canTargetPlayer = spellEffects.stream().anyMatch(CardEffect::canTargetPlayer);
                    if (canTargetPlayer) {
                        validTargets.addAll(gameData.orderedPlayerIds);
                    }

                    if (validTargets.isEmpty()) {
                        // No valid targets — spell can't be cast, put card back on top of library
                        deck.addFirst(cardToCast);
                        String logEntry = cardToCast.getName() + " has no valid targets.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} cast-from-library has no valid targets", gameData.id, cardToCast.getName());
                    } else {
                        gameData.interaction.setPermanentChoiceContext(
                                new PermanentChoiceContext.LibraryCastSpellTarget(cardToCast, player.getId(), spellEffects, spellType));
                        playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                                "Choose a target for " + cardToCast.getName() + ".");

                        String logEntry = playerName + " casts " + cardToCast.getName() + " without paying its mana cost — choosing target.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} casts {} from library, choosing target", gameData.id, playerName, cardToCast.getName());
                        return; // Wait for target choice
                    }
                } else {
                    // Non-targeted spell — put directly on stack
                    gameData.stack.add(new StackEntry(
                            spellType, cardToCast, player.getId(), cardToCast.getName(),
                            spellEffects, 0, (UUID) null, null
                    ));

                    gameData.spellsCastThisTurn.merge(player.getId(), 1, Integer::sum);
                    gameData.priorityPassedBy.clear();

                    String logEntry = playerName + " casts " + cardToCast.getName() + " without paying its mana cost.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} casts {} from library without paying mana", gameData.id, playerName, cardToCast.getName());

                    triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, player.getId(), false);
                }
            }
        } else {
            String logEntry = playerName + " declines to cast " + cardToCast.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to cast {} from library", gameData.id, playerName, cardToCast.getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Handles the "may play from library or exile" choice (e.g. Djinn of Wishes).
     * If accepted: play the card (land → battlefield, spell → stack without paying mana cost).
     * If declined: exile the card.
     */
    public void handlePlayFromLibraryOrExileChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card cardToPlay = ability.sourceCard();
        String playerName = player.getUsername();
        List<Card> deck = gameData.playerDecks.get(player.getId());

        if (!accepted) {
            // Declined — exile the card from library
            exileTopCardFromLibrary(gameData, player.getId(), deck, cardToPlay, playerName);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Verify the card is still on top of the library
        if (deck.isEmpty() || !deck.getFirst().getId().equals(cardToPlay.getId())) {
            String logEntry = cardToPlay.getName() + " is no longer on top of the library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} no longer on top of library for play-from-library", gameData.id, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (cardToPlay.hasType(CardType.LAND)) {
            // Play the land: put onto battlefield, increment land play count
            deck.removeFirst();
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), new Permanent(cardToPlay));
            gameData.landsPlayedThisTurn.merge(player.getId(), 1, Integer::sum);

            String logEntry = playerName + " plays " + cardToPlay.getName() + " without paying its mana cost.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} plays {} (land) from library", gameData.id, playerName, cardToPlay.getName());

            battlefieldEntryService.processCreatureETBEffects(gameData, player.getId(), cardToPlay, null, false);
        } else {
            // Cast the spell without paying its mana cost
            deck.removeFirst();

            StackEntryType spellType = switch (cardToPlay.getType()) {
                case CREATURE -> StackEntryType.CREATURE_SPELL;
                case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
                case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
                case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
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

            if (cardToPlay.isNeedsTarget()) {
                // Targeted spell — need to choose target before putting on stack
                List<UUID> validTargets = new ArrayList<>();
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (cardToPlay.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                            if (gameQueryService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                                validTargets.add(p.getId());
                            }
                        } else if (gameQueryService.isCreature(gameData, p)) {
                            validTargets.add(p.getId());
                        }
                    }
                }
                boolean canTargetPlayer = spellEffects.stream().anyMatch(CardEffect::canTargetPlayer);
                if (canTargetPlayer) {
                    validTargets.addAll(gameData.orderedPlayerIds);
                }

                if (validTargets.isEmpty()) {
                    // No valid targets — exile the card instead
                    exileService.exileCard(gameData, player.getId(), cardToPlay);
                    String logEntry = cardToPlay.getName() + " has no valid targets and is exiled.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} play-from-library has no valid targets, exiled", gameData.id, cardToPlay.getName());
                } else {
                    gameData.interaction.setPermanentChoiceContext(
                            new PermanentChoiceContext.LibraryCastSpellTarget(cardToPlay, player.getId(), spellEffects, spellType));
                    playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                            "Choose a target for " + cardToPlay.getName() + ".");

                    String logEntry = playerName + " casts " + cardToPlay.getName() + " without paying its mana cost — choosing target.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} casts {} from library, choosing target", gameData.id, playerName, cardToPlay.getName());
                    return; // Wait for target choice
                }
            } else {
                // Non-targeted spell — put directly on stack
                gameData.stack.add(new StackEntry(
                        spellType, cardToPlay, player.getId(), cardToPlay.getName(),
                        spellEffects, 0, (UUID) null, null
                ));

                gameData.spellsCastThisTurn.merge(player.getId(), 1, Integer::sum);
                gameData.priorityPassedBy.clear();

                String logEntry = playerName + " casts " + cardToPlay.getName() + " without paying its mana cost.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} casts {} from library without paying mana", gameData.id, playerName, cardToPlay.getName());

                triggerCollectionService.checkSpellCastTriggers(gameData, cardToPlay, player.getId(), false);
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void exileTopCardFromLibrary(GameData gameData, UUID playerId, List<Card> deck, Card card, String playerName) {
        if (!deck.isEmpty() && deck.getFirst().getId().equals(card.getId())) {
            deck.removeFirst();
        }
        exileService.exileCard(gameData, playerId, card);
        String logEntry = playerName + " exiles " + card.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} exiles {} from library", gameData.id, playerName, card.getName());
    }

    public void handleCastFromGraveyardChoice(GameData gameData, Player player, boolean accepted,
                                               PendingMayAbility ability,
                                               CastTargetInstantOrSorceryFromGraveyardEffect castEffect) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();
        GraveyardSearchScope scope = castEffect.scope();
        String castLabel = castEffect.withoutPayingManaCost() ? " without paying its mana cost" : "";

        if (accepted) {
            // Verify the card is still in a graveyard matching the scope
            Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, cardToCast.getId());
            if (graveyardCard == null) {
                String logEntry = cardToCast.getName() + " is no longer in the graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} no longer in graveyard for cast-from-graveyard", gameData.id, cardToCast.getName());
            } else {
                UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardToCast.getId());
                boolean validScope = graveyardOwnerId != null && switch (scope) {
                    case OPPONENT_GRAVEYARD -> !graveyardOwnerId.equals(player.getId());
                    case CONTROLLERS_GRAVEYARD -> graveyardOwnerId.equals(player.getId());
                    case ALL_GRAVEYARDS -> true;
                };
                if (!validScope) {
                    String logEntry = cardToCast.getName() + " is no longer in a valid graveyard.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} not in valid graveyard (scope={})", gameData.id, cardToCast.getName(), scope);
                } else {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, cardToCast.getId());

                    List<CardEffect> spellEffects = new ArrayList<>(cardToCast.getEffects(EffectSlot.SPELL));
                    StackEntryType spellType = cardToCast.hasType(CardType.INSTANT)
                            ? StackEntryType.INSTANT_SPELL : StackEntryType.SORCERY_SPELL;

                    if (cardToCast.isNeedsTarget()) {
                        // Targeted spell — need to choose target before putting on stack
                        List<UUID> validTargets = new ArrayList<>();
                        for (UUID pid : gameData.orderedPlayerIds) {
                            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                            if (battlefield == null) continue;
                            for (Permanent p : battlefield) {
                                if (cardToCast.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                                    if (gameQueryService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                                        validTargets.add(p.getId());
                                    }
                                } else if (gameQueryService.isCreature(gameData, p)) {
                                    validTargets.add(p.getId());
                                }
                            }
                        }
                        boolean canTargetPlayer = spellEffects.stream().anyMatch(CardEffect::canTargetPlayer);
                        if (canTargetPlayer) {
                            validTargets.addAll(gameData.orderedPlayerIds);
                        }

                        if (validTargets.isEmpty()) {
                            // No valid targets — card goes to owner's graveyard
                            graveyardService.addCardToGraveyard(gameData, graveyardOwnerId, cardToCast);
                            String logEntry = cardToCast.getName() + " has no valid targets.";
                            gameBroadcastService.logAndBroadcast(gameData, logEntry);
                            log.info("Game {} - {} cast-from-graveyard has no valid targets", gameData.id, cardToCast.getName());
                        } else {
                            gameData.interaction.setPermanentChoiceContext(
                                    new PermanentChoiceContext.GraveyardCastSpellTarget(cardToCast, player.getId(), spellEffects, spellType));
                            playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                                    "Choose a target for " + cardToCast.getName() + ".");

                            String logEntry = playerName + " casts " + cardToCast.getName() + castLabel + " — choosing target.";
                            gameBroadcastService.logAndBroadcast(gameData, logEntry);
                            log.info("Game {} - {} casts {} from graveyard, choosing target", gameData.id, playerName, cardToCast.getName());
                            return; // Wait for target choice
                        }
                    } else {
                        // Non-targeted spell — put directly on stack
                        gameData.stack.add(new StackEntry(
                                spellType, cardToCast, player.getId(), cardToCast.getName(),
                                spellEffects, 0, (UUID) null, null
                        ));

                        gameData.spellsCastThisTurn.merge(player.getId(), 1, Integer::sum);
                        gameData.priorityPassedBy.clear();

                        String logEntry = playerName + " casts " + cardToCast.getName() + castLabel + ".";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} casts {} from graveyard", gameData.id, playerName, cardToCast.getName());

                        triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, player.getId(), false);
                    }
                }
            }
        } else {
            String logEntry = playerName + " declines to cast " + cardToCast.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to cast {} from graveyard", gameData.id, playerName, cardToCast.getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
