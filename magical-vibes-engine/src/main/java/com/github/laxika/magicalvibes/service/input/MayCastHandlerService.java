package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeOrExileEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final ExileService exileService;
    private final ExileFreeCastSupport exileFreeCastSupport;

    public void handleCastFromLibraryChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card cardToCast = ability.sourceCard();
        String playerName = player.getUsername();

        if (accepted) {
            List<Card> deck = gameData.playerDecks.get(player.getId());

            // Verify the card is still on top of the library
            if (deck.isEmpty() || !deck.getFirst().getId().equals(cardToCast.getId())) {
                String logEntry = cardToCast.getName() + " is no longer on top of the library.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
                        String logEntry = cardToCast.getName() + " has no valid targets.";
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                        log.info("Game {} - {} cast-from-library has no valid targets", gameData.id, cardToCast.getName());
                    } else {
                        gameData.interaction.setPermanentChoiceContext(
                                new PermanentChoiceContext.LibraryCastSpellTarget(cardToCast, player.getId(), spellEffects, spellType));
                        playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                                "Choose a target for " + cardToCast.getName() + ".");

                        String logEntry = playerName + " casts " + cardToCast.getName() + " without paying its mana cost — choosing target.";
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

                    String logEntry = playerName + " casts " + cardToCast.getName() + " without paying its mana cost.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                    log.info("Game {} - {} casts {} from library without paying mana", gameData.id, playerName, cardToCast.getName());

                    triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, player.getId(), false);
                }
            }
        } else {
            String logEntry = playerName + " declines to cast " + cardToCast.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
        boolean exileIfNotPlayed = ability.effects().stream()
                .filter(e -> e instanceof RevealTopCardMayPlayFreeOrExileEffect)
                .map(e -> ((RevealTopCardMayPlayFreeOrExileEffect) e).exileIfNotPlayed())
                .findFirst().orElse(true);

        if (!accepted) {
            if (exileIfNotPlayed) {
                // Declined — exile the card from library
                exileTopCardFromLibrary(gameData, player.getId(), deck, cardToPlay, playerName);
            } else {
                // Declined — the card stays on top of the library
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " declines to play " + cardToPlay.getName() + "."));
                log.info("Game {} - {} declines to play {}, stays on top", gameData.id, playerName, cardToPlay.getName());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Verify the card is still on top of the library
        if (deck.isEmpty() || !deck.getFirst().getId().equals(cardToPlay.getId())) {
            String logEntry = cardToPlay.getName() + " is no longer on top of the library.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} no longer on top of library for play-from-library", gameData.id, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (cardToPlay.hasType(CardType.LAND)) {
            // Play the land: put onto battlefield, increment land play count
            deck.removeFirst();
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), new Permanent(cardToPlay));
            gameData.landsPlayedThisTurn.merge(player.getId(), 1, Integer::sum);

            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.playerPlays(playerName, cardToPlay, " without paying its mana cost."));
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

            if (EffectResolution.needsTarget(cardToPlay)) {
                // Targeted spell — need to choose target before putting on stack
                List<UUID> validTargets = buildValidSpellTargets(gameData, cardToPlay, spellEffects);

                if (validTargets.isEmpty()) {
                    if (exileIfNotPlayed) {
                        // No valid targets — exile the card instead
                        exileService.exileCard(gameData, player.getId(), cardToPlay);
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardToPlay.getName() + " has no valid targets and is exiled."));
                        log.info("Game {} - {} play-from-library has no valid targets, exiled", gameData.id, cardToPlay.getName());
                    } else {
                        // No valid targets — return the card to the top of the library
                        deck.addFirst(cardToPlay);
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardToPlay.getName() + " has no valid targets and stays on top of the library."));
                        log.info("Game {} - {} play-from-library has no valid targets, stays on top", gameData.id, cardToPlay.getName());
                    }
                } else {
                    gameData.interaction.setPermanentChoiceContext(
                            new PermanentChoiceContext.LibraryCastSpellTarget(cardToPlay, player.getId(), spellEffects, spellType));
                    playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                            "Choose a target for " + cardToPlay.getName() + ".");

                    String logEntry = playerName + " casts " + cardToPlay.getName() + " without paying its mana cost — choosing target.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

                String logEntry = playerName + " casts " + cardToPlay.getName() + " without paying its mana cost.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

    private void exileTopCardFromLibrary(GameData gameData, UUID playerId, List<Card> deck, Card card, String playerName) {
        if (!deck.isEmpty() && deck.getFirst().getId().equals(card.getId())) {
            deck.removeFirst();
        }
        exileService.exileCard(gameData, playerId, card);
        String logEntry = playerName + " exiles " + card.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
            String logEntry = cardToCast.getName() + " can't be cast from the graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            accepted = false;
        }

        if (accepted) {
            // Verify the card is still in a graveyard matching the scope
            Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, cardToCast.getId());
            if (graveyardCard == null) {
                String logEntry = cardToCast.getName() + " is no longer in the graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
                            String logEntry = cardToCast.getName() + " has no valid targets.";
                            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                            log.info("Game {} - {} cast-from-graveyard has no valid targets", gameData.id, cardToCast.getName());
                        } else {
                            gameData.interaction.setPermanentChoiceContext(
                                    new PermanentChoiceContext.GraveyardCastSpellTarget(cardToCast, player.getId(), spellEffects, spellType));
                            playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                                    "Choose a target for " + cardToCast.getName() + ".");

                            String logEntry = playerName + " casts " + cardToCast.getName() + castLabel + " — choosing target.";
                            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                            log.info("Game {} - {} casts {} from graveyard, choosing target", gameData.id, playerName, cardToCast.getName());
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

                        String logEntry = playerName + " casts " + cardToCast.getName() + castLabel + ".";
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                        log.info("Game {} - {} casts {} from graveyard", gameData.id, playerName, cardToCast.getName());

                        triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, player.getId(), false);
                    }
                }
            }
        } else {
            String logEntry = playerName + " declines to cast " + cardToCast.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
            String logEntry = playerName + " declines to play " + cardToPlay.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines to play {} from graveyard", gameData.id, playerName, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Non-land cards can't be cast from graveyards if a permanent forbids it (e.g. Ashes of the Abhorrent).
        if (!cardToPlay.hasType(CardType.LAND)
                && !gameQueryService.canPlayersCastSpellsFromZone(gameData, Zone.GRAVEYARD)) {
            String logEntry = cardToPlay.getName() + " can't be cast from the graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Verify the card is still in the controller's own graveyard and matches the filter.
        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, cardToPlay.getId());
        UUID graveyardOwnerId = graveyardCard == null
                ? null : gameQueryService.findGraveyardOwnerById(gameData, cardToPlay.getId());
        if (graveyardCard == null || graveyardOwnerId == null || !graveyardOwnerId.equals(player.getId())
                || !predicateEvaluationService.matchesCardPredicate(graveyardCard, effect.filter(), null)) {
            String logEntry = cardToPlay.getName() + " is no longer a legal target in your graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} no longer a legal graveyard target for play-from-graveyard", gameData.id, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardToPlay.getId());

        if (cardToPlay.hasType(CardType.LAND)) {
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), new Permanent(cardToPlay));
            gameData.landsPlayedThisTurn.merge(player.getId(), 1, Integer::sum);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.playerPlays(playerName, cardToPlay,
                    " from their graveyard without paying its mana cost."));
            log.info("Game {} - {} plays {} (land) from graveyard", gameData.id, playerName, cardToPlay.getName());

            battlefieldEntryService.processCreatureETBEffects(gameData, player.getId(), cardToPlay, null, false);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntryType spellType = switch (cardToPlay.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
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
                String logEntry = cardToPlay.getName() + " has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} play-from-graveyard has no valid targets", gameData.id, cardToPlay.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.GraveyardCastSpellTarget(cardToPlay, player.getId(), spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, player.getId(), validTargets,
                    "Choose a target for " + cardToPlay.getName() + ".");

            gameBroadcastService.logAndBroadcast(gameData, GameLog.playerPlays(playerName, cardToPlay,
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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.playerPlays(playerName, cardToPlay,
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " declines to play " + cardToPlay.getName() + "."));
            log.info("Game {} - {} declines to play imprinted {}", gameData.id, playerName, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Verify the card is still in exile (nothing else has moved it out).
        if (gameData.findExiledCard(cardToPlay.getId()) == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardToPlay.getName() + " is no longer in exile."));
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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " turns the exiled card face up: " + cardToPlay.getName() + "."));

        if (cardToPlay.hasType(CardType.LAND)) {
            gameData.removeFromExile(cardToPlay.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), new Permanent(cardToPlay));
            gameData.landsPlayedThisTurn.merge(player.getId(), 1, Integer::sum);
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.playerPlays(playerName, cardToPlay, " without paying its mana cost."));
            battlefieldEntryService.processCreatureETBEffects(gameData, player.getId(), cardToPlay, null, false);
            log.info("Game {} - {} plays imprinted land {} from exile", gameData.id, playerName, cardToPlay.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Non-land: cast from exile without paying (handles targeting, the stack, and cast triggers).
        exileFreeCastSupport.castFromExileWithoutPaying(gameData, player, cardToPlay.getId());
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
            String logEntry = playerName + " declines to cast " + cardToCast.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines to cast {} from hand (Counterlash)", gameData.id, playerName, cardToCast.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Verify the card is still in hand
        List<Card> hand = gameData.playerHands.get(player.getId());
        int cardIndex = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getId().equals(cardToCast.getId())) {
                cardIndex = i;
                break;
            }
        }

        if (cardIndex == -1) {
            String logEntry = cardToCast.getName() + " is no longer in hand.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
        UUID playerId = player.getId();
        String playerName = player.getUsername();

        StackEntryType spellType = switch (card.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
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
                // No valid targets — card goes to graveyard
                graveyardService.addCardToGraveyard(gameData, playerId, card);
                String logEntry = card.getName() + " has no valid targets.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} cast-from-hand has no valid targets", gameData.id, card.getName());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.HandCastSpellTarget(card, playerId, spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, playerId, validTargets,
                    "Choose a target for " + card.getName() + ".");

            String logEntry = playerName + " casts " + card.getName() + " without paying its mana cost — choosing target.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} casts {} from hand, choosing target", gameData.id, playerName, card.getName());
            return; // Wait for target choice
        }

        // Non-targeted spell — put directly on stack
        gameData.stack.add(new StackEntry(
                spellType, card, playerId, card.getName(),
                spellEffects, 0, (UUID) null, null
        ));

        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();

        String logEntry = playerName + " casts " + card.getName() + " without paying its mana cost.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} casts {} from hand without paying mana", gameData.id, playerName, card.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, false);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
