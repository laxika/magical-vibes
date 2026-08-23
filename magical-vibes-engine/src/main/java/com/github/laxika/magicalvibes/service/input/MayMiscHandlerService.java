package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.PendingGemstoneCavernsChoice;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingSphinxAmbassadorChoice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCounterTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.MulliganService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.UntapLockReleaseService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.EquipSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.SearchLibraryEffectHandler;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayMiscHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final MulliganService mulliganService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final EquipSupport equipSupport;
    private final AuraAttachmentService auraAttachmentService;
    private final CreatureControlService creatureControlService;
    private final UntapLockReleaseService untapLockReleaseService;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;
    @Autowired @Lazy
    private PermanentCounterSupport permanentCounterSupport;
    // @Lazy to break circular dependency:
    // MayMiscHandlerService → TriggerCollectionService → TriggeredAbilityQueueService → PlayerInputService → MayAbilityHandlerService → MayMiscHandlerService
    @Autowired @Lazy
    private TriggerCollectionService triggerCollectionService;
    @Autowired @Lazy
    private GraveyardService graveyardService;
    @Autowired @Lazy
    private StepTriggerService stepTriggerService;

    /** Setter for manual (non-Spring) construction (tests, AI simulator). */
    public void setTriggerCollectionService(TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
    }

    public void setGraveyardService(GraveyardService graveyardService) {
        this.graveyardService = graveyardService;
    }

    public void setStepTriggerService(StepTriggerService stepTriggerService) {
        this.stepTriggerService = stepTriggerService;
    }

    public void handleEquipmentAttachChoice(GameData gameData, Player player, boolean accepted,
                                             UUID equipId, UUID targetId) {
        gameData.interaction.clearPendingEquipmentAttach();
        if (accepted) {
            Permanent equipPerm = gameQueryService.findPermanentById(gameData, equipId);
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
            UUID attachmentControllerId = equipPerm == null
                    ? null : gameQueryService.findPermanentController(gameData, equipPerm.getId());
            boolean canAttach = equipPerm != null && targetPerm != null
                    && (equipPerm.getCard().isAura()
                    ? auraAttachmentService.canEnchant(
                            gameData, equipPerm.getCard(), attachmentControllerId, targetPerm)
                    : equipSupport.canAttachEquipment(gameData, equipPerm, targetPerm));
            if (canAttach) {
                UUID oldAttachedTo = equipPerm.getAttachedTo();
                gameData.expireFloatingEffectsForUnattachedSource(equipPerm.getId());
                equipPerm.setAttachedTo(targetPerm.getId());
                // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
                equipPerm.setTimestamp(gameData.nextTimestamp());
                equipSupport.applySacrificeOnUnattachIfNeeded(
                        gameData, equipPerm, oldAttachedTo, targetPerm.getId());
                if (!equipPerm.getCard().isAura()) {
                    equipSupport.expireAttachedCopyEffects(gameData, equipPerm);
                }
                equipPerm.setAttachedTo(targetPerm.getId());
                // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
                equipPerm.setTimestamp(gameData.nextTimestamp());
                if (!equipPerm.getCard().isAura()) {
                    equipSupport.notifyEquipmentAttached(gameData, equipPerm, oldAttachedTo);
                }
                
                gameLogService.append(gameData, GameLog.cardTextCard(equipPerm.getCard(), " is attached to ", targetPerm.getCard(), "."));
                log.info("Game {} - {} attached to {}", gameData.id, equipPerm.getCard().getName(), targetPerm.getCard().getName());
            }
        } else {
            String declineLog = player.getUsername() + " declines the attachment.";
            gameLogService.append(gameData, GameLog.text(declineLog));
            log.info("Game {} - {} declines equipment attachment", gameData.id, player.getUsername());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleMayNotUntapChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card sourceCard = ability.sourceCard();
        UUID controllerId = ability.controllerId();

        // Find the permanent on the battlefield by Card identity
        Permanent sourcePermanent = null;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(sourceCard.getId())) {
                    sourcePermanent = p;
                    break;
                }
            }
        }

        if (accepted && sourcePermanent != null && !gameQueryService.cantBecomeUntapped(gameData, sourcePermanent)) {
            sourcePermanent.untap();
            // A "for as long as this stays tapped" control effect (Seasinger) ends on untap.
            creatureControlService.onSourceUntapped(gameData, sourcePermanent);
            // Giant Oyster: the -1/-1 counters its untap lock accrued go away with the lock.
            untapLockReleaseService.releaseUntapLocks(gameData, sourcePermanent);
            
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " untaps " , sourceCard, "."));
            log.info("Game {} - {} untaps {} (may-not-untap choice)", gameData.id, player.getUsername(), sourceCard.getName());
        } else {
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " chooses not to untap " , sourceCard, "."));
            log.info("Game {} - {} keeps {} tapped (may-not-untap choice)", gameData.id, player.getUsername(), sourceCard.getName());
        }

        playerInputService.processNextMayAbility(gameData);

        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            // All may-not-untap choices resolved — complete the turn advance and resume auto-pass
            turnProgressionService.completeTurnAdvance(gameData);
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    public void handleOpeningHandDelayedCounterTrigger(GameData gameData, Player player, boolean accepted,
                                                        PendingMayAbility ability, RegisterDelayedCounterTriggerEffect effect) {
        if (accepted) {
            gameData.openingHandRevealTriggers.add(new OpeningHandRevealTrigger(
                    ability.controllerId(), ability.sourceCard(),
                    new CounterUnlessPaysEffect(effect.genericManaAmount())
            ));

            
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " reveals " , ability.sourceCard(), " from their opening hand."));
            log.info("Game {} - {} reveals {} from opening hand (delayed counter trigger registered)",
                    gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines to reveal " , ability.sourceCard(), "."));
            log.info("Game {} - {} declines to reveal {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleOpeningHandDelayedManaTrigger(GameData gameData, Player player, boolean accepted,
                                                     PendingMayAbility ability, RegisterDelayedManaTriggerEffect effect) {
        if (accepted) {
            gameData.openingHandManaTriggers.add(new OpeningHandRevealTrigger(
                    ability.controllerId(), ability.sourceCard(),
                    new AwardManaEffect(effect.color(), effect.amount())
            ));

            
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " reveals " , ability.sourceCard(), " from their opening hand."));
            log.info("Game {} - {} reveals {} from opening hand (delayed mana trigger registered)",
                    gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines to reveal " , ability.sourceCard(), "."));
            log.info("Game {} - {} declines to reveal {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleSingleDrawReplacementChoice(GameData gameData, Player player, boolean accepted,
                                                   PendingMayAbility ability,
                                                   ReplaceSingleDrawEffect effect) {
        UUID drawingPlayerId = effect.playerId();
        String playerName = gameData.playerIdToName.get(drawingPlayerId);

        if (!accepted) {
            drawService.resolveDrawCardWithoutStaticReplacementCheck(gameData, drawingPlayerId);
            if (effect.kind() == DrawReplacementKind.FASTING) {
                stepTriggerService.handleDrawStepTriggers(gameData);
            }
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines to use " , ability.sourceCard(), "."));

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        if (effect.kind() == DrawReplacementKind.FASTING) {
            lifeSupport.applyGainLife(gameData, drawingPlayerId, 2, ability.sourceCard().getName());
            gameLogService.append(gameData, GameLog.textCardText(playerName + " skips their draw step with ",
                    ability.sourceCard(), " and gains 2 life."));
            log.info("Game {} - {} skips draw step with {} and gains 2 life",
                    gameData.id, playerName, ability.sourceCard().getName());

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        if (effect.kind() == DrawReplacementKind.STUDY_COUNTER) {
            Permanent source = ability.sourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
            if (source != null) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, null, source, CounterType.STUDY, 1);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " replaces the draw with ", ability.sourceCard(), "."));
                log.info("Game {} - {} replaces a draw with a study counter on {}",
                        gameData.id, player.getUsername(), ability.sourceCard().getName());
            }
            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        if (effect.kind() == DrawReplacementKind.ARCHMAGE_ASCENSION) {
            StackEntry searchEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    drawingPlayerId,
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new SearchLibraryEffect())),
                    0,
                    ability.sourcePermanentId());
            searchLibraryEffectHandler.resolve(gameData, searchEntry, new SearchLibraryEffect());
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " replaces the draw with ", ability.sourceCard(), "."));
            log.info("Game {} - {} replaces a draw with a library search from {}",
                    gameData.id, player.getUsername(), ability.sourceCard().getName());

            if (!gameData.interaction.isAwaitingInput()) {
                playerInputService.processNextMayAbility(gameData);
            }
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        if (effect.kind() == DrawReplacementKind.ABUNDANCE) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    drawingPlayerId, null, null,
                    new ChoiceContext.DrawReplacementChoice(drawingPlayerId, effect.kind()),
                    List.of("LAND", "NONLAND"), "Choose land or nonland for Abundance."));
            log.info("Game {} - Awaiting {} to choose land or nonland for Abundance", gameData.id, playerName);
            return;
        }

        if (effect.kind() == DrawReplacementKind.OBSTINATE_FAMILIAR) {
            gameLogService.append(gameData, GameLog.textCardText(playerName + " skips their draw with ",
                    ability.sourceCard(), "."));
            log.info("Game {} - {} skips draw with {}", gameData.id, playerName,
                    ability.sourceCard().getName());

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        if (effect.kind() == DrawReplacementKind.ZURS_WEIRDING) {
            // The choosing player (may-ability controller) pays 2 life; the revealed top card of the
            // drawing player's library goes into that player's graveyard instead of being drawn.
            lifeSupport.applyLifeLoss(gameData, player.getId(), 2, ability.sourceCard().getName());

            List<Card> deck = gameData.playerDecks.get(drawingPlayerId);
            if (deck != null && !deck.isEmpty()) {
                Card top = deck.removeFirst();
                graveyardService.addCardToGraveyard(gameData, drawingPlayerId, top, Zone.LIBRARY);
                gameLogService.append(gameData, GameLog.textCardText(playerName + "'s ", top, " is put into their graveyard."));
                log.info("Game {} - {}'s revealed {} put into graveyard by {}",
                        gameData.id, playerName, top.getName(), ability.sourceCard().getName());
            }

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        if (effect.kind() == DrawReplacementKind.ISLAND_SANCTUARY) {
            // The draw is skipped (the accept branch never draws). Until the drawing player's next turn
            // they can't be attacked except by creatures with flying and/or islandwalk — stamped as a
            // player-scoped floating effect so it persists even if Island Sanctuary leaves.
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    ability.sourceCard().getName(),
                    null,
                    drawingPlayerId,
                    new CreaturesCantAttackControllerUnlessPredicateEffect(new PermanentAnyOfPredicate(List.of(
                            new PermanentHasKeywordPredicate(Keyword.FLYING),
                            new PermanentHasKeywordPredicate(Keyword.ISLANDWALK)))),
                    null,
                    drawingPlayerId,
                    null,
                    EffectDuration.UNTIL_YOUR_NEXT_TURN,
                    0L));

            gameLogService.append(gameData, GameLog.textCardText(playerName + " skips their draw with " , ability.sourceCard(), "."));
            log.info("Game {} - {} skips draw for Island Sanctuary shield", gameData.id, playerName);

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        throw new IllegalStateException("Unsupported draw replacement kind: " + effect.kind());
    }

    public void handleMaySacrificeArtifactForDividedDamage(GameData gameData, Player player,
                                                            boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            UUID controllerId = ability.controllerId();
            List<UUID> validArtifactIds = new ArrayList<>();
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (gameQueryService.isArtifact(p)) {
                        validArtifactIds.add(p.getId());
                    }
                }
            }

            if (validArtifactIds.isEmpty()) {
                String logEntry = player.getUsername() + " has no artifacts to sacrifice.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} has no artifacts to sacrifice for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());

                gameData.pendingETBDamageAssignments = Map.of();
                playerInputService.processNextMayAbility(gameData);
                if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                    inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                }
                return;
            }

            Map<UUID, Integer> damageAssignments = gameData.pendingETBDamageAssignments;
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.SacrificeArtifactForDividedDamage(
                            controllerId, ability.sourceCard(), damageAssignments));
            playerInputService.beginPermanentChoice(gameData, controllerId, validArtifactIds,
                    ability.sourceCard().getName() + " — Choose an artifact to sacrifice.");

            String logEntry = player.getUsername() + " accepts — choosing an artifact to sacrifice.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} accepts sacrifice for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            gameData.pendingETBDamageAssignments = Map.of();

            
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines to sacrifice an artifact for " , ability.sourceCard(), "."));
            log.info("Game {} - {} declines sacrifice for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());

            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
        }
    }

    public void handleSurveilMayGraveyardChoice(GameData gameData, Player player, boolean accepted) {
        UUID controllerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (accepted && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            graveyardService.addCardToGraveyard(gameData, controllerId, topCard, Zone.LIBRARY);
            
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", topCard, " into their graveyard (surveil)."));
            log.info("Game {} - {} puts {} into graveyard (surveil)",
                    gameData.id, player.getUsername(), topCard.getName());
        } else {
            String logEntry = player.getUsername() + " leaves the card on top of their library (surveil).";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} leaves card on top (surveil)", gameData.id, player.getUsername());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Eye Spy — the controller may put the looked-at top card into the target
     * player's graveyard. The library/graveyard owner is the targeted player, not
     * the prompted controller.
     */
    public void handleLookAtTargetPlayerTopCardChoice(GameData gameData, boolean accepted, UUID libraryOwnerId,
                                                      UUID controllerId, int lifeCost) {
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);
        String ownerName = gameData.playerIdToName.get(libraryOwnerId);

        boolean canPayLife = lifeCost <= 0 || gameData.getLife(controllerId) >= lifeCost;
        if (accepted && canPayLife && deck != null && !deck.isEmpty()) {
            if (lifeCost > 0) {
                int life = gameData.getLife(controllerId);
                gameData.playerLifeTotals.put(controllerId, life - lifeCost);
                triggerCollectionService.checkLifePaymentTriggers(gameData, controllerId, lifeCost);
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId) + " pays " + lifeCost + " life."));
            }
            Card topCard = deck.removeFirst();
            graveyardService.addCardToGraveyard(gameData, libraryOwnerId, topCard, Zone.LIBRARY);
            gameLogService.append(gameData, GameLog.builder().card(topCard).text(" is put into " + ownerName + "'s graveyard.").build());
            log.info("Game {} - {} put into {}'s graveyard (Eye Spy)",
                    gameData.id, topCard.getName(), ownerName);
        } else {
            gameLogService.append(gameData, GameLog.text("The card is left on top of " + ownerName + "'s library."));
            log.info("Game {} - card left on top of {}'s library (Eye Spy)", gameData.id, ownerName);
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleExploreMayGraveyardChoice(GameData gameData, Player player, boolean accepted) {
        UUID controllerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (accepted && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            graveyardService.addCardToGraveyard(gameData, controllerId, topCard, Zone.LIBRARY);
            
            gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " puts ").card(topCard).text(" into their graveyard.").build());
            log.info("Game {} - {} puts {} into graveyard (explore)",
                    gameData.id, player.getUsername(), topCard.getName());
        } else {
            String logEntry = player.getUsername() + " leaves the revealed card on top of their library.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} leaves revealed card on top (explore)", gameData.id, player.getUsername());
        }

        // Explore is complete — check for "whenever a creature you control explores" triggers
        triggerCollectionService.checkExploreTriggers(gameData, controllerId);

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ExploreTriggerTarget.class)) {
            triggerCollectionService.processNextExploreTriggerTarget(gameData);
            return;
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleRevealTopCardMayBottomChoice(GameData gameData, Player player, boolean accepted) {
        UUID controllerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (accepted && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            deck.add(topCard);
            
            gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " puts ").card(topCard).text(" on the bottom of their library.").build());
            log.info("Game {} - {} puts {} on the bottom of library",
                    gameData.id, player.getUsername(), topCard.getName());
        } else {
            String logEntry = player.getUsername() + " leaves the revealed card on top of their library.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} leaves revealed card on top", gameData.id, player.getUsername());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Nissa, Steward of Elements 0 — the controller may put the looked-at top card (already
     * confirmed to be a land or a low-enough-cost creature) onto the battlefield; otherwise it
     * stays on top of the library.
     */
    public void handleLookAtTopCardPutLandOrCreatureChoice(GameData gameData, Player player, boolean accepted) {
        handleLookAtTopCardPutLandOrCreatureChoice(gameData, player, accepted, false);
    }

    public void handleLookAtTopCardPutLandOrCreatureChoice(GameData gameData, Player player, boolean accepted,
                                                            boolean enterTapped) {
        UUID controllerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (accepted && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            Permanent perm = new Permanent(topCard, Zone.LIBRARY);
            if (enterTapped) {
                perm.tap();
            }
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);
            if (topCard.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, topCard, null, false);
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", topCard, " onto the battlefield."));
            log.info("Game {} - {} puts {} onto the battlefield from the top of their library",
                    gameData.id, player.getUsername(), topCard.getName());
        } else {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " leaves the top card on their library."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Believe: accept → top card onto battlefield; decline → top card into hand.
     */
    public void handleLookAtTopCardMayPutCreatureElseToHandChoice(
            GameData gameData, Player player, boolean accepted) {
        UUID controllerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (deck.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card topCard = deck.removeFirst();
        if (accepted) {
            Permanent perm = new Permanent(topCard, Zone.LIBRARY);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);
            if (topCard.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, topCard, null, false);
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", topCard, " onto the battlefield."));
            log.info("Game {} - {} puts {} onto the battlefield (Believe)",
                    gameData.id, player.getUsername(), topCard.getName());
        } else {
            gameData.playerHands.get(controllerId).add(topCard);
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " puts the top card into their hand."));
            log.info("Game {} - {} puts {} into hand from library top (Believe)",
                    gameData.id, player.getUsername(), topCard.getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Risen Reef: accept -> the matching top land enters the battlefield tapped; decline -> the
     * top card goes into hand.
     */
    public void handleLookAtTopCardMayPutMatchingElseToHandChoice(
            GameData gameData, Player player, boolean accepted, boolean tapped) {
        UUID controllerId = player.getId();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (deck.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card topCard = deck.removeFirst();
        if (accepted) {
            Permanent perm = new Permanent(topCard, Zone.LIBRARY);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);
            if (tapped) {
                perm.tap();
            }
            if (topCard.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, topCard, null, false);
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", topCard, " onto the battlefield."));
            log.info("Game {} - {} puts {} onto the battlefield from library top",
                    gameData.id, player.getUsername(), topCard.getName());
        } else {
            gameData.playerHands.get(controllerId).add(topCard);
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " puts the top card into their hand."));
            log.info("Game {} - {} puts {} into hand from library top",
                    gameData.id, player.getUsername(), topCard.getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Unexpected Results, land branch: "you may put it onto the battlefield and return Unexpected
     * Results to its owner's hand". Both halves are one choice, so a decline leaves the land on top
     * of the library and lets the sorcery go to the graveyard as normal. Putting the land onto the
     * battlefield is not a land play, so it neither uses up the turn's land drop nor fires
     * plays-a-land triggers.
     */
    public void handleUnexpectedResultsLandChoice(
            GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID controllerId = player.getId();
        Card land = ability.sourceCard();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to put ", land, " onto the battlefield."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (deck.isEmpty() || !deck.getFirst().getId().equals(land.getId())) {
            gameLogService.append(gameData, GameLog.cardThen(land, " is no longer on top of the library."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        deck.removeFirst();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId,
                new Permanent(land, Zone.LIBRARY));
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " puts ", land, " onto the battlefield."));
        log.info("Game {} - {} puts {} onto the battlefield (Unexpected Results)",
                gameData.id, player.getUsername(), land.getName());
        battlefieldEntryService.processCreatureETBEffects(gameData, controllerId, land, null, false);

        StackEntry parked = gameData.pendingEffectResolutionEntry;
        if (parked != null && parked.getCard().getId().equals(ability.targetCardId())) {
            parked.setReturnToHandAfterResolving(true);
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void handleLeylineChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            Card card = ability.sourceCard();
            UUID controllerId = ability.controllerId();
            List<Card> hand = gameData.playerHands.get(controllerId);
            hand.remove(card);

            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

            
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " begins the game with " , card, " on the battlefield."));
            log.info("Game {} - {} starts with {} on the battlefield (leyline)",
                    gameData.id, player.getUsername(), card.getName());
        } else {
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines to put " , ability.sourceCard(), " on the battlefield."));
            log.info("Game {} - {} declines leyline placement for {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        }

        playerInputService.processNextMayAbility(gameData);

        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            // All leyline choices resolved — continue with game start
            mulliganService.continueStartGame(gameData);
        }
    }

    public void handleGemstoneCavernsChoice(GameData gameData, Player player, boolean accepted,
                                            PendingMayAbility ability) {
        Card card = ability.sourceCard();
        UUID controllerId = ability.controllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean sourceInHand = hand != null && hand.stream()
                .anyMatch(handCard -> handCard.getId().equals(card.getId()));

        if (!accepted || !sourceInHand) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + (accepted ? " cannot begin the game with " : " declines to begin the game with "),
                    card, " on the battlefield."));
            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                mulliganService.continueStartGame(gameData);
            }
            return;
        }

        hand.removeIf(handCard -> handCard.getId().equals(card.getId()));
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setCounterCount(CounterType.LUCK, 1);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);

        if (hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " begins the game with ", card, " on the battlefield."));
            playerInputService.processNextMayAbility(gameData);
            if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
                mulliganService.continueStartGame(gameData);
            }
            return;
        }

        gameData.pendingGemstoneCavernsChoice = new PendingGemstoneCavernsChoice(card, controllerId);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " begins the game with ", card,
                " on the battlefield and must exile a card from their hand."));
        playerInputService.beginExileFromHandChoice(gameData, controllerId, null, 1);
    }

    public void handleSphinxAmbassadorChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        PendingSphinxAmbassadorChoice pending = gameData.peekPendingInteraction(PendingSphinxAmbassadorChoice.class);
        if (pending == null || pending.selectedCard() == null) {
            throw new IllegalStateException("No pending Sphinx Ambassador choice");
        }

        Card selectedCard = pending.selectedCard();
        UUID controllerId = pending.controllerId();
        UUID targetPlayerId = pending.targetPlayerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (accepted) {
            // Put creature onto battlefield under controller's control
            Permanent perm = new Permanent(selectedCard, Zone.LIBRARY);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, selectedCard, null, false);

            
            gameLogService.append(gameData, GameLog.builder().text(controllerName + " puts ").card(selectedCard).text(" onto the battlefield under their control. " + targetName + "'s library is shuffled.").build());
            log.info("Game {} - {} puts {} onto battlefield from Sphinx Ambassador",
                    gameData.id, controllerName, selectedCard.getName());
        } else {
            // Return card to library
            gameData.playerDecks.get(targetPlayerId).add(selectedCard);

            String logEntry = controllerName + " declines to put the card onto the battlefield. "
                    + targetName + "'s library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines Sphinx Ambassador placement", gameData.id, controllerName);
        }

        LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
        gameData.clearPendingInteractions(PendingSphinxAmbassadorChoice.class);

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
