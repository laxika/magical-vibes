package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.PendingBendOrBreak;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.PendingRagingRiver;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.PlayCardFromHandByWordOfCommandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCounterTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.BendOrBreakEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RagingRiverEffectHandler;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.MayEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MayAbilityHandlerService {

    private final InputCompletionService inputCompletionService;
    private final MayCastHandlerService mayCastHandlerService;
    private final MayCopyHandlerService mayCopyHandlerService;
    private final MayMiscHandlerService mayMiscHandlerService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EffectResolutionService effectResolutionService;
    private final DestructionSupport destructionSupport;
    private final BendOrBreakEffectHandler bendOrBreakEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.FightOrFlightSupport fightOrFlightSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.StandOrFallSupport standOrFallSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.MakeAnExampleEffectHandler makeAnExampleEffectHandler;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.BrilliantUltimatumSupport brilliantUltimatumSupport;
    private final MayAbilityTapCostService mayAbilityTapCostService;
    private final MayEffectHandlerRegistry mayEffectHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;
    private final RagingRiverEffectHandler ragingRiverEffectHandler;

    @Autowired @Lazy
    private LifeSupport lifeSupport;

    @Autowired @Lazy
    private PermanentChoiceHandlerService permanentChoiceHandlerService;

    public MayAbilityHandlerService(InputCompletionService inputCompletionService,
                                    MayCastHandlerService mayCastHandlerService,
                                    MayCopyHandlerService mayCopyHandlerService,
                                    MayMiscHandlerService mayMiscHandlerService,
                                    GameQueryService gameQueryService,
                                    GameLogService gameLogService,
                                    EffectResolutionService effectResolutionService,
                                    DestructionSupport destructionSupport,
                                    BendOrBreakEffectHandler bendOrBreakEffectHandler,
                                    com.github.laxika.magicalvibes.service.effect.normalfx.FightOrFlightSupport fightOrFlightSupport,
                                    com.github.laxika.magicalvibes.service.effect.normalfx.StandOrFallSupport standOrFallSupport,
                                    com.github.laxika.magicalvibes.service.effect.normalfx.MakeAnExampleEffectHandler makeAnExampleEffectHandler,
                                    GraveyardReturnSupport graveyardReturnSupport,
                                    com.github.laxika.magicalvibes.service.effect.normalfx.BrilliantUltimatumSupport brilliantUltimatumSupport,
                                    MayAbilityTapCostService mayAbilityTapCostService,
                                    MayEffectHandlerRegistry mayEffectHandlerRegistry,
                                    TriggerCollectionService triggerCollectionService,
                                    RagingRiverEffectHandler ragingRiverEffectHandler) {
        this.inputCompletionService = inputCompletionService;
        this.mayCastHandlerService = mayCastHandlerService;
        this.mayCopyHandlerService = mayCopyHandlerService;
        this.mayMiscHandlerService = mayMiscHandlerService;
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.effectResolutionService = effectResolutionService;
        this.destructionSupport = destructionSupport;
        this.bendOrBreakEffectHandler = bendOrBreakEffectHandler;
        this.fightOrFlightSupport = fightOrFlightSupport;
        this.standOrFallSupport = standOrFallSupport;
        this.makeAnExampleEffectHandler = makeAnExampleEffectHandler;
        this.graveyardReturnSupport = graveyardReturnSupport;
        this.brilliantUltimatumSupport = brilliantUltimatumSupport;
        this.mayAbilityTapCostService = mayAbilityTapCostService;
        this.mayEffectHandlerRegistry = mayEffectHandlerRegistry;
        this.triggerCollectionService = triggerCollectionService;
        this.ragingRiverEffectHandler = ragingRiverEffectHandler;
    }

    /**
     * Completes a pile separation whose chosen pile is destroyed or sacrificed (Make an Example, Liliana of the Veil,
     * Do or Die). It changes nothing before the pile leaves, so a creature in it with competing death replacements is
     * asked about first (CR 616.1) and the answer is completed again once its controller has chosen.
     */
    public void completeKillingPileChoice(GameData gameData, boolean accepted) {
        Runnable completion = gameData.peekPendingInteraction(PendingPileSeparation.class).disposition()
                == CardPileDisposition.MAKE_AN_EXAMPLE
                ? () -> makeAnExampleEffectHandler.completePileSeparationStep2(gameData, accepted)
                : () -> destructionSupport.completePileSeparationStep2(gameData, accepted);
        if (permanentChoiceHandlerService.runAskingDeathReplacementsFirst(gameData, completion,
                new GameData.PileChoiceReplay(accepted))) {
            return;
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /** Whether the chosen pile of {@code separation} is sacrificed or destroyed rather than moved or kept. */
    private static boolean killsChosenPile(PendingPileSeparation separation) {
        CardPileDisposition disposition = separation.disposition();
        return disposition == CardPileDisposition.MAKE_AN_EXAMPLE
                || !separation.cardPileMode() && disposition != CardPileDisposition.ATTACKERS
                        && disposition != CardPileDisposition.BLOCKERS
                        && disposition != CardPileDisposition.PLAY_FROM_EXILE;
    }

    public void handleMayAbilityChosen(GameData gameData, Player player, boolean accepted) {
        if (gameData.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) == null) {
            throw new IllegalStateException("Not awaiting may ability choice");
        }
        PendingInteraction.MayAbilityChoice mayAbilityChoice =
                gameData.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        if (mayAbilityChoice == null || !player.getId().equals(mayAbilityChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        PendingMayAbility upcoming = gameData.pendingMayAbilities.isEmpty() ? null : gameData.pendingMayAbilities.getFirst();
        if (!accepted && upcoming != null && upcoming.effects().stream()
                .anyMatch(PlayCardFromHandByWordOfCommandEffect.class::isInstance)) {
            mayCastHandlerService.requireWordOfCommandPlayIfAble(gameData, upcoming);
        }

        PendingMayAbility ability = gameData.pendingMayAbilities.removeFirst();
        gameData.interaction.clearAwaitingInput();

        if (ability.effects().stream().anyMatch(PlayCardFromHandByWordOfCommandEffect.class::isInstance)) {
            mayCastHandlerService.handleWordOfCommandPlay(gameData, player, accepted, ability);
            return;
        }

        if (gameData.peekPendingInteraction(PendingRagingRiver.class) != null) {
            ragingRiverEffectHandler.completeAttackerChoice(gameData, accepted);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        // Pile separation: permanent-pile (Liliana) vs card-pile (Boneyard Parley, Brilliant Ultimatum,
        // Unesh, Curator of Destinies)
        PendingPileSeparation pileSeparation = gameData.peekPendingInteraction(PendingPileSeparation.class);
        if (pileSeparation != null) {
            if (killsChosenPile(pileSeparation)) {
                completeKillingPileChoice(gameData, accepted);
                return;
            } else if (pileSeparation.disposition() == CardPileDisposition.ATTACKERS) {
                fightOrFlightSupport.completePileSeparationStep2(gameData, accepted);
            } else if (pileSeparation.disposition() == CardPileDisposition.BLOCKERS) {
                standOrFallSupport.completePileSeparationStep2(gameData, accepted);
            } else if (pileSeparation.disposition() == CardPileDisposition.PLAY_FROM_EXILE) {
                brilliantUltimatumSupport.completePileSeparationStep2(gameData, accepted);
            } else if (pileSeparation.cardPileMode()) {
                // BATTLEFIELD (Boneyard Parley) and HAND (Unesh) both flow through step 2, which branches on disposition.
                graveyardReturnSupport.completeCardPileSeparationStep2(gameData, accepted);
            } else {
                destructionSupport.completePileSeparationStep2(gameData, accepted);
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.hasPendingInteraction(PendingBendOrBreak.class)) {
            bendOrBreakEffectHandler.completePileChoice(gameData, accepted);
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

        // Registry dispatch (mayfx): route to the first effect in the ability's list that has a
        // migrated handler. No card bundles two registered may-effect types at the top level of
        // effects(), so list order matches the old fixed code-order chain.
        for (CardEffect effect : ability.effects()) {
            var mayHandler = mayEffectHandlerRegistry.getHandler(effect);
            if (mayHandler != null) {
                mayHandler.handle(gameData, player, accepted, ability);
                return;
            }
        }

        // Cast-from-library-without-paying — e.g. Galvanoth (second phase: cast prompt)
        CastTopOfLibraryWithoutPayingManaCostEffect castFromLibEffect = ability.effects().stream()
                .filter(e -> e instanceof CastTopOfLibraryWithoutPayingManaCostEffect)
                .map(e -> (CastTopOfLibraryWithoutPayingManaCostEffect) e)
                .findFirst().orElse(null);
        if (castFromLibEffect != null && castFromLibEffect.matches(ability.sourceCard())) {
            if (accepted) {
                markOncePerTurnTriggerOnAcceptance(gameData, ability);
            }
            mayCastHandlerService.handleCastFromLibraryChoice(gameData, player, accepted, ability);
            return;
        }

        if (!accepted) {
            gameLogService.append(gameData, GameLog.playerDeclinesAbility(player.getUsername(), ability.sourceCard()));
            log.info("Game {} - {} declines may ability from {}", gameData.id, player.getUsername(),
                    ability.sourceCard().getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        resolveAcceptedInAskingResolution(gameData, player, ability);
    }

    /**
     * Carries out the accepted option of a question that has no handler of its own, at once: in the resolution that
     * asked it when one is waiting for the answer (CR 608.2d: "that player may search their library", "each of that
     * player's opponents may draw a card", "you may exile all other creatures. If you do, ..."), and otherwise as the
     * event that asked it happens ("you may exert this creature as it attacks", CR 701.43a). Nothing is put on the
     * stack for it: a triggered ability's own "you may" is never asked this way, it goes on the stack with its
     * {@code MayEffect} and is asked as it resolves (CR 603.5). The payload resolves as an entry of its own, which
     * names the player who acts and the object or player the question was about, and which a "when you do" among
     * its steps records (CR 603.12).
     */
    private void resolveAcceptedInAskingResolution(GameData gameData, Player player, PendingMayAbility ability) {
        if (ability.manaCost() != null || ability.tapPermanentsCost() != null) {
            throw new IllegalStateException("A question with a cost needs a may handler that pays it: "
                    + ability.description());
        }
        if (ability.targetCardId() == null && ability.effects().stream().anyMatch(effect ->
                !effect.targetsChosenByReflexiveAbility()
                        && (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)))) {
            throw new IllegalStateException("A target is chosen as its spell or ability is put on the stack, not "
                    + "once a question is answered: " + ability.description());
        }
        markOncePerTurnTriggerOnAcceptance(gameData, ability);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " accepts — ", ability.sourceCard(), "."));
        log.info("Game {} - {} accepts may ability from {}", gameData.id, player.getUsername(),
                ability.sourceCard().getName());

        UUID sourcePermanentId = ability.sourcePermanentId() != null
                ? ability.sourcePermanentId() : sourcePermanentOnBattlefield(gameData, ability);
        StackEntry accepted = ReflexiveAbilitySupport.mayAbilityRecord(ability, ability.effects(),
                ability.sourceCard().getName() + "'s ability", sourcePermanentId, 0);
        effectResolutionService.resolveNestedInParkedResolution(gameData, accepted);
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Completes a resolution-time "you may" answer again (a "pay {M} or N life", "pay {M} and N life" or K'rrik
     * payment) once the Lich's Duel Mastery sacrifice its life payment would cause has had its death replacements
     * chosen.
     */
    public void completeResolutionTimeMayChoiceAgain(GameData gameData, Player player, boolean accepted,
                                                     PendingMayAbility ability) {
        handleResolutionTimeMayChoice(gameData, player, accepted, ability);
    }

    /**
     * Pays {@code amount} life for {@code ability}, the first change its accepted answer makes. Paying life is losing
     * it (CR 119.4), which Lich's Duel Mastery can replace with its sacrifice; that sacrifice's death replacements
     * are then chosen before the payment (CR 616.1), and the answer is completed again.
     *
     * @return {@code true} when the payment stopped to ask
     */
    private boolean payLifeAskingDeathReplacementsFirst(GameData gameData, Player player, PendingMayAbility ability,
                                                        int amount) {
        return permanentChoiceHandlerService.answerAskingDeathReplacementsFirst(gameData, player, ability, true,
                () -> lifeSupport.applyLifePayment(gameData, player.getId(), amount, ability.sourceCard().getName()));
    }

    /**
     * The answer to the "you may" of an ability that is resolving (CR 603.5): its costs are paid, and the resolution
     * that asked goes on with the answer. Nothing is chosen here: a target of the ability, the one its optional effect
     * acts on included, was chosen as the ability was put on the stack (CR 603.3d) and is only checked again as the
     * ability resolves (CR 608.2b), so an accepted effect whose entry has no target resolves without one.
     */
    private void handleResolutionTimeMayChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        gameData.resolvingMayEffectFromStack = false;
        MayPayManaEffect anyPlayerMayPay = ability.effects().stream()
                .filter(e -> e instanceof MayPayManaEffect mayPay
                        && (mayPay.payer() == MayPayPayer.ANY_PLAYER
                        || mayPay.payer() == MayPayPayer.ANY_OTHER_PLAYER
                        || mayPay.payer() == MayPayPayer.ANY_OPPONENT))
                .map(MayPayManaEffect.class::cast)
                .findFirst().orElse(null);
        if (anyPlayerMayPay != null) {
            handleAnyPlayerMayPayChoice(gameData, player, accepted, ability);
            return;
        }
        RegisterDelayedCounterTriggerEffect dct = ability.effects().stream().filter(e -> e instanceof RegisterDelayedCounterTriggerEffect).map(e -> (RegisterDelayedCounterTriggerEffect) e).findFirst().orElse(null);
        if (dct != null) { gameData.pendingEffectResolutionEntry = null; gameData.pendingEffectResolutionIndex = 0; mayMiscHandlerService.handleOpeningHandDelayedCounterTrigger(gameData, player, accepted, ability, dct); return; }
        RegisterDelayedManaTriggerEffect dmt = ability.effects().stream().filter(e -> e instanceof RegisterDelayedManaTriggerEffect).map(e -> (RegisterDelayedManaTriggerEffect) e).findFirst().orElse(null);
        if (dmt != null) { gameData.pendingEffectResolutionEntry = null; gameData.pendingEffectResolutionIndex = 0; mayMiscHandlerService.handleOpeningHandDelayedManaTrigger(gameData, player, accepted, ability, dmt); return; }
        // Redirect retarget — ChooseNewTargetsForTargetSpellEffect needs the full retarget UI flow
        boolean isRedirectRetarget = ability.effects().stream().anyMatch(e -> e instanceof ChooseNewTargetsForTargetSpellEffect);
        if (isRedirectRetarget) {
            // Advance past this "may choose new targets" effect (the stored index points at the
            // MayEffect for re-run) so any following effects on the same spell continue after the
            // retarget completes — e.g. Wild Ricochet's "Then copy that spell." The entry is kept
            // so the retarget-completion path (handleSpellRetarget / the decline branch) resumes
            // the remaining effects. For a standalone Redirect the resumed index is past the end,
            // so it is a harmless no-op.
            if (gameData.pendingEffectResolutionEntry != null) {
                gameData.pendingEffectResolutionIndex = gameData.pendingEffectResolutionIndex + 1;
            }
            mayCopyHandlerService.handleRedirectRetargetChoice(gameData, player, accepted, ability);
            return;
        }
        if (accepted) {
            if (requiresSourceSacrifice(ability.effects())
                    && (ability.sourcePermanentId() == null
                    || gameData.playerBattlefields.get(ability.controllerId()) == null
                    || gameData.playerBattlefields.get(ability.controllerId()).stream()
                    .noneMatch(p -> p.getId().equals(ability.sourcePermanentId())))) {
                accepted = false;
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " cannot sacrifice ", ability.sourceCard(), "."));
            }
        }
        if (accepted) {
            if (ability.tapPermanentsCost() != null) {
                // beginTapCostPayment either awaits input or continues the parked resolution.
                mayAbilityTapCostService.beginTapCostPayment(
                        gameData, player, ability.tapPermanentsCost(), ability.sourcePermanentId(), ability);
                return;
            } else if (ability.manaCost() != null) {
                ManaCost cost = new ManaCost(ability.manaCost());
                ManaPool pool = gameData.playerManaPools.get(player.getId());
                boolean paidWithLife = false;
                if (!cost.canPayResolutionCost(pool) && ability.lifeCost() > 0) {
                    // "Pay {M} or N life" — mana that would empty at end of step is the cheaper
                    // resource, so life is only spent when the mana cost cannot be paid.
                    boolean canPayLife = gameQueryService.canPlayerLifeChange(gameData, player.getId())
                            && gameData.getLife(player.getId()) >= ability.lifeCost();
                    if (canPayLife) {
                        if (payLifeAskingDeathReplacementsFirst(gameData, player, ability, ability.lifeCost())) {
                            return;
                        }
                        paidWithLife = true;
                    }
                }
                // "Pay {M} and N life" (Purgatory) — both halves are part of one cost, so neither is
                // paid unless both can be.
                boolean canPayAdditionalLife = ability.additionalLifeCost() == 0
                        || (gameQueryService.canPlayerLifeChange(gameData, player.getId())
                                && gameData.getLife(player.getId()) >= ability.additionalLifeCost());
                // K'rrik-style permission: each black symbol in the cost may be paid with 2 life instead.
                ManaCost blackAsLifeCost = cost.withBlackManaAsPhyrexian();
                if (!paidWithLife && ability.lifeCost() == 0 && ability.additionalLifeCost() == 0
                        && !cost.canPayResolutionCost(pool) && canPayAdditionalLife
                        && gameQueryService.canPayBlackManaWithLife(gameData, player.getId())
                        && blackAsLifeCost.canPayResolutionCost(pool)) {
                    int lifeOwed = blackAsLifeCost.payPhyrexianManaAuto(new ManaPool(pool), 0);
                    if (gameQueryService.canPlayerLifeChange(gameData, player.getId())
                            && gameData.getLife(player.getId()) >= lifeOwed) {
                        if (lifeOwed > 0 && payLifeAskingDeathReplacementsFirst(gameData, player, ability, lifeOwed)) {
                            return;
                        }
                        blackAsLifeCost.payPhyrexianManaAuto(pool, 0);
                        blackAsLifeCost.payResolutionCost(pool);
                        paidWithLife = true;
                    }
                }
                if (!paidWithLife && (!cost.canPayResolutionCost(pool) || !canPayAdditionalLife)) {
                    gameLogService.append(gameData, GameLog.textCardText(
                            player.getUsername() + " cannot pay " + ability.manaCost()
                                    + (ability.additionalLifeCost() > 0 ? " and " + ability.additionalLifeCost() + " life" : "")
                                    + " for ", ability.sourceCard(), "'s ability."));
                    gameData.resolvedMayAccepted = false;
                    if (gameData.pendingEffectResolutionEntry != null) { effectResolutionService.resolveEffectsFrom(gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex); }
                    if (gameData.interaction.isAwaitingInput()) { return; }
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                    return;
                }
                if (!paidWithLife) {
                    if (ability.additionalLifeCost() > 0 && payLifeAskingDeathReplacementsFirst(
                            gameData, player, ability, ability.additionalLifeCost())) {
                        return;
                    }
                    cost.payResolutionCost(pool);
                    triggerCollectionService.checkTaxPaymentTriggers(
                            gameData, player.getId(), ability.sourceControllerId(), cost.getManaValue());
                }
            }
            markOncePerTurnTriggerOnAcceptance(gameData, ability);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " accepts — resolving ", ability.sourceCard(), "'s ability."));
            gameData.resolvedMayAccepted = true;
        } else {
            gameLogService.append(gameData,
                    GameLog.playerDeclinesAbility(player.getUsername(), ability.sourceCard()));
            gameData.resolvedMayAccepted = false;
        }
        resumeAfterResolutionTimeMayChoice(gameData);
    }

    private boolean requiresSourceSacrifice(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof SacrificeSelfEffect) {
                return true;
            }
            if (effect instanceof SequenceEffect sequence
                    && requiresSourceSacrifice(sequence.steps())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Continues the resolving ability once the tap cost of its accepted "you may" is paid. Like every answer of a
     * resolving ability's own "you may", it chooses no target (see {@link #handleResolutionTimeMayChoice}).
     */
    public void continueAfterTapCost(GameData gameData, Player player, PendingMayAbility ability) {
        gameData.resolvingMayEffectFromStack = false;
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " accepts — resolving ", ability.sourceCard(), "'s ability."));
        gameData.resolvedMayAccepted = true;
        resumeAfterResolutionTimeMayChoice(gameData);
    }

    /**
     * Uses up a "do this only once each turn" ability (CR 603.2h) as its "you may" is accepted: the ability's limit
     * ({@code TriggerLimit.DO_ONLY_ONCE_EACH_TURN}) marked the resolving entry as its collector put it on the stack, so
     * no effect is read for it.
     */
    private void markOncePerTurnTriggerOnAcceptance(GameData gameData, PendingMayAbility ability) {
        if (ability.sourcePermanentId() != null && gameData.pendingEffectResolutionEntry != null) {
            TriggeredAbilityStackService.markActionTaken(gameData, ability.sourcePermanentId(),
                    gameData.pendingEffectResolutionEntry.getOnceEachTurnActionKey());
        }
    }

    private void handleAnyPlayerMayPayChoice(GameData gameData, Player player, boolean accepted,
                                             PendingMayAbility ability) {
        if (accepted && ability.manaCost() != null) {
            ManaCost cost = new ManaCost(ability.manaCost());
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            if (cost.canPayResolutionCost(pool)) {
                cost.payResolutionCost(pool);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + ability.manaCost() + ". (", ability.sourceCard(), ")"));
                gameData.anyPlayerMayPayManaRemainingPlayers.clear();
                gameData.resolvedMayAccepted = true;
                resumeAfterResolutionTimeMayChoice(gameData);
                return;
            }
        }

        if (!gameData.anyPlayerMayPayManaRemainingPlayers.isEmpty()) {
            UUID next = gameData.anyPlayerMayPayManaRemainingPlayers.removeFirst();
            gameData.resolvingMayEffectFromStack = true;
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    ability.sourceCard(), next, ability.effects(), ability.description(), ability.targetCardId(),
                    ability.manaCost(), ability.sourcePermanentId(), ability.tapPermanentsCost(),
                    ability.lifeCost(), ability.additionalLifeCost(), ability.attackedTargetId(),
                    ability.activePlayerId(), ability.choicePlayerId(), ability.sourcePermanentSnapshot(),
                    ability.sourceControllerId(), ability.triggeringCardId(), ability.eventValue()));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.anyPlayerMayPayManaRemainingPlayers.clear();
        gameData.resolvedMayAccepted = false;
        resumeAfterResolutionTimeMayChoice(gameData);
    }

    private void resumeAfterResolutionTimeMayChoice(GameData gameData) {
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData, gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /** The permanent of {@code ability}'s source card on its controller's battlefield, or {@code null}. */
    private static UUID sourcePermanentOnBattlefield(GameData gameData, PendingMayAbility ability) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(ability.controllerId());
        if (battlefield == null) {
            return null;
        }
        for (Permanent permanent : battlefield) {
            if (permanent.getCard() == ability.sourceCard()) {
                return permanent.getId();
            }
        }
        return null;
    }
}
