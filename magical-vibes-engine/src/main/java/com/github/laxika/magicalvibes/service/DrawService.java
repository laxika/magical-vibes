package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingNextDrawDamageReplacement;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AbundanceDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ChainsOfMephistophelesDrawReplacement;
import com.github.laxika.magicalvibes.model.effect.CounterThresholdDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnFromGraveyardInsteadOfDrawEffect;
import com.github.laxika.magicalvibes.model.effect.BoobyTrapEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawExceptFirstDrawStepDrawEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentExtraDrawsRedirectedEffect;
import com.github.laxika.magicalvibes.model.effect.QuantumRiddlerDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SharedFateDrawReplacement;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOpponentPermanentOnDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseOneToHandDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MaySkipDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.service.effect.DredgeSupport;
import com.github.laxika.magicalvibes.model.effect.DrawRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.DrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.FirstDrawRevealTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmptyHandDrawExtraCardAndLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsCreaturesToHandDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCreatureToGraveyardElseDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawIfEmptyLibraryReplacementEffect;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.effect.MiracleRevealEffect;
import com.github.laxika.magicalvibes.model.effect.RevealFirstDrawDrawOnBasicLandEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameOnEmptyLibraryDrawEffect;
import com.github.laxika.magicalvibes.model.effect.UbaMaskDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ZursWeirdingDrawReplacementEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.mayfx.BreathstealersCryptDrawReplacementHandler;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.OncePerTurnTriggerSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerReturnsPermanentToHandEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import com.github.laxika.magicalvibes.service.outcome.LossOutcome;
import com.github.laxika.magicalvibes.service.outcome.LossReason;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
public class DrawService {

    private final GameQueryService gameQueryService;
    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    // @Lazy to break the constructor cycle DrawService → InteractionHandlerRegistry →
    // (graveyard/card choice handlers) → DrawService.
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;
    // @Lazy: handler → InputCompletionService → … can reach back into draw/resolution paths.
    private final BreathstealersCryptDrawReplacementHandler breathstealersCryptDrawReplacementHandler;
    private final LifeSupport lifeSupport;
    private final GraveyardService graveyardService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final EachPlayerReturnsPermanentToHandEffectHandler eachPlayerReturnsPermanentToHandEffectHandler;
    private final DamageSupport damageSupport;
    private final PermanentControlSupport permanentControlSupport;

    private static final CreateTokenEffect WORDS_OF_WILDING_BEAR = new CreateTokenEffect(
            "Bear", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR), Set.of(), Set.of());
    private final GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final DredgeSupport dredgeSupport;

    public DrawService(GameQueryService gameQueryService,
                       ExileService exileService,
                       GameLogService gameLogService,
                       GameOutcomeService gameOutcomeService,
                       TriggeredAbilityQueueService triggeredAbilityQueueService,
                       @Lazy InteractionHandlerRegistry interactionHandlerRegistry,
                       @Lazy PlayerInteractionSupport playerInteractionSupport,
                       @Lazy BreathstealersCryptDrawReplacementHandler breathstealersCryptDrawReplacementHandler,
                       @Lazy LifeSupport lifeSupport,
                       @Lazy GraveyardService graveyardService,
                       ConditionEvaluationService conditionEvaluationService,
                       @Lazy EachPlayerReturnsPermanentToHandEffectHandler eachPlayerReturnsPermanentToHandEffectHandler,
                       @Lazy DamageSupport damageSupport,
                       @Lazy PermanentControlSupport permanentControlSupport,
                        GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport,
                        DredgeSupport dredgeSupport) {
        this.gameQueryService = gameQueryService;
        this.exileService = exileService;
        this.gameLogService = gameLogService;
        this.gameOutcomeService = gameOutcomeService;
        this.triggeredAbilityQueueService = triggeredAbilityQueueService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.playerInteractionSupport = playerInteractionSupport;
        this.breathstealersCryptDrawReplacementHandler = breathstealersCryptDrawReplacementHandler;
        this.lifeSupport = lifeSupport;
        this.graveyardService = graveyardService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.eachPlayerReturnsPermanentToHandEffectHandler = eachPlayerReturnsPermanentToHandEffectHandler;
        this.damageSupport = damageSupport;
        this.permanentControlSupport = permanentControlSupport;
        this.grantedTriggeredAbilitySupport = grantedTriggeredAbilitySupport;
        this.dredgeSupport = dredgeSupport;
    }

    public void resolveDrawCard(GameData gameData, UUID playerId) {
        resolveDrawCards(gameData, playerId, 1);
    }

    public void resolveDrawCards(GameData gameData, UUID playerId, int amount) {
        if (amount <= 0) {
            return;
        }

        int drawAmount = amount;
        Permanent quantumRiddler = findQuantumRiddlerDrawReplacementSource(gameData, playerId);
        if (quantumRiddler != null) {
            drawAmount++;
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " draws one additional card with " + quantumRiddler.getCard().getName() + "."));
            log.info("Game {} - {} draws one additional card with {}",
                    gameData.id, playerName, quantumRiddler.getCard().getName());
        }

        for (int i = 0; i < drawAmount; i++) {
            resolveDrawCardInternal(gameData, playerId);
        }
    }

    private void resolveDrawCardInternal(GameData gameData, UUID playerId) {
        if (preventDrawIfNeeded(gameData, playerId)) {
            gameData.chainsDrawReplacementsApplied.remove(playerId);
            return;
        }

        if (isDrawSkipped(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " skips that draw."));
            log.info("Game {} - {} skips a draw (draw replacement in effect)", gameData.id, playerName);
            gameData.chainsDrawReplacementsApplied.remove(playerId);
            return;
        }

        Integer pendingExileTopCard = gameData.pendingNextDrawExileTopCard.get(playerId);
        if (pendingExileTopCard != null && pendingExileTopCard > 0) {
            if (pendingExileTopCard == 1) {
                gameData.pendingNextDrawExileTopCard.remove(playerId);
            } else {
                gameData.pendingNextDrawExileTopCard.put(playerId, pendingExileTopCard - 1);
            }
            resolveNextDrawExileTopCardMayPlayThisTurn(gameData, playerId);
            return;
        }

        // Mark this draw as the player's turn-based draw-step draw before any replacement is applied,
        // so effects that exempt "the first card they draw in each of their draw steps" (Notion Thief)
        // see a stable answer even if their source enters play later in the turn.
        boolean firstDrawStepDraw = markFirstDrawStepDraw(gameData, playerId);

        if (!firstDrawStepDraw && resolveChainsOfMephistophelesDrawReplacement(gameData, playerId)) {
            return;
        }

        List<Integer> dredgeIndices = dredgeSupport.eligibleGraveyardIndices(gameData, playerId);
        if (!dredgeIndices.isEmpty()) {
            interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                    .builder(playerId, dredgeIndices, GraveyardChoiceDestination.DREDGE,
                            "Choose a card to dredge, or decline.")
                    .build());
            return;
        }

        Permanent sharedFateSource = findSharedFateSource(gameData);
        if (sharedFateSource != null) {
            resolveSharedFateDrawReplacement(gameData, playerId, sharedFateSource);
            return;
        }

        Card maySkipDrawSource = findMaySkipDrawSourceCard(gameData, playerId);
        if (maySkipDrawSource != null) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    maySkipDrawSource,
                    playerId,
                    List.of(new ReplaceSingleDrawEffect(playerId, DrawReplacementKind.OBSTINATE_FAMILIAR)),
                    "Skip this draw with " + maySkipDrawSource.getName() + "?"
            ));
            return;
        }

        // Aladdin's Lamp — one-shot, turn-scoped delayed replacement of this player's next draw:
        // instead look at the top X cards, put all but one on the bottom in a random order, then draw.
        Integer lookAtTopX = gameData.pendingNextDrawLookAtTop.remove(playerId);
        if (lookAtTopX != null) {
            resolveNextDrawLookAtTop(gameData, playerId, lookAtTopX);
            return;
        }

        // Mangara's Tome — one-shot, turn-scoped delayed replacement of this player's next draw:
        // instead put the top card of the exiled pile into its owner's hand.
        List<UUID> pendingPileDraws = gameData.pendingNextDrawFromExiledPile.get(playerId);
        if (pendingPileDraws != null && !pendingPileDraws.isEmpty()) {
            UUID pileSourceId = pendingPileDraws.removeFirst();
            if (pendingPileDraws.isEmpty()) {
                gameData.pendingNextDrawFromExiledPile.remove(playerId);
            }
            resolveNextDrawFromExiledPile(gameData, playerId, pileSourceId);
            return;
        }

        // Words of Worship — one queued activation replaces one draw with gaining 5 life.
        Integer pendingGainLife = gameData.pendingNextDrawGainLife.remove(playerId);
        if (pendingGainLife != null) {
            if (pendingGainLife > 1) {
                gameData.pendingNextDrawGainLife.put(playerId, pendingGainLife - 1);
            }
            lifeSupport.applyGainLife(gameData, playerId, 5, "Words of Worship");
            return;
        }

        // Words of Wilding - one queued activation replaces one draw with creating a Bear token.
        List<String> pendingCreateBears = gameData.pendingNextDrawCreateBears.get(playerId);
        if (pendingCreateBears != null && !pendingCreateBears.isEmpty()) {
            String sourceSetCode = pendingCreateBears.removeFirst();
            if (pendingCreateBears.isEmpty()) {
                gameData.pendingNextDrawCreateBears.remove(playerId);
            }
            permanentControlSupport.applyCreateToken(gameData, playerId, WORDS_OF_WILDING_BEAR,
                    1, sourceSetCode);
            return;
        }

        List<PendingNextDrawDamageReplacement> pendingDamage = gameData.pendingNextDrawDamage.get(playerId);
        if (pendingDamage != null && !pendingDamage.isEmpty()) {
            PendingNextDrawDamageReplacement replacement = pendingDamage.removeFirst();
            if (pendingDamage.isEmpty()) {
                gameData.pendingNextDrawDamage.remove(playerId);
            }
            resolveNextDrawDamage(gameData, playerId, replacement);
            return;
        }

        Integer pendingReturnPermanents = gameData.pendingNextDrawReturnPermanents.remove(playerId);
        if (pendingReturnPermanents != null) {
            if (pendingReturnPermanents > 1) {
                gameData.pendingNextDrawReturnPermanents.put(playerId, pendingReturnPermanents - 1);
            }
            eachPlayerReturnsPermanentToHandEffectHandler.beginReplacement(gameData, "Words of Wind");
            return;
        }

        Integer pendingDiscardOpponents = gameData.pendingNextDrawDiscardOpponents.remove(playerId);
        if (pendingDiscardOpponents != null) {
            if (pendingDiscardOpponents > 1) {
                gameData.pendingNextDrawDiscardOpponents.put(playerId, pendingDiscardOpponents - 1);
            }
            playerInteractionSupport.startNextEachPlayerDiscard(gameData,
                    DiscardFollowUp.eachPlayer(opponentsInApnapOrder(gameData, playerId), playerId, 1));
            return;
        }

        // Forbidden Crypt — "If you would draw a card, return a card from your graveyard to your
        // hand instead. If you can't, you lose the game." Mandatory replacement for the drawer.
        if (findReturnFromGraveyardInsteadOfDrawSourceCard(gameData, playerId) != null) {
            // A prior forced return from the same multi-card draw is still awaiting a choice; don't
            // stack another interaction over it (it would overwrite the active one).
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) {
                // Can't return a card — the player loses the game (CR 104.3a, replacement wording).
                if (gameOutcomeService.resolveLoss(gameData, playerId, LossReason.EFFECT) == LossOutcome.LOSES) {
                    UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                    String lossLog = gameData.playerIdToName.get(playerId)
                            + " can't return a card from their graveyard and loses the game.";
                    gameLogService.append(gameData, GameLog.text(lossLog));
                    log.info("Game {} - {} loses (Forbidden Crypt: empty graveyard on draw)",
                            gameData.id, gameData.playerIdToName.get(playerId));
                    gameOutcomeService.declareWinner(gameData, winnerId);
                }
                return;
            }
            List<Integer> validIndices = IntStream.range(0, graveyard.size()).boxed().toList();
            interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                    .builder(playerId, validIndices, GraveyardChoiceDestination.HAND,
                            "Return a card from your graveyard to your hand.")
                    .build());
            return;
        }

        Card abundanceSource = findAbundanceSourceCard(gameData, playerId);
        if (abundanceSource != null) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    abundanceSource,
                    playerId,
                    List.of(new ReplaceSingleDrawEffect(playerId, DrawReplacementKind.ABUNDANCE)),
                    "Replace this draw with Abundance?"
            ));
            return;
        }

        // Sages of the Anima — "If you would draw a card, instead reveal the top three cards of your
        // library. Put all creature cards revealed this way into your hand and the rest on the bottom of
        // your library in any order." Mandatory replacement for the drawing controller.
        Permanent revealCreaturesSource = findRevealCreaturesDrawReplacementSource(gameData, playerId);
        if (revealCreaturesSource != null) {
            resolveRevealCreaturesDrawReplacement(gameData, playerId, revealCreaturesSource);
            return;
        }

        // Top-three hand replacement effects (Tomorrow, Azami's Familiar and Underrealm Lich).
        Permanent lookChooseOneSource = findLookAtTopChooseOneToHandDrawReplacementSource(gameData, playerId);
        if (lookChooseOneSource != null) {
            resolveLookAtTopChooseOneToHandDrawReplacement(gameData, playerId, lookChooseOneSource);
            return;
        }

        // Enduring Renewal — "If you would draw a card, reveal the top card of your library instead.
        // If it's a creature card, put it into your graveyard. Otherwise, draw a card."
        Permanent enduringRenewalSource = findRevealTopCreatureToGraveyardElseDrawSource(gameData, playerId);
        if (enduringRenewalSource != null) {
            resolveRevealTopCreatureToGraveyardElseDraw(gameData, playerId, enduringRenewalSource);
            return;
        }

        Permanent counterDrawReplacementSource = findCounterDrawReplacementSource(
                gameData, playerId, CounterType.STUDY);
        if (counterDrawReplacementSource != null) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    counterDrawReplacementSource.getCard(),
                    playerId,
                    List.of(new ReplaceSingleDrawEffect(playerId, DrawReplacementKind.STUDY_COUNTER)),
                    "Put a study counter on " + counterDrawReplacementSource.getCard().getName()
                            + " instead of drawing?",
                    null,
                    null,
                    counterDrawReplacementSource.getId()));
            return;
        }

        Permanent archmageAscensionSource = findCounterThresholdDrawReplacementSource(
                gameData, playerId, CounterType.QUEST);
        if (archmageAscensionSource != null) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    archmageAscensionSource.getCard(),
                    playerId,
                    List.of(new ReplaceSingleDrawEffect(playerId, DrawReplacementKind.ARCHMAGE_ASCENSION)),
                    "Search your library for a card instead of drawing?",
                    null,
                    null,
                    archmageAscensionSource.getId()));
            return;
        }

        // Uba Mask — "If a player would draw a card, that player exiles that card face up instead."
        // Global, mandatory: the exiled card carries an end-of-turn play permission for that player.
        Permanent ubaMaskSource = findUbaMaskSource(gameData);
        if (ubaMaskSource != null) {
            resolveUbaMaskDrawReplacement(gameData, playerId, ubaMaskSource);
            return;
        }

        Card zursWeirdingSource = findZursWeirdingSourceCard(gameData);
        if (zursWeirdingSource != null) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            UUID otherPlayerId = gameQueryService.getOpponentId(gameData, playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            if (deck != null && !deck.isEmpty()) {
                Card revealed = deck.getFirst();
                gameLogService.append(gameData, GameLog.builder()
                        .text(playerName + " reveals ")
                        .card(revealed)
                        .text(" with ")
                        .card(zursWeirdingSource)
                        .text(".")
                        .build());

            }
            if (gameData.getLife(otherPlayerId) >= 2
                    && gameQueryService.canPlayerLifeChange(gameData, otherPlayerId)) {
                String prompt = deck != null && !deck.isEmpty()
                        ? "Pay 2 life to put " + playerName + "'s revealed "
                                + deck.getFirst().getName() + " into their graveyard?"
                        : "Pay 2 life to replace " + playerName + "'s draw?";
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        zursWeirdingSource,
                        otherPlayerId,
                        List.of(new ReplaceSingleDrawEffect(playerId, DrawReplacementKind.ZURS_WEIRDING)),
                        prompt
                ));
                return;
            }
            performDrawCard(gameData, playerId);
            return;
        }

        // Notion Thief — if an opponent of the source's controller would draw a card except the first
        // one they draw in each of their draw steps, that player skips the draw and the controller
        // draws a card instead.
        if (!firstDrawStepDraw) {
            Card notionThief = findOpponentExtraDrawsRedirectedSourceCard(gameData, playerId);
            if (notionThief != null) {
                UUID thiefController = gameQueryService.getOpponentId(gameData, playerId);
                gameLogService.append(gameData, GameLog.builder()
                        .text(gameData.playerIdToName.get(playerId) + " skips a draw — ")
                        .card(notionThief)
                        .text(" makes " + gameData.playerIdToName.get(thiefController) + " draw a card instead.")
                        .build());
                performDrawCard(gameData, thiefController);
                return;
            }
        }

        UUID replacementController = gameData.drawReplacementTargetToController.get(playerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is redirected — " + controllerName + " draws a card instead.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - Draw redirect: {}'s draw goes to {} instead",
                    gameData.id, playerName, controllerName);
            performDrawCard(gameData, replacementController);
            return;
        }

        // Blood Scrivener: if you would draw a card while you have no cards in hand, instead you
        // draw two cards and you lose 1 life. The hand is checked as the draw would happen, so only
        // the first draw of a multi-card draw sees an empty hand.
        Card bloodScrivenerSource = findEmptyHandDrawExtraSourceCard(gameData, playerId);
        if (bloodScrivenerSource != null && isHandEmpty(gameData, playerId)) {
            performDrawCard(gameData, playerId);
            performDrawCard(gameData, playerId);
            lifeSupport.applyLifeLoss(gameData, playerId, 1, bloodScrivenerSource.getName());
            return;
        }

        // Thought Reflection / Alhammarret's Archive: if you would draw a card, draw two cards
        // instead, except for the first card drawn during the controller's own draw step.
        boolean doubles = findDoubleDrawSourceCard(gameData, playerId) != null
                || (!firstDrawStepDraw && findExceptFirstDoubleDrawSourceCard(gameData, playerId) != null);
        if (doubles) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + "'s draw is doubled — they draw two cards instead."));
            log.info("Game {} - {}'s draw doubled", gameData.id, playerName);
            performDrawCard(gameData, playerId);
            performDrawCard(gameData, playerId);
            return;
        }

        performDrawCard(gameData, playerId);
    }

    private List<UUID> opponentsInApnapOrder(GameData gameData, UUID controllerId) {
        List<UUID> opponents = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && !activePlayerId.equals(controllerId)
                && gameData.playerIds.contains(activePlayerId)) {
            opponents.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId) && !playerId.equals(activePlayerId)
                    && gameData.playerIds.contains(playerId)) {
                opponents.add(playerId);
            }
        }
        return opponents;
    }

    private void resolveNextDrawDamage(GameData gameData, UUID playerId,
                                       PendingNextDrawDamageReplacement replacement) {
        StackEntry damageEntry = new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                replacement.sourceCard(),
                playerId,
                replacement.sourceCard().getName() + "'s draw replacement",
                List.of(),
                replacement.targetId(),
                replacement.sourcePermanentId());
        damageSupport.resolveAnyTargetDamage(gameData, damageEntry, replacement.targetId(), 2, false);
    }

    public void resolveDrawCardWithoutStaticReplacementCheck(GameData gameData, UUID playerId) {
        gameData.chainsDrawReplacementsApplied.remove(playerId);
        if (preventDrawIfNeeded(gameData, playerId)) {
            return;
        }

        if (isDrawSkipped(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " skips that draw."));
            log.info("Game {} - {} skips a draw (draw replacement in effect)", gameData.id, playerName);
            return;
        }

        UUID replacementController = gameData.drawReplacementTargetToController.get(playerId);
        if (replacementController != null) {
            String playerName = gameData.playerIdToName.get(playerId);
            String controllerName = gameData.playerIdToName.get(replacementController);
            String logEntry = playerName + "'s draw is redirected — " + controllerName + " draws a card instead.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - Draw redirect: {}'s draw goes to {} instead",
                    gameData.id, playerName, controllerName);

            if (replacementController.equals(playerId)) {
                performDrawCard(gameData, replacementController);
            } else {
                resolveDrawCard(gameData, replacementController);
            }
            return;
        }

        performDrawCard(gameData, playerId);
    }

    private boolean preventDrawIfNeeded(GameData gameData, UUID playerId) {
        if (!isDrawPrevented(gameData, playerId)) {
            return false;
        }

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.text(playerName + " can't draw a card."));
        log.info("Game {} - {} can't draw (draw prevention in effect)", gameData.id, playerName);
        return true;
    }

    private boolean isDrawPrevented(GameData gameData, UUID playerId) {
        int cardsDrawnThisTurn = gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0);
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                boolean prevents = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(DrawRestrictionEffect.class::isInstance)
                        .map(DrawRestrictionEffect.class::cast)
                        .anyMatch(effect -> effect.appliesTo(pid, playerId)
                                && effect.preventsDraw(cardsDrawnThisTurn));
                if (prevents) return true;
            }
        }
        return false;
    }

    private boolean resolveChainsOfMephistophelesDrawReplacement(GameData gameData, UUID playerId) {
        int activeChains = countChainsOfMephistopheles(gameData);
        int alreadyApplied = gameData.chainsDrawReplacementsApplied.getOrDefault(playerId, 0);
        if (activeChains == 0 || alreadyApplied >= activeChains) {
            gameData.chainsDrawReplacementsApplied.remove(playerId);
            return false;
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            gameData.chainsDrawReplacementsApplied.remove(playerId);
            graveyardService.resolveMillPlayer(gameData, playerId, 1);
            return true;
        }

        gameData.chainsDrawReplacementsApplied.put(playerId, alreadyApplied + 1);
        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(
                gameData, playerId, 1, DiscardFollowUp.rummage(1));
        return true;
    }

    private int countChainsOfMephistopheles(GameData gameData) {
        int count = 0;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                count += permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(ChainsOfMephistophelesDrawReplacement.class::isInstance)
                        .count();
            }
        }
        return count;
    }

    private boolean isDrawSkipped(GameData gameData, UUID playerId) {
        List<Card> library = gameData.playerDecks.get(playerId);
        boolean libraryEmpty = library == null || library.isEmpty();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                boolean skips = permanent.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(effect ->
                        effect instanceof SkipDrawReplacementEffect
                                || (libraryEmpty
                                && pid.equals(playerId)
                                && effect instanceof SkipDrawIfEmptyLibraryReplacementEffect));
                if (skips) return true;
            }
        }
        return false;
    }

    private Permanent findSharedFateSource(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(effect -> effect instanceof SharedFateDrawReplacement);
                if (hasEffect) {
                    return permanent;
                }
            }
        }
        return null;
    }

    /** Shared Fate replaces the draw with a face-down exile from the opponent's library. */
    private void resolveSharedFateDrawReplacement(GameData gameData, UUID playerId, Permanent source) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, playerId);
        List<Card> deck = gameData.playerDecks.get(opponentId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + "'s draw is replaced; ", source.getCard(), " exiles nothing."));
            return;
        }

        Card exiled = deck.removeFirst();
        exileService.exileCardFaceDown(gameData, opponentId, exiled, source.getId(), playerId);

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles a card face down from an opponent's library with ")
                .card(source.getCard())
                .text(" instead of drawing.")
                .build());
        log.info("Game {} - {} exiles the top card of an opponent's library face down with Shared Fate",
                gameData.id, playerName);
    }

    private Card findMaySkipDrawSourceCard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof MaySkipDrawReplacementEffect);
            if (hasEffect) {
                return permanent.getCard();
            }
        }
        return null;
    }

    private Permanent findUbaMaskSource(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(effect -> effect instanceof UbaMaskDrawReplacementEffect);
                if (hasEffect) {
                    return permanent;
                }
            }
        }
        return null;
    }

    /**
     * Uba Mask replacement: the card that would have been drawn is exiled face up instead, tracked
     * against Uba Mask, and the player who would have drawn it may play it this turn (normal timing
     * and costs). An empty library exiles nothing and does not lose the game — the draw was replaced.
     */
    private void resolveUbaMaskDrawReplacement(GameData gameData, UUID playerId, Permanent source) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + "'s library is empty; ", source.getCard(), " exiles nothing."));
            return;
        }

        Card exiled = deck.removeFirst();
        exileService.exileCard(gameData, playerId, exiled, source.getId());
        gameData.exilePlayPermissions.put(exiled.getId(), playerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(exiled.getId());

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles ").card(exiled)
                .text(" face up with ").card(source.getCard())
                .text(" instead of drawing (may play it this turn).").build());
        log.info("Game {} - {} exiles {} face up with Uba Mask instead of drawing",
                gameData.id, playerName, exiled.getName());
    }

    private Card findZursWeirdingSourceCard(GameData gameData) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(effect -> effect instanceof ZursWeirdingDrawReplacementEffect);
                if (hasEffect) {
                    return permanent.getCard();
                }
            }
        }
        return null;
    }

    private boolean isHandEmpty(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        return hand == null || hand.isEmpty();
    }

    public boolean hasQuantumRiddlerDrawReplacement(GameData gameData, UUID playerId) {
        return findQuantumRiddlerDrawReplacementSource(gameData, playerId) != null;
    }

    private Permanent findQuantumRiddlerDrawReplacementSource(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean active = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> isActiveQuantumRiddlerDrawReplacement(
                            gameData, permanent, playerId, effect));
            if (active) {
                return permanent;
            }
        }
        return null;
    }

    private boolean isActiveQuantumRiddlerDrawReplacement(GameData gameData, Permanent permanent,
                                                          UUID controllerId, CardEffect effect) {
        if (effect.getClass() == QuantumRiddlerDrawReplacementEffect.class) {
            return true;
        }
        if (effect.getClass() != ConditionalEffect.class) {
            return false;
        }

        ConditionalEffect conditional = (ConditionalEffect) effect;
        return conditional.wrapped().getClass()
                == QuantumRiddlerDrawReplacementEffect.class
                && conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forPermanent(permanent, controllerId));
    }

    private Card findEmptyHandDrawExtraSourceCard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof EmptyHandDrawExtraCardAndLoseLifeEffect);
            if (hasEffect) {
                return permanent.getCard();
            }
        }
        return null;
    }

    /**
     * Records that {@code playerId} is taking a draw during their own draw step, returning whether
     * this is the first such draw this turn.
     */
    private boolean markFirstDrawStepDraw(GameData gameData, UUID playerId) {
        if (!playerId.equals(gameData.activePlayerId) || gameData.currentStep != TurnStep.DRAW) {
            return false;
        }
        return gameData.drawStepFirstDrawTaken.add(playerId);
    }

    /** The opponent-controlled Notion Thief-style source that steals {@code playerId}'s extra draws. */
    private Card findOpponentExtraDrawsRedirectedSourceCard(GameData gameData, UUID playerId) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, playerId);
        if (opponentId == null) {
            return null;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(opponentId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof OpponentExtraDrawsRedirectedEffect);
            if (hasEffect) {
                return permanent.getCard();
            }
        }
        return null;
    }

    private Card findDoubleDrawSourceCard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> isActiveDoubleDrawReplacement(gameData, permanent, playerId, effect));
            if (hasEffect) {
                return permanent.getCard();
            }
        }
        return null;
    }

    private boolean isActiveDoubleDrawReplacement(GameData gameData, Permanent permanent,
                                                   UUID controllerId, CardEffect effect) {
        if (effect.getClass() == DoubleDrawReplacementEffect.class) {
            return true;
        }
        if (effect.getClass() != ConditionalEffect.class) {
            return false;
        }

        ConditionalEffect conditional = (ConditionalEffect) effect;
        return conditional.wrapped().getClass() == DoubleDrawReplacementEffect.class
                && conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forPermanent(permanent, controllerId));
    }

    private Permanent findCounterDrawReplacementSource(GameData gameData, UUID playerId,
                                                        CounterType counterType) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(CounterDrawReplacementEffect.class::isInstance)
                    .map(CounterDrawReplacementEffect.class::cast)
                    .anyMatch(effect -> effect.counterType() == counterType);
            if (hasEffect) {
                return permanent;
            }
        }
        return null;
    }

    private Permanent findCounterThresholdDrawReplacementSource(GameData gameData, UUID playerId,
                                                                  CounterType counterType) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean active = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(CounterThresholdDrawReplacementEffect.class::isInstance)
                    .map(CounterThresholdDrawReplacementEffect.class::cast)
                    .anyMatch(effect -> effect.counterType() == counterType
                            && permanent.getCounterCount(effect.counterType()) >= effect.minimumCounters());
            if (active) {
                return permanent;
            }
        }
        return null;
    }

    private Card findExceptFirstDoubleDrawSourceCard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof DoubleDrawExceptFirstDrawStepDrawEffect);
            if (hasEffect) {
                return permanent.getCard();
            }
        }
        return null;
    }

    private Card findReturnFromGraveyardInsteadOfDrawSourceCard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof ReturnFromGraveyardInsteadOfDrawEffect);
            if (hasEffect) {
                return permanent.getCard();
            }
        }
        return null;
    }

    private Card findAbundanceSourceCard(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasAbundanceEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof AbundanceDrawReplacementEffect);
            if (hasAbundanceEffect) {
                return permanent.getCard();
            }
        }
        return null;
    }

    private Permanent findRevealCreaturesDrawReplacementSource(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof RevealTopCardsCreaturesToHandDrawReplacementEffect);
            if (hasEffect) {
                return permanent;
            }
        }
        return null;
    }

    private Permanent findLookAtTopChooseOneToHandDrawReplacementSource(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof LookAtTopCardsChooseOneToHandDrawReplacementEffect);
            if (hasEffect) {
                return permanent;
            }
        }
        return null;
    }

    /**
     * Resolves a top-card hand replacement: look at the top {@code lookCount} cards of the drawing
     * player's library, put one of them into their hand, and put the rest in the effect's declared
     * destination (an async {@link PendingInteraction.LibraryRevealChoice} when a choice is needed).
     *
     * <p>The draw is replaced entirely, so the kept card is put into hand rather than "drawn" (no draw
     * triggers, no cards-drawn bookkeeping), and an empty library does not lose the game — the player
     * simply looks at nothing.
     */
    private void resolveLookAtTopChooseOneToHandDrawReplacement(GameData gameData, UUID playerId, Permanent source) {
        // A prior look from the same multi-card draw is still awaiting a choice; don't stack another
        // interaction over it (consistent with Sages of the Anima's multi-draw handling).
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        var replacement = source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(effect -> effect instanceof LookAtTopCardsChooseOneToHandDrawReplacementEffect)
                .map(LookAtTopCardsChooseOneToHandDrawReplacementEffect.class::cast)
                .findFirst().orElse(null);
        if (replacement == null) {
            return;
        }
        int lookCount = replacement.lookCount();
        boolean restToGraveyard = replacement.restToGraveyard();

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        int actual = deck == null ? 0 : Math.min(lookCount, deck.size());
        if (actual == 0) {
            // Library empty — the draw is replaced, so nothing happens and the player does not lose.
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + "'s library is empty; ", source.getCard(), " looks at no cards."));
            log.info("Game {} - {} looks at no cards for {} (empty library)",
                    gameData.id, playerName, source.getCard().getName());
            return;
        }

        List<Card> looked = new ArrayList<>();
        for (int i = 0; i < actual; i++) {
            looked.add(deck.removeFirst());
        }

        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " looks at the top " + actual + " card" + (actual == 1 ? "" : "s")
                        + " of their library with ", source.getCard(), "."));
        log.info("Game {} - {} looks at top {} cards for {}",
                gameData.id, playerName, actual, source.getCard().getName());

        if (actual == 1) {
            // Only one card to look at — it goes to hand, nothing is left for the bottom.
            gameData.addCardToHand(playerId, looked.getFirst());
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " puts ", looked.getFirst(), " into their hand."));
            return;
        }

        List<UUID> cardIds = looked.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                playerId, looked, cardIds, restToGraveyard, true, !restToGraveyard,
                false, false, 0, null, 1,
                restToGraveyard
                        ? "Put one of these cards into your hand and the rest into your graveyard."
                        : "Put one of these cards into your hand and the rest on the bottom of your library in any order."));
    }

    private Permanent findRevealTopCreatureToGraveyardElseDrawSource(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return null;
        }

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof RevealTopCreatureToGraveyardElseDrawReplacementEffect);
            if (hasEffect) {
                return permanent;
            }
        }
        return null;
    }

    /**
     * Enduring Renewal replacement: reveal the top card of the drawing player's library. If it's a
     * creature card, put it into their graveyard (not drawn). Otherwise, draw that card (a real draw).
     * An empty library reveals nothing and does not lose the game — the draw was replaced.
     */
    private void resolveRevealTopCreatureToGraveyardElseDraw(GameData gameData, UUID playerId, Permanent source) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + "'s library is empty; ", source.getCard(), " reveals no cards."));
            log.info("Game {} - {} reveals no cards for {} (empty library)",
                    gameData.id, playerName, source.getCard().getName());
            return;
        }

        Card revealed = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(revealed)
                .text(" with ")
                .card(source.getCard())
                .text(".")
                .build());
        log.info("Game {} - {} reveals {} with {}",
                gameData.id, playerName, revealed.getName(), source.getCard().getName());

        if (revealed.hasType(CardType.CREATURE)) {
            deck.removeFirst();
            graveyardService.addCardToGraveyard(gameData, playerId, revealed, Zone.LIBRARY);
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " puts ", revealed, " into their graveyard."));
            log.info("Game {} - {} puts revealed creature {} into graveyard (Enduring Renewal)",
                    gameData.id, playerName, revealed.getName());
        } else {
            // Otherwise draw a card — the revealed card is still on top; use performDrawCard so this
            // is a real draw (triggers, empty-library loss) and does not re-enter the replacement.
            performDrawCard(gameData, playerId);
        }
    }

    /**
     * Sages of the Anima replacement: reveal the top {@code revealCount} cards of the drawing player's
     * library, put every revealed creature card into their hand, and put the rest on the bottom of their
     * library in any order (an async {@link PendingInteraction.LibraryReorder} when two or more remain).
     *
     * <p>The draw is replaced entirely, so the revealed creatures are put into hand rather than "drawn"
     * (no draw triggers, no cards-drawn bookkeeping), and an empty library does not lose the game — the
     * player simply reveals nothing.
     */
    private void resolveRevealCreaturesDrawReplacement(GameData gameData, UUID playerId, Permanent source) {
        // A prior reveal from the same multi-card draw is still awaiting a bottom-order choice; don't
        // stack another interaction over it (consistent with Forbidden Crypt's multi-draw handling).
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }

        int revealCount = source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(effect -> effect instanceof RevealTopCardsCreaturesToHandDrawReplacementEffect)
                .map(effect -> ((RevealTopCardsCreaturesToHandDrawReplacementEffect) effect).revealCount())
                .findFirst().orElse(0);

        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        int actual = deck == null ? 0 : Math.min(revealCount, deck.size());
        if (actual == 0) {
            // Library empty — the draw is replaced, so nothing happens and the player does not lose.
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + "'s library is empty; ", source.getCard(), " reveals no cards."));
            log.info("Game {} - {} reveals no cards for {} (empty library)",
                    gameData.id, playerName, source.getCard().getName());
            return;
        }

        List<Card> revealed = new ArrayList<>();
        for (int i = 0; i < actual; i++) {
            revealed.add(deck.removeFirst());
        }

        String revealedNames = revealed.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " reveals " + revealedNames + " with ", source.getCard(), "."));
        log.info("Game {} - {} reveals top {} cards for {}",
                gameData.id, playerName, actual, source.getCard().getName());

        List<Card> creatures = new ArrayList<>();
        List<Card> rest = new ArrayList<>();
        for (Card card : revealed) {
            if (card.hasType(CardType.CREATURE)) {
                creatures.add(card);
            } else {
                rest.add(card);
            }
        }

        for (Card creature : creatures) {
            gameData.addCardToHand(playerId, creature);
        }
        if (!creatures.isEmpty()) {
            String creatureNames = creatures.stream().map(Card::getName).collect(Collectors.joining(", "));
            gameLogService.append(gameData, GameLog.text(
                    playerName + " puts " + creatureNames + " into their hand."));
        }

        // Put the non-creature cards on the bottom of the library in any order.
        if (rest.size() == 1) {
            deck.add(rest.getFirst());
            gameLogService.append(gameData, GameLog.text(
                    playerName + " puts 1 card on the bottom of their library."));
        } else if (rest.size() >= 2) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    playerId, new ArrayList<>(rest), true, playerId,
                    "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
        }
    }

    /**
     * Aladdin's Lamp replacement: look at the top X cards of the player's library, keep the chosen
     * one, put the rest on the bottom in a random order, then draw a card (the kept one). With one or
     * zero cards to look at, this is just a normal draw. Otherwise a {@link PendingInteraction.LibrarySearch}
     * lets the player pick which card to keep; the bottoming-plus-final-draw completes in
     * {@code LibraryChoiceHandlerService} (the {@code DRAW_CHOSEN_REST_TO_BOTTOM_RANDOM} destination).
     */
    /**
     * Mangara's Tome's replaced draw: the top card of the pile exiled with {@code pileSourceId} is
     * put into its owner's hand instead of the draw. The draw is replaced either way — an empty pile
     * simply means nothing is put into a hand (no card is drawn, no draw triggers fire).
     */
    private void resolveNextDrawFromExiledPile(GameData gameData, UUID playerId, UUID pileSourceId) {
        var top = gameData.topOfExilePile(pileSourceId);
        if (top == null) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                    + "'s exiled pile is empty, so no card is put into a hand."));
            log.info("Game {} - exiled pile empty; replaced draw does nothing", gameData.id);
            return;
        }
        gameData.removeFromExile(top.card().getId());
        gameData.addCardToHand(top.ownerId(), top.card());
        String ownerName = gameData.playerIdToName.get(top.ownerId());
        gameLogService.append(gameData, GameLog.textCardText(ownerName + " puts ", top.card(),
                " from the exiled pile into their hand instead of drawing."));
        log.info("Game {} - {} puts {} from the exiled pile into their hand instead of drawing",
                gameData.id, ownerName, top.card().getName());
    }

    private void resolveNextDrawLookAtTop(GameData gameData, UUID playerId, int x) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty()) {
            // No cards to look at — "then draw a card" from an empty library (handles the loss).
            performDrawCard(gameData, playerId);
            return;
        }

        int lookCount = Math.min(x, deck.size());
        if (lookCount <= 1) {
            // Looking at a single card (or X == 1) — nothing to put on the bottom; just draw it.
            performDrawCard(gameData, playerId);
            return;
        }

        List<Card> looked = new ArrayList<>();
        for (int i = 0; i < lookCount; i++) {
            looked.add(deck.removeFirst());
        }

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData,
                GameLog.text(playerName + " looks at the top " + lookCount + " cards of their library."));
        log.info("Game {} - {} looks at top {} cards (Aladdin's Lamp)", gameData.id, playerName, lookCount);

        String prompt = "Look at the top " + lookCount + " cards. Choose one to draw; the rest go to the "
                + "bottom of your library in a random order.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(playerId, looked)
                        .sourceCards(new ArrayList<>(looked))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.DRAW_CHOSEN_REST_TO_BOTTOM_RANDOM)
                        .build(),
                prompt,
                false));
    }

    /** Urabrask, Heretic Praetor's replaced draw: exile the top card and let its owner play it this turn. */
    private void resolveNextDrawExileTopCardMayPlayThisTurn(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty; the replaced draw exiles nothing."));
            return;
        }

        Card exiled = deck.removeFirst();
        exileService.exileCard(gameData, playerId, exiled);
        gameData.exilePlayPermissions.put(exiled.getId(), playerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(exiled.getId());

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles ").card(exiled)
                .text(" from the top of their library instead of drawing (may play it this turn).")
                .build());
        log.info("Game {} - {} exiles {} instead of drawing (Urabrask)",
                gameData.id, playerName, exiled.getName());
    }

    void performDrawCard(GameData gameData, UUID playerId) {
        if (preventDrawIfNeeded(gameData, playerId)) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(playerId);

        if (deck == null || deck.isEmpty()) {
            gameData.playersAttemptedDrawFromEmptyLibrary.add(playerId);
            String logEntry = gameData.playerIdToName.get(playerId) + " has no cards to draw.";
            gameLogService.append(gameData, GameLog.text(logEntry));

            // Check for Laboratory Maniac-style replacement: win instead of lose
            if (hasWinOnEmptyLibraryDraw(gameData, playerId)) {
                UUID opponentId = gameQueryService.getOpponentId(gameData, playerId);
                if (gameOutcomeService.canPlayerWinGame(gameData, playerId)) {
                    String winLog = gameData.playerIdToName.get(playerId) + " wins the game (drew from an empty library with a replacement effect).";
                    gameLogService.append(gameData, GameLog.text(winLog));
                    log.info("Game {} - {} wins (empty library draw replacement)", gameData.id, gameData.playerIdToName.get(playerId));
                    gameOutcomeService.declareWinner(gameData, playerId);
                } else {
                    String blockedLog = gameData.playerIdToName.get(playerId) + "'s win condition is met but " +
                            gameData.playerIdToName.get(opponentId) + " can't lose the game.";
                    gameLogService.append(gameData, GameLog.text(blockedLog));
                    log.info("Game {} - {} empty library win prevented — opponent can't lose", gameData.id, gameData.playerIdToName.get(playerId));
                }
                return;
            }

            // CR 704.5b — player who attempted to draw from an empty library loses the game
            if (gameOutcomeService.resolveLoss(gameData, playerId, LossReason.EMPTY_LIBRARY) == LossOutcome.LOSES) {
                UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                String lossLog = gameData.playerIdToName.get(playerId) + " attempted to draw from an empty library and loses the game.";
                gameLogService.append(gameData, GameLog.text(lossLog));
                log.info("Game {} - {} loses (drew from empty library)", gameData.id, gameData.playerIdToName.get(playerId));
                gameOutcomeService.declareWinner(gameData, winnerId);
            }
            return;
        }

        Card drawn = deck.removeFirst();
        gameData.addCardToHand(playerId, drawn);

        // Track cards drawn this turn (for Molten Psyche, etc.)
        gameData.cardsDrawnThisTurn.merge(playerId, 1, Integer::sum);
        gameData.cardsDrawnThisTurnIds.computeIfAbsent(playerId, k -> new ArrayList<>()).add(drawn.getId());

        String logEntry = gameData.playerIdToName.get(playerId) + " draws a card.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} draws a card from effect", gameData.id, gameData.playerIdToName.get(playerId));

        checkControllerDrawTriggers(gameData, playerId, drawn);
        checkOpponentDrawTriggers(gameData, playerId);
        checkEnchantedPlayerDrawTriggers(gameData, playerId);
        checkBoobyTraps(gameData, playerId, drawn);
        checkRevealFirstDrawTriggers(gameData, playerId, drawn);
        breathstealersCryptDrawReplacementHandler.afterDraw(gameData, playerId, drawn);
        checkMiracleReveal(gameData, playerId, drawn);
    }

    /**
     * Miracle (CR 702.94a): if the drawn card has a {@link MiracleCast} option and this is the
     * first card the player has drawn this turn, offer to reveal it. Accepting queues the miracle
     * triggered ability ({@link com.github.laxika.magicalvibes.model.effect.MiracleMayCastEffect}).
     */
    private void checkMiracleReveal(GameData gameData, UUID drawingPlayerId, Card drawn) {
        if (gameData.cardsDrawnThisTurn.getOrDefault(drawingPlayerId, 0) != 1) {
            return;
        }
        if (drawn.getCastingOption(MiracleCast.class).isEmpty()) {
            return;
        }

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                drawn,
                drawingPlayerId,
                List.of(new MiracleRevealEffect()),
                "Reveal " + drawn.getName() + " for its miracle ability?"
        ));
        log.info("Game {} - offering miracle reveal for {}", gameData.id, drawn.getName());
    }

    /**
     * Rowen: the controller reveals the first card they draw each turn; whenever that card is a basic
     * land, a "draw a card" triggered ability is put onto the stack. Only the turn's first draw is
     * revealed — {@code cardsDrawnThisTurn} has already been incremented for this draw, so first draw
     * means a count of exactly 1. The extra draw is therefore never revealed itself.
     */
    private void checkRevealFirstDrawTriggers(GameData gameData, UUID drawingPlayerId, Card drawn) {
        if (gameData.cardsDrawnThisTurn.getOrDefault(drawingPlayerId, 0) != 1) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(drawingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : new ArrayList<>(battlefield)) {
            boolean reveals = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof RevealFirstDrawDrawOnBasicLandEffect);
            if (!reveals) continue;

            String drawerName = gameData.playerIdToName.get(drawingPlayerId);
            gameLogService.append(gameData, GameLog.builder()
                    .text(drawerName + " reveals ")
                    .card(drawn)
                    .text(" with ")
                    .card(perm.getCard())
                    .text(".")
                    .build());

            boolean basicLand = drawn.hasType(CardType.LAND)
                    && drawn.getSupertypes().contains(CardSupertype.BASIC);
            if (basicLand) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        drawingPlayerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(new DrawCardEffect(1))),
                        drawingPlayerId,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), " triggers — draw a card."));
                log.info("Game {} - {} triggers on {} revealing a basic land",
                        gameData.id, perm.getCard().getName(), drawerName);
            }
        }
    }

    /**
     * Booby Trap: an opponent's Booby Trap makes the drawing (chosen) player reveal each card they
     * draw; when the revealed card's name matches the trap's chosen name, the trap is sacrificed and
     * — if it was — deals 10 damage to that player. The chosen player is always an opponent of the
     * trap's controller.
     */
    private void checkBoobyTraps(GameData gameData, UUID drawingPlayerId, Card drawn) {
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            if (controllerId.equals(drawingPlayerId)) return;

            for (Permanent perm : new ArrayList<>(battlefield)) {
                boolean isBoobyTrap = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof BoobyTrapEffect);
                if (!isBoobyTrap) continue;

                String drawerName = gameData.playerIdToName.get(drawingPlayerId);
                gameLogService.append(gameData, GameLog.builder()
                        .text(drawerName + " reveals ")
                        .card(drawn)
                        .text(" with ")
                        .card(perm.getCard())
                        .text(".")
                        .build());

                if (drawn.getName().equals(perm.getChosenName())) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(new SacrificeSelfThenEffect(
                                    new DealDamageToPlayersEffect(10, DamageRecipient.TARGET_PLAYER)))),
                            drawingPlayerId,
                            perm.getId()
                    ));
                    gameLogService.append(gameData, GameLog.builder()
                            .card(perm.getCard())
                            .text(" triggers on " + drawerName + " drawing ")
                            .card(drawn)
                            .text(".")
                            .build());
                    log.info("Game {} - Booby Trap triggers on {} drawing {}",
                            gameData.id, drawerName, drawn.getName());
                }
            }
        });
    }

    public void checkControllerDrawTriggers(GameData gameData, UUID drawingPlayerId, Card drawn) {
        checkControllerDrawTriggerSlot(gameData, drawingPlayerId, EffectSlot.ON_CONTROLLER_DRAWS, drawn);
        if (gameData.cardsDrawnThisTurn.getOrDefault(drawingPlayerId, 0) == 2) {
            checkControllerDrawTriggerSlot(
                    gameData, drawingPlayerId, EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD, drawn);
            checkGraveyardControllerDrawTriggerSlot(
                    gameData, drawingPlayerId, EffectSlot.GRAVEYARD_ON_CONTROLLER_DRAWS_SECOND_CARD);
        }

        // Emblem draw triggers (e.g. Teferi, Hero of Dominaria emblem)
        checkEmblemDrawTriggers(gameData, drawingPlayerId);
    }

    private void checkControllerDrawTriggerSlot(GameData gameData, UUID drawingPlayerId,
                                                EffectSlot slot, Card drawn) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(drawingPlayerId);
        if (battlefield == null) return;
        int cardsDrawnThisTurn = gameData.cardsDrawnThisTurn.getOrDefault(drawingPlayerId, 0);

        for (Permanent perm : battlefield) {
            List<CardEffect> drawEffects = perm.getCard().getEffects(slot);
            drawEffects = drawEffects == null ? new ArrayList<>() : new ArrayList<>(drawEffects);
            drawEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(gameData, perm, slot));
            if (drawEffects.isEmpty()) continue;

            for (CardEffect authoredEffect : drawEffects) {
                CardEffect effect = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, perm, authoredEffect);
                if (effect == null) continue;

                if (effect instanceof FirstDrawRevealTriggerEffect firstDraw) {
                    if (drawn == null
                            || (firstDraw.onlyOnControllerTurn()
                            && !drawingPlayerId.equals(gameData.activePlayerId))
                            || gameData.cardsDrawnThisTurn.getOrDefault(drawingPlayerId, 0) != 1) {
                        continue;
                    }

                    if (firstDraw.revealBeforeChoice()) {
                        String drawerName = gameData.playerIdToName.get(drawingPlayerId);
                        gameLogService.append(gameData, GameLog.builder()
                                .text(drawerName + " reveals ")
                                .card(drawn)
                                .text(" with ")
                                .card(perm.getCard())
                                .text(".")
                                .build());
                    }
                    effect = firstDraw.effectFor(drawn);
                    if (effect == null) {
                        continue;
                    }
                }
                if (effect instanceof DrawTriggerEffect drawTrigger) {
                    effect = drawTrigger.effectForDrawCount(cardsDrawnThisTurn).orElse(null);
                    if (effect == null) {
                        continue;
                    }
                }
                if (!effect.triggersOnControllerDrawCount(cardsDrawnThisTurn)) {
                    continue;
                }

                if (effect instanceof ConditionalEffect conditional && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(perm, drawingPlayerId))) {
                    continue;
                }

                // Equipment-granted draw trigger (Diviner's Wand): the ability is granted to the
                // equipped creature, so an unattached Equipment has no such ability — no trigger.
                if (effect instanceof BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect
                        && perm.getAttachedTo() == null) {
                    continue;
                }

                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), drawingPlayerId, may);
                    OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                } else if (effect.targetSpec().declares(TargetPredicates.anyTarget())
                        || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                    // Targeted draw trigger: the controller must choose a target before the ability
                    // goes on the stack. This includes player-only targets such as "target opponent".
                    gameData.queueInteraction(new PermanentChoiceContext.DrawTriggerAnyTarget(
                            perm.getCard(),
                            drawingPlayerId,
                            new ArrayList<>(List.of(effect)),
                            perm.getId()
                    ));

                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} controller-draw any-target trigger queued",
                            gameData.id, perm.getCard().getName());
                    OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                } else if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        && (perm.getCard().getEffectTargetIndex(effect) >= 0
                        || perm.getCard().getEffectTargetIndex(authoredEffect) >= 0)) {
                    // A permanent-target draw trigger (Mantle of Tides): choose the target as the
                    // ability is put on the stack, using the card's declared target filter.
                    gameData.queueInteraction(new PermanentChoiceContext.DrawTriggerPermanentTarget(
                            perm.getCard(),
                            drawingPlayerId,
                            new ArrayList<>(List.of(effect)),
                            perm.getId(),
                            perm.getCard().getTargetFilter()
                    ));

                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} controller-draw permanent-target trigger queued",
                            gameData.id, perm.getCard().getName());
                    OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            drawingPlayerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            drawingPlayerId,
                            perm.getId()
                    ));

                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} controller-draw trigger pushed onto stack",
                            gameData.id, perm.getCard().getName());
                    OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                }
            }
        }

    }

    private void checkGraveyardControllerDrawTriggerSlot(GameData gameData, UUID drawingPlayerId,
                                                         EffectSlot slot) {
        List<Card> graveyard = gameData.playerGraveyards.get(drawingPlayerId);
        if (graveyard == null) return;

        int cardsDrawnThisTurn = gameData.cardsDrawnThisTurn.getOrDefault(drawingPlayerId, 0);
        for (Card card : new ArrayList<>(graveyard)) {
            for (CardEffect effect : card.getEffects(slot)) {
                if (!effect.triggersOnControllerDrawCount(cardsDrawnThisTurn)) continue;

                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        drawingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));

                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard ability triggers on second card draw",
                        gameData.id, card.getName());
            }
        }
    }

    public void checkControllerDrawTriggers(GameData gameData, UUID drawingPlayerId) {
        checkControllerDrawTriggers(gameData, drawingPlayerId, null);
    }

    private void checkEmblemDrawTriggers(GameData gameData, UUID drawingPlayerId) {
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(drawingPlayerId)) continue;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof ExileTargetOpponentPermanentOnDrawEffect) {
                    gameData.queueInteraction(new PermanentChoiceContext.EmblemTriggerTarget(
                            "Teferi's emblem",
                            emblem.controllerId(),
                            List.of(new ExileTargetPermanentEffect()),
                            emblem.sourceCard(),
                            true
                    ));
                }
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class)) {
            triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
        }
    }

    private boolean hasWinOnEmptyLibraryDraw(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent perm : battlefield) {
            boolean hasEffect = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof WinGameOnEmptyLibraryDrawEffect);
            if (hasEffect) return true;
        }
        return false;
    }

    public void checkOpponentDrawTriggers(GameData gameData, UUID drawingPlayerId) {
        int cardsDrawnThisTurn = gameData.cardsDrawnThisTurn.getOrDefault(drawingPlayerId, 0);
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(drawingPlayerId)) return;

            for (Permanent perm : battlefield) {
                List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DRAWS);
                if (drawEffects == null || drawEffects.isEmpty()) continue;

                for (CardEffect authoredEffect : drawEffects) {
                    CardEffect effect = authoredEffect;
                    if (effect instanceof DrawTriggerEffect drawTrigger) {
                        effect = drawTrigger.effectForDrawCount(cardsDrawnThisTurn).orElse(null);
                        if (effect == null) continue;
                    }
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), playerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(effect)),
                                drawingPlayerId,
                                perm.getId()
                        ));
                    }

                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on opponent draw", gameData.id, perm.getCard().getName());
                }
            }
        });
    }

    public void checkEnchantedPlayerDrawTriggers(GameData gameData, UUID drawingPlayerId) {
        int cardsDrawnThisTurn = gameData.cardsDrawnThisTurn.getOrDefault(drawingPlayerId, 0);
        gameData.forEachBattlefield((auraControllerId, battlefield) -> {
            if (auraControllerId.equals(drawingPlayerId)) return;

            for (Permanent perm : battlefield) {
                if (!perm.isAttached() || !drawingPlayerId.equals(perm.getAttachedTo())) continue;

                List<CardEffect> drawEffects = perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PLAYER_DRAWS);
                if (drawEffects == null || drawEffects.isEmpty()) continue;

                for (CardEffect authoredEffect : drawEffects) {
                    CardEffect effect = authoredEffect;
                    if (effect instanceof DrawTriggerEffect drawTrigger) {
                        effect = drawTrigger.effectForDrawCount(cardsDrawnThisTurn).orElse(null);
                        if (effect == null) continue;
                    }
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), auraControllerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                auraControllerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(effect)),
                                drawingPlayerId,
                                perm.getId()
                        ));
                    }

                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on enchanted player draw", gameData.id, perm.getCard().getName());
                }
            }
        });
    }
}
