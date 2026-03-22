package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeOrExileEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealTypeTransformEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayReturnExiledCardOrDrawEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCounterTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceSingleDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUnlessExileCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SphinxAmbassadorPutOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.DestructionResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardReturnResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayAbilityHandlerService {

    private final InputCompletionService inputCompletionService;
    private final MayCastHandlerService mayCastHandlerService;
    private final MayCopyHandlerService mayCopyHandlerService;
    private final MayPenaltyChoiceHandlerService mayPenaltyChoiceHandlerService;
    private final MayMiscHandlerService mayMiscHandlerService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;
    private final DestructionResolutionService destructionResolutionService;
    private final GraveyardReturnResolutionService graveyardReturnResolutionService;

    public void handleMayAbilityChosen(GameData gameData, Player player, boolean accepted) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.MAY_ABILITY_CHOICE)) {
            throw new IllegalStateException("Not awaiting may ability choice");
        }
        InteractionContext.MayAbilityChoice mayAbilityChoice = gameData.interaction.mayAbilityChoiceContext();
        if (mayAbilityChoice == null || !player.getId().equals(mayAbilityChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        PendingMayAbility ability = gameData.pendingMayAbilities.removeFirst();
        gameData.interaction.clearAwaitingInput();
        gameData.interaction.clearMayAbilityChoice();

        // Pile separation: permanent-pile (Liliana) vs card-pile (Boneyard Parley)
        if (gameData.pendingPileSeparation) {
            if (!gameData.pendingPileSeparationCards.isEmpty()) {
                graveyardReturnResolutionService.completeCardPileSeparationStep2(gameData, accepted);
            } else {
                destructionResolutionService.completePileSeparationStep2(gameData, accepted);
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // CR 603.5: resolution-time "you may" choice for triggered abilities on the stack.
        if (gameData.resolvingMayEffectFromStack) {
            handleResolutionTimeMayChoice(gameData, player, accepted, ability);
            return;
        }

        // Pending equipment attach — e.g. Auriok Survivors "you may attach it to this creature"
        UUID pendingEquipId = gameData.interaction.pendingEquipmentAttachEquipmentId();
        UUID pendingTargetId = gameData.interaction.pendingEquipmentAttachTargetId();
        if (pendingEquipId != null && pendingTargetId != null) {
            mayMiscHandlerService.handleEquipmentAttachChoice(gameData, player, accepted, pendingEquipId, pendingTargetId);
            return;
        }

        // Cast-from-library-without-paying — e.g. Galvanoth (second phase: cast prompt)
        CastTopOfLibraryWithoutPayingManaCostEffect castFromLibEffect = ability.effects().stream()
                .filter(e -> e instanceof CastTopOfLibraryWithoutPayingManaCostEffect)
                .map(e -> (CastTopOfLibraryWithoutPayingManaCostEffect) e)
                .findFirst().orElse(null);
        if (castFromLibEffect != null && castFromLibEffect.castableTypes().contains(ability.sourceCard().getType())) {
            mayCastHandlerService.handleCastFromLibraryChoice(gameData, player, accepted, ability);
            return;
        }

        // Play-from-library-or-exile — e.g. Djinn of Wishes (play any card or exile)
        boolean isPlayFromLibraryOrExile = ability.effects().stream()
                .anyMatch(e -> e instanceof RevealTopCardMayPlayFreeOrExileEffect);
        if (isPlayFromLibraryOrExile) {
            mayCastHandlerService.handlePlayFromLibraryOrExileChoice(gameData, player, accepted, ability);
            return;
        }

        // Explore — may put revealed non-land card into graveyard
        boolean isExplore = ability.effects().stream()
                .anyMatch(e -> e instanceof ExploreEffect);
        if (isExplore) {
            mayMiscHandlerService.handleExploreMayGraveyardChoice(gameData, player, accepted);
            return;
        }

        // Reveal top card creature-to-battlefield or may-bottom — e.g. Lurking Predators
        boolean isRevealCreatureOrBottom = ability.effects().stream()
                .anyMatch(e -> e instanceof RevealTopCardCreatureToBattlefieldOrMayBottomEffect);
        if (isRevealCreatureOrBottom) {
            mayMiscHandlerService.handleRevealTopCardMayBottomChoice(gameData, player, accepted);
            return;
        }

        // Look at top card, may reveal to transform — e.g. Delver of Secrets
        // Per ruling (2011-09-22): you may reveal even if it's not an instant or sorcery.
        // The card stays on top of your library. Transform only happens if revealed card matches.
        LookAtTopCardMayRevealTypeTransformEffect revealTypeTransform = ability.effects().stream()
                .filter(e -> e instanceof LookAtTopCardMayRevealTypeTransformEffect)
                .map(e -> (LookAtTopCardMayRevealTypeTransformEffect) e)
                .findFirst().orElse(null);
        if (revealTypeTransform != null) {
            if (accepted) {
                List<Card> deck = gameData.playerDecks.get(ability.controllerId());
                if (!deck.isEmpty()) {
                    Card topCard = deck.getFirst();
                    String revealLog = player.getUsername() + " reveals " + topCard.getName() + " from the top of their library.";
                    gameBroadcastService.logAndBroadcast(gameData, revealLog);

                    // Transform only if the revealed card matches the required types
                    boolean matches = revealTypeTransform.cardTypes().contains(topCard.getType())
                            || topCard.getAdditionalTypes().stream().anyMatch(revealTypeTransform.cardTypes()::contains);
                    if (matches) {
                        Permanent self = ability.sourcePermanentId() != null
                                ? gameQueryService.findPermanentById(gameData, ability.sourcePermanentId()) : null;
                        if (self != null && !self.isTransformed()) {
                            Card backFace = self.getOriginalCard().getBackFaceCard();
                            if (backFace != null) {
                                String frontName = self.getCard().getName();
                                self.setCard(backFace);
                                self.setTransformed(true);
                                String transformLog = frontName + " transforms into " + backFace.getName() + ".";
                                gameBroadcastService.logAndBroadcast(gameData, transformLog);
                                log.info("Game {} - {} transforms into {} (revealed instant/sorcery)",
                                        gameData.id, frontName, backFace.getName());
                            }
                        }
                    } else {
                        log.info("Game {} - {} revealed {} but it's not a matching type, no transform",
                                gameData.id, player.getUsername(), topCard.getName());
                    }
                }
            } else {
                String logEntry = player.getUsername() + " chooses not to reveal.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} declines to reveal top card ({})", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Cast-from-graveyard — e.g. Chancellor of the Spires
        CastTargetInstantOrSorceryFromGraveyardEffect castFromGraveyardEffect = ability.effects().stream()
                .filter(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect)
                .map(e -> (CastTargetInstantOrSorceryFromGraveyardEffect) e)
                .findFirst().orElse(null);
        if (castFromGraveyardEffect != null) {
            mayCastHandlerService.handleCastFromGraveyardChoice(gameData, player, accepted, ability, castFromGraveyardEffect);
            return;
        }

        // May-not-untap choice from untap step (e.g. Rust Tick)
        boolean isMayNotUntap = ability.effects().stream().anyMatch(e -> e instanceof MayNotUntapDuringUntapStepEffect);
        if (isMayNotUntap) {
            mayMiscHandlerService.handleMayNotUntapChoice(gameData, player, accepted, ability);
            return;
        }

        // Leyline pregame choice (CR 103.6a) — put card onto battlefield from opening hand
        boolean isLeyline = ability.effects().stream().anyMatch(e -> e instanceof LeylineStartOnBattlefieldEffect);
        if (isLeyline) {
            mayMiscHandlerService.handleLeylineChoice(gameData, player, accepted, ability);
            return;
        }

        // Opening hand delayed counter trigger (e.g. Chancellor of the Annex)
        RegisterDelayedCounterTriggerEffect delayedCounterTrigger = ability.effects().stream()
                .filter(e -> e instanceof RegisterDelayedCounterTriggerEffect)
                .map(e -> (RegisterDelayedCounterTriggerEffect) e)
                .findFirst().orElse(null);
        if (delayedCounterTrigger != null) {
            mayMiscHandlerService.handleOpeningHandDelayedCounterTrigger(gameData, player, accepted, ability, delayedCounterTrigger);
            return;
        }

        // Opening hand delayed mana trigger (e.g. Chancellor of the Tangle)
        RegisterDelayedManaTriggerEffect delayedManaTrigger = ability.effects().stream()
                .filter(e -> e instanceof RegisterDelayedManaTriggerEffect)
                .map(e -> (RegisterDelayedManaTriggerEffect) e)
                .findFirst().orElse(null);
        if (delayedManaTrigger != null) {
            mayMiscHandlerService.handleOpeningHandDelayedManaTrigger(gameData, player, accepted, ability, delayedManaTrigger);
            return;
        }

        // Counter-unless-pays — handled via the may ability system
        boolean isCounterUnlessPays = ability.effects().stream().anyMatch(e -> e instanceof CounterUnlessPaysEffect);
        if (isCounterUnlessPays) {
            mayPenaltyChoiceHandlerService.handleCounterUnlessPaysChoice(gameData, player, accepted, ability);
            return;
        }

        // Lose-life-unless-discard — handled via the may ability system
        boolean isLoseLifeUnlessDiscard = ability.effects().stream().anyMatch(e -> e instanceof LoseLifeUnlessDiscardEffect);
        if (isLoseLifeUnlessDiscard) {
            mayPenaltyChoiceHandlerService.handleLoseLifeUnlessDiscardChoice(gameData, player, accepted, ability);
            return;
        }

        // Lose-life-unless-pays — handled via the may ability system
        boolean isLoseLifeUnlessPays = ability.effects().stream().anyMatch(e -> e instanceof LoseLifeUnlessPaysEffect);
        if (isLoseLifeUnlessPays) {
            mayPenaltyChoiceHandlerService.handleLoseLifeUnlessPaysChoice(gameData, player, accepted, ability);
            return;
        }

        // Opponent may return exiled card to hand, or controller draws N (e.g. Distant Memories)
        OpponentMayReturnExiledCardOrDrawEffect opponentExileChoice = ability.effects().stream()
                .filter(e -> e instanceof OpponentMayReturnExiledCardOrDrawEffect)
                .map(e -> (OpponentMayReturnExiledCardOrDrawEffect) e)
                .findFirst().orElse(null);
        if (opponentExileChoice != null) {
            mayPenaltyChoiceHandlerService.handleOpponentExileChoice(gameData, player, accepted, ability, opponentExileChoice);
            return;
        }

        // Discard-unless-exile-from-graveyard — handled via the may ability system
        boolean isDiscardUnlessExile = ability.effects().stream().anyMatch(e -> e instanceof DiscardUnlessExileCardFromGraveyardEffect);
        if (isDiscardUnlessExile) {
            mayPenaltyChoiceHandlerService.handleDiscardUnlessExileChoice(gameData, player, accepted, ability);
            return;
        }

        // Sacrifice-unless-discard — handled via the may ability system
        boolean isSacrificeUnlessDiscard = ability.effects().stream().anyMatch(e -> e instanceof SacrificeUnlessDiscardCardTypeEffect);
        if (isSacrificeUnlessDiscard) {
            mayPenaltyChoiceHandlerService.handleSacrificeUnlessDiscardChoice(gameData, player, accepted, ability);
            return;
        }

        // Sacrifice-unless-return-own-permanent — handled via the may ability system
        boolean isSacrificeUnlessReturnPermanent = ability.effects().stream().anyMatch(e -> e instanceof SacrificeUnlessReturnOwnPermanentTypeToHandEffect);
        if (isSacrificeUnlessReturnPermanent) {
            mayPenaltyChoiceHandlerService.handleSacrificeUnlessReturnOwnPermanentChoice(gameData, player, accepted, ability);
            return;
        }

        // Generic single-draw replacement
        ReplaceSingleDrawEffect replaceSingleDrawEffect = ability.effects().stream()
                .filter(e -> e instanceof ReplaceSingleDrawEffect)
                .map(e -> (ReplaceSingleDrawEffect) e)
                .findFirst()
                .orElse(null);
        if (replaceSingleDrawEffect != null) {
            mayMiscHandlerService.handleSingleDrawReplacementChoice(gameData, player, accepted, ability, replaceSingleDrawEffect);
            return;
        }

        // Redirect retarget — choose new targets for target spell (e.g. Redirect)
        boolean isRedirectRetarget = ability.effects().stream().anyMatch(e -> e instanceof ChooseNewTargetsForTargetSpellEffect);
        if (isRedirectRetarget) {
            mayCopyHandlerService.handleRedirectRetargetChoice(gameData, player, accepted, ability);
            return;
        }

        // Copy spell retarget — choose new targets for a copied spell
        boolean isCopySpellRetarget = ability.effects().stream().anyMatch(e -> e instanceof CopySpellEffect);
        if (isCopySpellRetarget) {
            mayCopyHandlerService.handleCopySpellRetargetChoice(gameData, player, accepted, ability);
            return;
        }

        // BecomeCopyOfTargetCreatureEffect — targets "another creature" (e.g. Cryptoplasm)
        boolean isBecomeCopyEffect = ability.effects().stream().anyMatch(e -> e instanceof BecomeCopyOfTargetCreatureEffect);
        if (isBecomeCopyEffect) {
            mayCopyHandlerService.handleBecomeCopyChoice(gameData, player, accepted, ability);
            return;
        }

        // Copy permanent effect (Clone / Sculpting Steel) — handled as replacement effect (pre-entry)
        CopyPermanentOnEnterEffect copyEffect = ability.effects().stream()
                .filter(e -> e instanceof CopyPermanentOnEnterEffect)
                .map(e -> (CopyPermanentOnEnterEffect) e)
                .findFirst().orElse(null);
        if (copyEffect != null) {
            mayCopyHandlerService.handleCopyPermanentOnEnterChoice(gameData, player, accepted, ability, copyEffect);
            return;
        }

        // Sacrifice-artifact for divided damage (e.g. Kuldotha Flamefiend)
        boolean isSacrificeArtifact = ability.effects().stream()
                .anyMatch(e -> e instanceof SacrificeArtifactThenDealDividedDamageEffect);
        if (isSacrificeArtifact) {
            mayMiscHandlerService.handleMaySacrificeArtifactForDividedDamage(gameData, player, accepted, ability);
            return;
        }

        // Sphinx Ambassador — put selected card onto battlefield or return to library
        boolean isSphinxAmbassador = ability.effects().stream()
                .anyMatch(e -> e instanceof SphinxAmbassadorPutOnBattlefieldEffect);
        if (isSphinxAmbassador) {
            mayMiscHandlerService.handleSphinxAmbassadorChoice(gameData, player, accepted, ability);
            return;
        }

        // Shuffle library — resolved directly without creating a stack entry (e.g. Ponder "You may shuffle")
        boolean isShuffleLibrary = ability.effects().stream()
                .anyMatch(e -> e instanceof ShuffleLibraryEffect);
        if (isShuffleLibrary) {
            if (accepted) {
                LibraryShuffleHelper.shuffleLibrary(gameData, ability.controllerId());
                String logEntry = player.getUsername() + " shuffles their library.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} shuffles their library ({})", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
            } else {
                String logEntry = player.getUsername() + " chooses not to shuffle.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} declines shuffle ({})", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Mana payment for may-pay triggers (e.g. Embersmith "pay {1}", Vigil for the Lost "pay {X}")
        int xValuePaid = 0;
        if (accepted && ability.manaCost() != null) {
            ManaCost cost = new ManaCost(ability.manaCost());
            ManaPool pool = gameData.playerManaPools.get(player.getId());

            if (cost.hasX()) {
                // X cost: pay all available mana as X
                int maxX = cost.calculateMaxX(pool);
                if (maxX <= 0) {
                    String logEntry = player.getUsername() + " has no mana to pay for " + ability.sourceCard().getName() + "'s ability.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} has no mana for X may ability", gameData.id, player.getUsername());

                    inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                    return;
                }
                xValuePaid = maxX;
                cost.pay(pool, maxX);
            } else {
                if (!cost.canPay(pool)) {
                    String logEntry = player.getUsername() + " cannot pay " + ability.manaCost() + " for " + ability.sourceCard().getName() + "'s ability.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} can't pay {} for may ability", gameData.id, player.getUsername(), ability.manaCost());

                    inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                    return;
                }
                cost.pay(pool);
            }
        }

        // Targeted may ability (e.g. "you may deal 3 damage to target creature", "you may destroy target Equipment")
        boolean isTargetedPermanentEffect = ability.effects().stream()
                .anyMatch(CardEffect::canTargetPermanent);
        boolean isTargetedPlayerEffect = ability.effects().stream()
                .anyMatch(CardEffect::canTargetPlayer);
        boolean isTargetedGraveyardEffect = ability.effects().stream()
                .anyMatch(CardEffect::canTargetGraveyard);
        boolean isTargetedEffect = isTargetedPermanentEffect || isTargetedPlayerEffect || isTargetedGraveyardEffect;

        // Pre-targeted may ability — target was already chosen (e.g. "You may tap or untap that creature", "you may have that player lose 1 life")
        if (accepted && isTargetedEffect && ability.targetCardId() != null) {
            boolean isPreTargetedPlayer = gameData.playerIds.contains(ability.targetCardId());
            Permanent target = isPreTargetedPlayer ? null : gameQueryService.findPermanentById(gameData, ability.targetCardId());
            if (target != null || isPreTargetedPlayer) {
                StackEntry entry;
                if (ability.sourcePermanentId() != null) {
                    entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            ability.sourceCard(),
                            ability.controllerId(),
                            ability.sourceCard().getName() + "'s ability",
                            new ArrayList<>(ability.effects()),
                            null,
                            ability.sourcePermanentId()
                    );
                } else {
                    entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            ability.sourceCard(),
                            ability.controllerId(),
                            ability.sourceCard().getName() + "'s ability",
                            new ArrayList<>(ability.effects()),
                            0
                    );
                }
                entry.setTargetId(ability.targetCardId());
                gameData.stack.add(entry);

                if (isPreTargetedPlayer) {
                    String targetName = gameData.playerIdToName.get(ability.targetCardId());
                    String logEntry = player.getUsername() + " accepts — " + ability.sourceCard().getName()
                            + "'s ability targets " + targetName + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                } else {
                    String logEntry = player.getUsername() + " accepts — " + ability.sourceCard().getName()
                            + "'s ability targets " + target.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
                log.info("Game {} - {} accepts pre-targeted may ability from {}", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
            } else {
                String logEntry = ability.sourceCard().getName() + "'s ability fizzles — target no longer exists.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} pre-targeted may ability target gone", gameData.id, ability.sourceCard().getName());
            }

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (accepted && isTargetedGraveyardEffect) {
            handleGraveyardTargetedMayAbility(gameData, player, ability);
            return;
        }

        if (accepted && isTargetedEffect) {
            handleTargetedMayAbilityAccepted(gameData, player, ability);
            return;
        }

        if (accepted) {
            StackEntry entry;
            if (ability.sourcePermanentId() != null) {
                // Combat damage trigger with source permanent and target context
                entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        ability.sourceCard(),
                        ability.controllerId(),
                        ability.sourceCard().getName() + "'s ability",
                        new ArrayList<>(ability.effects()),
                        ability.targetCardId(),
                        ability.sourcePermanentId()
                );
            } else {
                entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        ability.sourceCard(),
                        ability.controllerId(),
                        ability.sourceCard().getName() + "'s ability",
                        new ArrayList<>(ability.effects()),
                        xValuePaid
                );
            }

            // Self-targeting effects need the source permanent's ID to resolve
            boolean needsSelfTarget = ability.effects().stream().anyMatch(e ->
                    e instanceof PutChargeCounterOnSelfEffect
                            || e instanceof AnimateSelfEffect || e instanceof AnimateSelfByChargeCountersEffect
                            || e instanceof AnimateSelfWithStatsEffect || e instanceof BoostSelfEffect
                            || e instanceof ImprintDyingCreatureEffect
                            || e instanceof ExileFromHandToImprintEffect
                            || e instanceof ReturnDyingCreatureToBattlefieldAndAttachSourceEffect);
            if (needsSelfTarget) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(ability.controllerId());
                if (battlefield != null) {
                    for (Permanent p : battlefield) {
                        if (p.getCard() == ability.sourceCard()) {
                            entry.setTargetId(p.getId());
                            break;
                        }
                    }
                }
            }

            // Effects that copy an entering permanent need the target permanent ID from the trigger
            boolean needsEnteringTarget = ability.effects().stream()
                    .anyMatch(e -> e instanceof CreateTokenCopyOfTargetPermanentEffect);
            if (needsEnteringTarget && ability.targetCardId() != null) {
                entry.setTargetId(ability.targetCardId());
            }

            gameData.stack.add(entry);

            String logEntry = player.getUsername() + " accepts — " + ability.sourceCard().getName() + "'s triggered ability goes on the stack.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} accepts may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        } else {
            String logEntry = player.getUsername() + " declines " + ability.sourceCard().getName() + "'s triggered ability.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void handleTargetedMayAbilityAccepted(GameData gameData, Player player, PendingMayAbility ability) {
        // Collect valid permanent targets from all battlefields using card's target filter
        List<UUID> validTargets = new ArrayList<>();
        Card sourceCard = ability.sourceCard();
        boolean canTargetPermanent = ability.effects().stream().anyMatch(CardEffect::canTargetPermanent);
        if (canTargetPermanent) {
            FilterContext ctx = FilterContext.of(gameData)
                    .withSourceCardId(sourceCard.getId())
                    .withSourceControllerId(ability.controllerId());
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (sourceCard.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                        if (gameQueryService.matchesPermanentPredicate(p, filter.predicate(), ctx)) {
                            validTargets.add(p.getId());
                        }
                    } else if (gameQueryService.isCreature(gameData, p)) {
                        validTargets.add(p.getId());
                    }
                }
            }
        }

        // Add player IDs for effects that can target players (e.g. DealDamageToAnyTargetEffect, MillTargetPlayerEffect)
        boolean canTargetPlayer = ability.effects().stream().anyMatch(CardEffect::canTargetPlayer);
        if (canTargetPlayer) {
            validTargets.addAll(gameData.orderedPlayerIds);
        }

        if (validTargets.isEmpty()) {
            String logEntry = ability.sourceCard().getName() + "'s ability has no valid targets.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} may ability has no valid targets", gameData.id, ability.sourceCard().getName());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                ability.sourceCard(), ability.controllerId(), new ArrayList<>(ability.effects())
        ));
        String targetDescription;
        if (!canTargetPermanent && canTargetPlayer) {
            targetDescription = "player";
        } else if (sourceCard.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
            targetDescription = filter.errorMessage().replace("Target must be ", "").replace("an ", "").replace("a ", "");
        } else if (canTargetPlayer) {
            targetDescription = "any target";
        } else {
            targetDescription = "creature";
        }
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets,
                ability.sourceCard().getName() + "'s ability — Choose target " + targetDescription + ".");

        String logEntry = player.getUsername() + " accepts — choosing a target for " + ability.sourceCard().getName() + "'s ability.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} accepts targeted may ability from {}", gameData.id, player.getUsername(), ability.sourceCard().getName());
    }

    private void handleGraveyardTargetedMayAbility(GameData gameData, Player player, PendingMayAbility ability) {
        UUID controllerId = ability.controllerId();

        // Determine filter from the graveyard-targeting effect
        CardPredicate filter = null;
        boolean anyGraveyard = false;
        for (CardEffect effect : ability.effects()) {
            if (effect instanceof ExileTargetCardFromGraveyardAndImprintOnSourceEffect imprint) {
                filter = imprint.filter();
                anyGraveyard = effect.canTargetAnyGraveyard();
                break;
            }
            if (effect.canTargetGraveyard()) {
                anyGraveyard = effect.canTargetAnyGraveyard();
                break;
            }
        }

        // Collect matching graveyard cards
        List<UUID> searchPlayerIds = anyGraveyard
                ? gameData.orderedPlayerIds
                : List.of(controllerId);
        UUID graveyardOwnerId = null;
        List<Integer> matchingIndices = new ArrayList<>();
        for (UUID pid : searchPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(pid);
            if (graveyard == null) continue;
            for (int i = 0; i < graveyard.size(); i++) {
                if (gameQueryService.matchesCardPredicate(graveyard.get(i), filter, ability.sourceCard().getId())) {
                    matchingIndices.add(i);
                    graveyardOwnerId = pid;
                }
            }
        }

        if (matchingIndices.isEmpty()) {
            String filterLabel = CardPredicateUtils.describeFilter(filter);
            String logEntry = ability.sourceCard().getName() + "'s ability has no valid " + filterLabel + " targets in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} may ability has no valid graveyard targets", gameData.id, ability.sourceCard().getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // If only one match, create stack entry immediately
        if (matchingIndices.size() == 1) {
            List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
            Card targetCard = graveyard.get(matchingIndices.getFirst());

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    controllerId,
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(ability.effects()),
                    targetCard.getId(),
                    ability.sourcePermanentId()
            );
            gameData.stack.add(entry);

            String logEntry = player.getUsername() + " accepts — " + ability.sourceCard().getName()
                    + "'s ability targets " + targetCard.getName() + " in graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} accepts graveyard-targeted may ability from {}", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Multiple matches — prompt player to choose
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        gameData.interaction.prepareGraveyardChoice(GraveyardChoiceDestination.MAY_ABILITY_TARGET, null);
        gameData.interaction.graveyardChoice().setMayAbilityContext(
                ability.sourceCard(), controllerId, new ArrayList<>(ability.effects()), ability.sourcePermanentId());
        playerInputService.beginGraveyardChoice(gameData, graveyardOwnerId, matchingIndices,
                "Choose a " + filterLabel + " from your graveyard to target.");

        String logEntry = player.getUsername() + " accepts — choosing a graveyard target for " + ability.sourceCard().getName() + "'s ability.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} accepts graveyard-targeted may ability from {}", gameData.id,
                player.getUsername(), ability.sourceCard().getName());
    }

    private void handleResolutionTimeMayChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        gameData.resolvingMayEffectFromStack = false;
        RegisterDelayedCounterTriggerEffect dct = ability.effects().stream().filter(e -> e instanceof RegisterDelayedCounterTriggerEffect).map(e -> (RegisterDelayedCounterTriggerEffect) e).findFirst().orElse(null);
        if (dct != null) { gameData.pendingEffectResolutionEntry = null; gameData.pendingEffectResolutionIndex = 0; mayMiscHandlerService.handleOpeningHandDelayedCounterTrigger(gameData, player, accepted, ability, dct); return; }
        RegisterDelayedManaTriggerEffect dmt = ability.effects().stream().filter(e -> e instanceof RegisterDelayedManaTriggerEffect).map(e -> (RegisterDelayedManaTriggerEffect) e).findFirst().orElse(null);
        if (dmt != null) { gameData.pendingEffectResolutionEntry = null; gameData.pendingEffectResolutionIndex = 0; mayMiscHandlerService.handleOpeningHandDelayedManaTrigger(gameData, player, accepted, ability, dmt); return; }
        // Redirect retarget — ChooseNewTargetsForTargetSpellEffect needs the full retarget UI flow
        boolean isRedirectRetarget = ability.effects().stream().anyMatch(e -> e instanceof ChooseNewTargetsForTargetSpellEffect);
        if (isRedirectRetarget) {
            gameData.pendingEffectResolutionEntry = null;
            gameData.pendingEffectResolutionIndex = 0;
            mayCopyHandlerService.handleRedirectRetargetChoice(gameData, player, accepted, ability);
            return;
        }
        if (accepted) {
            if (ability.manaCost() != null) {
                ManaCost cost = new ManaCost(ability.manaCost());
                ManaPool pool = gameData.playerManaPools.get(player.getId());
                if (!cost.canPay(pool)) {
                    gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + " cannot pay " + ability.manaCost() + " for " + ability.sourceCard().getName() + "'s ability.");
                    gameData.resolvedMayAccepted = false;
                    if (gameData.pendingEffectResolutionEntry != null) { effectResolutionService.resolveEffectsFrom(gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex); }
                    if (gameData.interaction.isAwaitingInput()) { return; }
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    return;
                }
                cost.pay(pool);
            }
            gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + " accepts — resolving " + ability.sourceCard().getName() + "'s ability.");
            CardEffect innerEffect = extractInnerEffect(ability);
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            boolean isTargetedPermanent = innerEffect != null && innerEffect.canTargetPermanent();
            boolean isTargetedPlayer = innerEffect != null && innerEffect.canTargetPlayer();
            boolean isTargetedGraveyard = innerEffect != null && innerEffect.canTargetGraveyard();
            boolean targetAlreadySet = pendingEntry != null
                    && (pendingEntry.getTargetId() != null || !pendingEntry.getTargetIds().isEmpty());
            if ((isTargetedPermanent || isTargetedPlayer) && pendingEntry != null && !targetAlreadySet) {
                gameData.resolvedMayAccepted = true;
                handleResolutionTimeTargetSelection(gameData, player, ability, pendingEntry, isTargetedPermanent, isTargetedPlayer);
                return;
            }
            if (isTargetedGraveyard && pendingEntry != null && !targetAlreadySet) {
                gameData.resolvedMayAccepted = true;
                handleResolutionTimeGraveyardTargetSelection(gameData, player, ability, pendingEntry);
                return;
            }
            if (pendingEntry != null) { setUpSelfTargetIfNeeded(gameData, ability, pendingEntry, innerEffect); }
            gameData.resolvedMayAccepted = true;
        } else {
            gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + " declines " + ability.sourceCard().getName() + "'s ability.");
            gameData.resolvedMayAccepted = false;
        }
        if (gameData.pendingEffectResolutionEntry != null) { effectResolutionService.resolveEffectsFrom(gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex); }
        if (gameData.interaction.isAwaitingInput()) { return; }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private CardEffect extractInnerEffect(PendingMayAbility ability) {
        if (ability.effects().isEmpty()) return null;
        CardEffect first = ability.effects().getFirst();
        if (first instanceof MayEffect may) { return may.wrapped(); }
        return first;
    }

    private void setUpSelfTargetIfNeeded(GameData gameData, PendingMayAbility ability, StackEntry pendingEntry, CardEffect innerEffect) {
        if (innerEffect == null) return;
        boolean needsSelfTarget = innerEffect instanceof PutChargeCounterOnSelfEffect || innerEffect instanceof AnimateSelfEffect || innerEffect instanceof AnimateSelfByChargeCountersEffect || innerEffect instanceof AnimateSelfWithStatsEffect || innerEffect instanceof BoostSelfEffect || innerEffect instanceof ImprintDyingCreatureEffect || innerEffect instanceof ExileFromHandToImprintEffect || innerEffect instanceof ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
        if (needsSelfTarget && pendingEntry.getTargetId() == null) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(ability.controllerId());
            if (battlefield != null) { for (Permanent p : battlefield) { if (p.getCard() == ability.sourceCard()) { pendingEntry.setTargetId(p.getId()); break; } } }
        }
    }

    private void handleResolutionTimeGraveyardTargetSelection(GameData gameData, Player player,
                                                              PendingMayAbility ability, StackEntry pendingEntry) {
        UUID controllerId = ability.controllerId();

        // Determine filter from the graveyard-targeting effect
        CardPredicate filter = null;
        boolean anyGraveyard = false;
        for (CardEffect effect : ability.effects()) {
            if (effect instanceof ExileTargetCardFromGraveyardAndImprintOnSourceEffect imprint) {
                filter = imprint.filter();
                anyGraveyard = effect.canTargetAnyGraveyard();
                break;
            }
            if (effect.canTargetGraveyard()) {
                anyGraveyard = effect.canTargetAnyGraveyard();
                break;
            }
        }

        List<UUID> searchPlayerIds = anyGraveyard
                ? gameData.orderedPlayerIds
                : List.of(controllerId);
        UUID graveyardOwnerId = null;
        List<Integer> matchingIndices = new ArrayList<>();
        for (UUID pid : searchPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(pid);
            if (graveyard == null) continue;
            for (int i = 0; i < graveyard.size(); i++) {
                if (gameQueryService.matchesCardPredicate(graveyard.get(i), filter, ability.sourceCard().getId())) {
                    matchingIndices.add(i);
                    graveyardOwnerId = pid;
                }
            }
        }

        if (matchingIndices.isEmpty()) {
            String filterLabel = CardPredicateUtils.describeFilter(filter);
            gameBroadcastService.logAndBroadcast(gameData,
                    ability.sourceCard().getName() + "'s ability — no valid " + filterLabel + " targets in graveyard.");
            // Resume resolution with may declined (no valid target)
            gameData.resolvedMayAccepted = false;
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex);
            }
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        // Single match — set target immediately and resume resolution
        if (matchingIndices.size() == 1) {
            List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
            Card targetCard = graveyard.get(matchingIndices.getFirst());
            pendingEntry.setTargetId(targetCard.getId());

            gameBroadcastService.logAndBroadcast(gameData,
                    player.getUsername() + " targets " + targetCard.getName() + " in graveyard.");
            effectResolutionService.resolveEffectsFrom(gameData, pendingEntry, gameData.pendingEffectResolutionIndex);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        // Multiple matches — prompt player to choose via graveyard choice
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        gameData.resolvedMayTargetingEntry = pendingEntry;
        gameData.interaction.prepareGraveyardChoice(GraveyardChoiceDestination.MAY_ABILITY_TARGET, null);
        gameData.interaction.graveyardChoice().setMayAbilityContext(
                ability.sourceCard(), controllerId, new ArrayList<>(ability.effects()), ability.sourcePermanentId());
        playerInputService.beginGraveyardChoice(gameData, graveyardOwnerId, matchingIndices,
                "Choose a " + filterLabel + " from your graveyard to target.");
    }

    private void handleResolutionTimeTargetSelection(GameData gameData, Player player, PendingMayAbility ability, StackEntry pendingEntry, boolean canTargetPermanent, boolean canTargetPlayer) {
        List<UUID> validTargets = new ArrayList<>();
        Card sourceCard = ability.sourceCard();
        if (canTargetPermanent) { FilterContext ctx = FilterContext.of(gameData).withSourceCardId(sourceCard.getId()).withSourceControllerId(ability.controllerId()); for (UUID pid : gameData.orderedPlayerIds) { List<Permanent> battlefield = gameData.playerBattlefields.get(pid); if (battlefield == null) continue; for (Permanent p : battlefield) { if (sourceCard.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) { if (gameQueryService.matchesPermanentPredicate(p, filter.predicate(), ctx)) { validTargets.add(p.getId()); } } else if (gameQueryService.isCreature(gameData, p)) { validTargets.add(p.getId()); } } } }
        if (canTargetPlayer) { validTargets.addAll(gameData.orderedPlayerIds); }
        if (validTargets.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, ability.sourceCard().getName() + "'s ability has no valid targets.");
            gameData.resolvedMayAccepted = false;
            if (gameData.pendingEffectResolutionEntry != null) { effectResolutionService.resolveEffectsFrom(gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex); }
            if (gameData.interaction.isAwaitingInput()) return;
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }
        gameData.resolvedMayTargetingEntry = pendingEntry;
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(ability.sourceCard(), ability.controllerId(), new ArrayList<>(ability.effects())));
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets, ability.sourceCard().getName() + "'s ability — Choose target.");
        gameBroadcastService.logAndBroadcast(gameData, player.getUsername() + " accepts — choosing a target for " + ability.sourceCard().getName() + "'s ability.");
    }
}
