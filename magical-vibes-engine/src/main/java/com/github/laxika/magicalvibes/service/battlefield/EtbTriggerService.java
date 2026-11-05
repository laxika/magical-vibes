package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetGroup;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.model.effect.DraftProteanWarEngineSpellbookEffect;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.service.effect.entryfx.FlipCoinOnEnterEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DraftCardFromSpellbookToExileEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DraftProteanWarEngineSpellbookEffectHandler;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseIndependentModesOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeChoiceOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.TargetedGraveyardAndPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeredAbilityCounterEffect;
import com.github.laxika.magicalvibes.model.effect.BattlefieldAndGraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectContext;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectResolver;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.RulesEventPipeline;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class EtbTriggerService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final EtbEffectResolver etbEffectResolver;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private LegendRuleService legendRuleService;
    private DraftProteanWarEngineSpellbookEffectHandler proteanWarEngineSpellbookDraft;
    private DraftCardFromSpellbookToExileEffectHandler spellbookDraft;
    private FlipCoinOnEnterEffectHandler flipCoinOnEnterEffectHandler;
    private RulesEventPipeline rulesEventPipeline;

    public EtbTriggerService(GameQueryService gameQueryService,
                             GameLogService gameLogService,
                             PlayerInputService playerInputService,
                             @Lazy TriggerCollectionService triggerCollectionService,
                             GraveyardTargetingService graveyardTargetingService,
                             ETBTokenTargetService etbTokenTargetService,
                             EtbEffectResolver etbEffectResolver,
                             AmountEvaluationService amountEvaluationService,
                             ConditionEvaluationService conditionEvaluationService,
                             PredicateEvaluationService predicateEvaluationService,
                             GraveyardTargetingSupport graveyardTargetingSupport) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.graveyardTargetingService = graveyardTargetingService;
        this.etbTokenTargetService = etbTokenTargetService;
        this.etbEffectResolver = etbEffectResolver;
        this.amountEvaluationService = amountEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.graveyardTargetingSupport = graveyardTargetingSupport;
    }

    @Autowired
    void setLegendRuleService(@Lazy LegendRuleService legendRuleService) {
        this.legendRuleService = legendRuleService;
    }

    @Autowired
    void setProteanWarEngineSpellbookDraft(@Lazy DraftProteanWarEngineSpellbookEffectHandler draft) {
        this.proteanWarEngineSpellbookDraft = draft;
    }

    @Autowired
    void setSpellbookDraft(@Lazy DraftCardFromSpellbookToExileEffectHandler draft) {
        this.spellbookDraft = draft;
    }

    @Autowired
    void setRulesEventPipeline(@Lazy RulesEventPipeline rulesEventPipeline) {
        this.rulesEventPipeline = rulesEventPipeline;
    }

    @Autowired
    void setFlipCoinOnEnterEffectHandler(@Lazy FlipCoinOnEnterEffectHandler flipCoinOnEnterEffectHandler) {
        this.flipCoinOnEnterEffectHandler = flipCoinOnEnterEffectHandler;
    }

    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count) {
        triggerCollectionService.checkAllyTokenEntersTriggers(gameData, controllerId, count);
    }

    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId,
                                              List<UUID> permanentIds) {
        triggerCollectionService.checkAllyTokenEntersTriggers(gameData, controllerId, permanentIds);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, 0, false, List.of());
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, List<UUID> targetIds) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, 0, false, targetIds);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, false, List.of());
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked, List.of());
    }

    public void processLandETBEffects(GameData gameData, UUID controllerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        Permanent enteringPermanent = findEnteringPermanent(gameData, card);
        ChooseIndependentModesOnEnterEffect independentModeChoice = card.getStaticEffects().stream()
                .filter(ChooseIndependentModesOnEnterEffect.class::isInstance)
                .map(ChooseIndependentModesOnEnterEffect.class::cast)
                .findFirst().orElse(null);
        if (enteringPermanent != null && independentModeChoice != null
                && enteringPermanent.getChosenModeLabels().size() < independentModeChoice.modeGroups().size()) {
            playerInputService.beginChooseIndependentModesOnEnterChoice(gameData, controllerId, card,
                    enteringPermanent.getId(), independentModeChoice.modeGroups(),
                    enteringPermanent.getChosenModeLabels().size());
            return;
        }
        ChooseModeOnEnterEffect modeChoice = card.getStaticEffects().stream()
                .filter(ChooseModeOnEnterEffect.class::isInstance)
                .map(ChooseModeOnEnterEffect.class::cast)
                .findFirst().orElse(null);
        if (enteringPermanent != null && modeChoice != null) {
            if (modeChoice.eachPlayer()) {
                if (playerInputService.beginChooseModeOnEnterChoiceForEachPlayer(
                        gameData, card, enteringPermanent.getId(), modeChoice.modes())) {
                    return;
                }
            } else if (enteringPermanent.getChosenModeLabels().stream()
                    .filter(modeChoice.modes()::contains)
                    .count() < modeChoice.choicesRequired()) {
                playerInputService.beginChooseModeOnEnterChoice(gameData, controllerId, card,
                        enteringPermanent.getId(), modeChoice.modes(), modeChoice.choicesRequired(),
                        enteringPermanent.getChosenModeLabels().stream()
                                .filter(modeChoice.modes()::contains)
                                .toList());
                return;
            }
        }
        ChooseColorEffect colorChoice = card.getStaticEffects().stream()
                .filter(e -> e instanceof ChooseColorEffect)
                .map(e -> (ChooseColorEffect) e)
                .findFirst()
                .orElse(null);
        if (enteringPermanent != null && colorChoice != null && !colorChoice.choiceComplete(enteringPermanent)) {
            playerInputService.beginColorChoice(gameData, controllerId, enteringPermanent.getId(), null, colorChoice);
            return;
        }

        SubtypeChoiceOnEnterEffect subtypeChoice = card.getStaticEffects().stream()
                .filter(SubtypeChoiceOnEnterEffect.class::isInstance)
                .map(SubtypeChoiceOnEnterEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (enteringPermanent != null && subtypeChoice != null
                && (subtypeChoice.writesToBuddyList()
                ? !enteringPermanent.isBuddyListChoiceMade()
                : enteringPermanent.getChosenSubtype() == null)) {
            playerInputService.beginSubtypeChoice(gameData, controllerId, enteringPermanent.getId(),
                    subtypeChoice, true);
            return;
        }
        ChooseBasicLandTypeOnEnterEffect basicLandTypeChoice = card.getStaticEffects().stream()
                .filter(ChooseBasicLandTypeOnEnterEffect.class::isInstance)
                .map(ChooseBasicLandTypeOnEnterEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (enteringPermanent != null && enteringPermanent.getChosenSubtype() == null
                && basicLandTypeChoice != null) {
            playerInputService.beginBasicLandTypeChoice(gameData, controllerId, enteringPermanent.getId(),
                    false, basicLandTypeChoice.choicesRequired() > 1, basicLandTypeChoice.allowedTypes());
            return;
        }
        // "As this land enters, draft a card from its spellbook" is a replacement-style choice made while the
        // land enters, so it is offered before any other entry ability and never reaches the stack.
        DraftCardFromSpellbookToExileEffect spellbookDraftEffect = card.getStaticEffects()
                .stream()
                .filter(DraftCardFromSpellbookToExileEffect.class::isInstance)
                .map(DraftCardFromSpellbookToExileEffect.class::cast)
                .findFirst().orElse(null);
        if (enteringPermanent != null && spellbookDraftEffect != null
                && gameData.getCardsExiledByPermanent(enteringPermanent.getId()).isEmpty()) {
            spellbookDraft.beginDraft(gameData, controllerId, enteringPermanent.getId(), card.getName(),
                    spellbookDraftEffect);
            return;
        }
        processCreatureETBEffects(gameData, controllerId, card, null, false);
    }

    public void processFaceDownCreatureETBTriggers(GameData gameData, UUID controllerId, Card card) {
        if (gameQueryService.areCreatureETBTriggersSuppressed(gameData, card)) {
            log.info("Game {} - {} ETB triggers suppressed (creature entering triggers disabled)", gameData.id, card.getName());
            return;
        }

        processCreatureEntersTriggers(gameData, controllerId, card, 0, true);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked, List<UUID> targetIds) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, etbMode, kicked, targetIds);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                          boolean wasCastFromHand, int etbMode, int xValue,
                                          boolean kicked, List<UUID> targetIds) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds, List.of());
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                          boolean wasCastFromHand, int etbMode, int xValue,
                                          boolean kicked, List<UUID> targetIds,
                                          List<String> repeatedAdditionalCosts) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds, repeatedAdditionalCosts, List.of());
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                          boolean wasCastFromHand, int etbMode, int xValue,
                                          boolean kicked, List<UUID> targetIds,
                                          List<String> repeatedAdditionalCosts,
                                          List<UUID> convokeCreatureIds) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        Permanent enteringPermanent = findEnteringPermanent(gameData, card);
        if (kicked && enteringPermanent != null) {
            // Non-creature permanent spells reach ETB processing without the as-enters pass that
            // stamps this flag, but the ETB stack entry reads the kicked state from the permanent.
            enteringPermanent.setKicked(true);
        }
        if (wasCastFromHand || enteringPermanent != null && enteringPermanent.isCast()) {
            // A resolved permanent spell's ETB abilities choose their targets as they are put on the
            // stack (CR 603.3d), never while the spell was cast. Only an Aura's enchant target is a
            // real spell target, so it is the one cast-time choice carried onto the battlefield.
            targetIds = List.of();
            if (!card.isAura()) {
                targetId = null;
            }
        }
        // Krark's Thumb: the kept result of an "as this enters, flip a coin" flip is chosen as the permanent enters,
        // so its form is fixed before any of its enter abilities (CR 614.1c).
        if (enteringPermanent != null && flipCoinOnEnterEffectHandler != null
                && flipCoinOnEnterEffectHandler.beginPendingResultChoice(gameData, enteringPermanent,
                new ChoiceContext.EntryCoinFlipResultChoice(enteringPermanent.getId(), controllerId, card, targetId,
                        wasCastFromHand, etbMode, xValue, kicked, targetIds, repeatedAdditionalCosts,
                        convokeCreatureIds))) {
            return;
        }
        // A permanent that lost its printed abilities as it entered makes none of its own as-enters choices.
        boolean printedAbilitiesRemoved = enteringPermanent != null
                && gameQueryService.hasLostPrintedAbilities(gameData, enteringPermanent);
        ChooseIndependentModesOnEnterEffect independentModeChoice = printedAbilitiesRemoved ? null
                : card.getStaticEffects().stream()
                .filter(ChooseIndependentModesOnEnterEffect.class::isInstance)
                .map(ChooseIndependentModesOnEnterEffect.class::cast)
                .findFirst().orElse(null);
        if (enteringPermanent != null && independentModeChoice != null
                && enteringPermanent.getChosenModeLabels().size() < independentModeChoice.modeGroups().size()) {
            playerInputService.beginChooseIndependentModesOnEnterChoice(gameData, controllerId, card,
                    enteringPermanent.getId(), independentModeChoice.modeGroups(),
                    enteringPermanent.getChosenModeLabels().size());
            return;
        }
        ChooseModeOnEnterEffect modeChoice = printedAbilitiesRemoved ? null
                : card.getStaticEffects().stream()
                .filter(ChooseModeOnEnterEffect.class::isInstance)
                .map(ChooseModeOnEnterEffect.class::cast)
                .findFirst().orElse(null);
        if (enteringPermanent != null && modeChoice != null) {
            if (modeChoice.eachPlayer()) {
                if (playerInputService.beginChooseModeOnEnterChoiceForEachPlayer(
                        gameData, card, enteringPermanent.getId(), modeChoice.modes())) {
                    return;
                }
            } else if (enteringPermanent.getChosenModeLabels().stream().noneMatch(modeChoice.modes()::contains)) {
                playerInputService.beginChooseModeOnEnterChoice(gameData, controllerId, card,
                        enteringPermanent.getId(), modeChoice.modes());
                return;
            }
        }
        SubtypeChoiceOnEnterEffect subtypeChoice = printedAbilitiesRemoved ? null
                : card.getStaticEffects().stream()
                .filter(SubtypeChoiceOnEnterEffect.class::isInstance)
                .map(SubtypeChoiceOnEnterEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (enteringPermanent != null && subtypeChoice != null
                && (subtypeChoice.writesToBuddyList()
                ? !enteringPermanent.isBuddyListChoiceMade()
                : enteringPermanent.getChosenSubtype() == null)) {
            playerInputService.beginSubtypeChoice(gameData, controllerId, enteringPermanent.getId(), subtypeChoice);
            return;
        }

        // "As this enters, draft a card from its spellbook and exile it" is made while the permanent enters
        // (CR 614.12), not as a triggered ability, so it waits for the draft before any entry trigger.
        if (enteringPermanent != null && !printedAbilitiesRemoved
                && card.getStaticEffects().stream()
                .anyMatch(DraftProteanWarEngineSpellbookEffect.class::isInstance)
                && gameData.getCardsExiledByPermanent(enteringPermanent.getId()).isEmpty()) {
            proteanWarEngineSpellbookDraft.beginDraft(gameData, controllerId, enteringPermanent.getId());
            return;
        }

        if (gameQueryService.areArtifactOrCreatureETBTriggersSuppressed(gameData, card)) {
            log.info("Game {} - {} ETB triggers suppressed (entering permanent triggers disabled)",
                    gameData.id, card.getName());
            return;
        }

        boolean enteringPermanentTriggersSuppressed = gameQueryService
                .areOpponentPermanentETBTriggersSuppressed(gameData, controllerId);
        int extraEtbTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, card);

        // Each "when this enters" ability is one object on the stack (CR 603.3), judged as a whole: "triggers only
        // once each turn" (MACH-1, Oasis of Renewal) is read from the ability and marked by its key, which the
        // ability's other conditions share. The card's printed abilities come first, then the granted ones, each
        // ability's effects in the form TriggeredAbility.ownEntryEffects() gives.
        List<OwnEntryAbility> printedAbilities = new ArrayList<>();
        if (!enteringPermanentTriggersSuppressed && !printedAbilitiesRemoved) {
            for (TriggeredAbility ability : card.getOwnEntryAbilities()) {
                if (ownEntryLimitAvailable(gameData, enteringPermanent, ability)) {
                    printedAbilities.add(new OwnEntryAbility(ability, card.getOwnEntryEffectRegistrations(ability)
                            .stream().map(EffectRegistration::effect).toList()));
                }
            }
        }
        List<OwnEntryAbility> grantedAbilities = new ArrayList<>();
        if (!enteringPermanentTriggersSuppressed && enteringPermanent != null) {
            addGrantedOwnEntryAbilities(gameData, enteringPermanent, enteringPermanent.getGrantedOwnEntryAbilities(),
                    grantedAbilities);
        }
        if (!enteringPermanentTriggersSuppressed && !printedAbilitiesRemoved) {
            // A perpetual "when this enters" grant is an ability of its own.
            for (CardEffect effect : gameData.perpetualEnterEffectsByCardId.getOrDefault(card.getId(), List.of())) {
                grantedAbilities.add(new OwnEntryAbility(null, List.of(effect)));
            }
        }
        int additionalElementalTriggers = enteringPermanent == null ? 0
                : gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        gameData, controllerId, enteringPermanent);
        int extraTriggerCopies = extraEtbTriggers + additionalElementalTriggers;
        if (!enteringPermanentTriggersSuppressed && battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard() == card) {
                    addGrantedOwnEntryAbilities(gameData, enteringPermanent,
                            triggerCollectionService.grantedOwnEntryAbilities(gameData, permanent),
                            grantedAbilities);
                    break;
                }
            }
        }
        if (!printedAbilities.isEmpty() || !grantedAbilities.isEmpty()) {
            // A modal ETB on a permanent that was not cast has no cast-time mode; its mode is chosen
            // when the trigger is put on the stack (CR 603.3c), so it takes the trigger-time path.
            // A mandatory exact choose-one ETB on a cast creature whose spell declared no mode
            // (negative mode; multi-mode selections are negative bitmasks) takes the same path.
            boolean modeNotDeclaredAtCast = wasCastFromHand && etbMode < 0;
            if ((!wasCastFromHand && enteringPermanent != null && !enteringPermanent.isCast())
                    || modeNotDeclaredAtCast) {
                java.util.function.UnaryOperator<CardEffect> toTriggerTime = e ->
                        e instanceof ChooseOneEffect coe && coe.choicesRequired() <= 1
                                && (!modeNotDeclaredAtCast || (!coe.optional() && coe.choicesMax() <= 1))
                                ? new ChooseOneAtTriggerTimeEffect(coe) : e;
                printedAbilities.replaceAll(ability -> ability.map(toTriggerTime));
                grantedAbilities.replaceAll(ability -> ability.map(toTriggerTime));
            }

            // Evoke sacrifice gate (CR 603.4): read the just-entered permanent's evoked flag, which
            // was stamped from the spell's cast context at resolution time.
            boolean evoked = enteringPermanent != null && enteringPermanent.isEvoked();
            // Prowl gate (CR 603.4): read the just-entered permanent's prowl flag, stamped from the
            // spell's cast context at resolution time.
            boolean prowl = enteringPermanent != null && enteringPermanent.isProwl();
            boolean madness = enteringPermanent != null && enteringPermanent.isMadness();
            boolean alternateCost = enteringPermanent != null && enteringPermanent.isAlternateCost();
            // The context the abilities trigger in. Each one's event condition (CR 603.2) and intervening "if"
            // (CR 603.4) are judged first: an ability that doesn't trigger is left out. Then each mandatory effect is
            // resolved into its trigger-time form (modal unwrap, value materialisation, the facts fixed as the
            // permanent entered) — a null result drops the effect, and an ability with no effect left doesn't trigger.
            EtbEffectContext etbCtx = new EtbEffectContext(gameData, card, controllerId, wasCastFromHand, etbMode,
                    kicked, evoked, prowl, madness, alternateCost, enteringPermanent, repeatedAdditionalCosts,
                    targetId, xValue);
            printedAbilities.removeIf(ability -> !ownEntryOptionsMet(etbCtx, ability));
            grantedAbilities.removeIf(ability -> !ownEntryOptionsMet(etbCtx, ability));

            // The per-mode targetFilter of a cast-time ChooseOneEffect, for the ability that has it.
            TargetFilter modeTargetFilter = null;
            OwnEntryAbility modalAbility = null;
            for (OwnEntryAbility ability : concat(printedAbilities, grantedAbilities)) {
                for (CardEffect e : ability.effects()) {
                    if (modalAbility == null && e instanceof ChooseOneEffect coe) {
                        int idx = (etbMode >= 0 && etbMode < coe.options().size()) ? etbMode : 0;
                        modeTargetFilter = coe.options().get(idx).targetFilter();
                        modalAbility = ability;
                    }
                }
            }

            // An ability that is one "you may" goes through the may paths, which ask before it targets nothing
            // (CR 603.3d); a "you may" among an ability's other effects is asked as that ability resolves (CR 603.5).
            List<CardEffect> mayEffects = concat(printedAbilities, grantedAbilities).stream()
                    .filter(ability -> ability.effects().size() == 1
                            && ability.effects().getFirst() instanceof MayEffect)
                    .map(ability -> ability.effects().getFirst())
                    .toList();
            List<ChooseOneAtTriggerTimeEffect> triggerTimeChoices = concat(printedAbilities, grantedAbilities)
                    .stream().flatMap(ability -> ability.effects().stream())
                    .filter(ChooseOneAtTriggerTimeEffect.class::isInstance)
                    .map(ChooseOneAtTriggerTimeEffect.class::cast)
                    .toList();
            boolean hasEvokeSacrifice = printedAbilities.stream().flatMap(ability -> ability.effects().stream())
                    .anyMatch(com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect.class::isInstance);
            UUID triggerSourcePermanentId = enteringPermanent != null ? enteringPermanent.getId() : null;
            List<OwnEntryAbility> triggered = new ArrayList<>();
            List<CardEffect> grantedMandatoryEffects = new ArrayList<>();
            // The granted abilities, each an object of its own with its own targets.
            for (OwnEntryAbility ability : grantedAbilities) {
                List<CardEffect> abilityEffects = mandatoryEffects(etbCtx, ability.effects());
                grantedMandatoryEffects.addAll(abilityEffects);
                if (!abilityEffects.isEmpty() || ability.triggersWithoutMandatoryEffects()) {
                    triggered.add(ability);
                }
                if (!abilityEffects.isEmpty()) {
                    queueSeparateOwnEntryAbility(gameData, controllerId, card, targetId, targetIds, abilityEffects,
                            ability == modalAbility ? modeTargetFilter : null, extraTriggerCopies, etbMode, xValue,
                            kicked, enteringPermanent, repeatedAdditionalCosts, convokeCreatureIds);
                }
            }
            if (evoked && hasEvokeSacrifice) {
                queueMandatoryETBEffects(gameData, controllerId, card, null, List.of(),
                        List.of(new com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect(true)),
                        null, extraTriggerCopies, etbMode, xValue, repeatedAdditionalCosts, convokeCreatureIds);
            }
            for (ChooseOneAtTriggerTimeEffect triggerTimeChoice : triggerTimeChoices) {
                ChooseOneEffect choice = triggerTimeChoice.choice();
                boolean additionalModesAllowed = choice.additionalModesCondition() == null
                        || conditionEvaluationService.isMet(
                                gameData,
                                choice.additionalModesCondition(),
                                enteringPermanent != null
                                        ? ConditionContext.forPermanent(enteringPermanent, controllerId)
                                        : ConditionContext.forCard(card, controllerId));
                int minimumChoices = choice.choicesRequired();
                int maximumChoices = choice.effectiveChoicesMax(additionalModesAllowed);
                if (additionalModesAllowed && choice.allModesWhenConditionMet()) {
                    minimumChoices = maximumChoices;
                }
                if (triggerTimeChoice.maximumChoices() != null) {
                    minimumChoices = 0;
                    maximumChoices = Math.min(
                            maximumChoices,
                            Math.max(0, amountEvaluationService.evaluate(gameData,
                                    triggerTimeChoice.maximumChoices(),
                                    new AmountContext(controllerId, enteringPermanent, null, xValue, 0))));
                    if (maximumChoices == 0) {
                        continue;
                    }
                }
                if (minimumChoices != choice.choicesRequired() || maximumChoices != choice.choicesMax()) {
                    choice = new ChooseOneEffect(choice.options(), choice.optional(), minimumChoices,
                            maximumChoices, choice.allModesWhenOptionalCostPaid(), choice.modesMayRepeat(),
                            choice.additionalModesCondition());
                }
                for (int i = 0; i < 1 + extraTriggerCopies; i++) {
                    gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                            card, controllerId, choice, triggerSourcePermanentId));
                }
            }

            for (CardEffect effect : mayEffects) {
                MayEffect may = (MayEffect) effect;
                if (may.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.SoulbondChoosePartnerEffect) {
                    Permanent source = findEnteringPermanent(gameData, card);
                    if (source == null || source.getPairedWithId() != null
                            || !gameQueryService.isCreature(gameData, source)
                            || gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                            .noneMatch(partner -> !partner.getId().equals(source.getId())
                                    && partner.getPairedWithId() == null
                                    && gameQueryService.isCreature(gameData, partner))) {
                        continue;
                    }
                }
                if (may.wrapped() instanceof ReturnTargetCardsFromGraveyardToHandEffect returnToHand) {
                    for (int i = 0; i < 1 + extraTriggerCopies; i++) {
                        graveyardTargetingService.handleReturnToHandETBTargeting(
                                gameData, controllerId, card, List.of(may), returnToHand);
                    }
                    continue;
                }
                if (may.wrapped() instanceof ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect shuffle) {
                    for (int i = 0; i < 1 + extraTriggerCopies; i++) {
                        graveyardTargetingService.handleShuffleIntoLibraryETBTargeting(
                                gameData, controllerId, card, List.of(may), shuffle);
                    }
                    continue;
                }
                if (may.wrapped() instanceof ExileCardsFromGraveyardEffect exile) {
                    for (int i = 0; i < 1 + extraTriggerCopies; i++) {
                        graveyardTargetingService.handleGraveyardExileETBTargeting(
                                gameData, controllerId, card, List.of(may), triggerSourcePermanentId, exile);
                    }
                    continue;
                }
                if (may.wrapped() instanceof ExchangeControlOfTargetPermanentsEffect exchange
                        && exchange.requireOpponentPowerNotGreater()) {
                    for (int i = 0; i < 1 + extraTriggerCopies; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.PucasMischiefOwnTarget(
                                card, controllerId, List.of(may), triggerSourcePermanentId));
                    }
                    continue;
                }
                if (may.wrapped() instanceof SacrificePermanentThenEffect
                        && may.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    for (int i = 0; i < 1 + extraTriggerCopies; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                card, controllerId, List.of(may), xValue));
                    }
                    continue;
                }
                // CR 603.3d: a "may [do X to] target permanent" ETB (e.g. Leonin Relic-Warder)
                // targets, so with no legal target the ability isn't put onto the stack at all —
                // the controller isn't even prompted. Skip queueing it in that case.
                if (mayEtbTargetsPermanentButHasNoLegalTarget(gameData, controllerId, card, may)) {
                    log.info("Game {} - {} may ETB ability not put on stack (no legal targets)",
                            gameData.id, card.getName());
                    continue;
                }
                TargetSpec mayTargetSpec = may.targetSpec();
                if (mayTargetSpec.admits(TargetPredicate.Kind.PERMANENT)
                        || mayTargetSpec.admits(TargetPredicate.Kind.PLAYER)
                        || mayTargetSpec.admits(TargetPredicate.Kind.SPELL)
                        || mayTargetSpec.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    queueMandatoryETBEffects(gameData, controllerId, card, targetId, targetIds,
                            List.of(may), null, extraTriggerCopies, etbMode, xValue,
                            repeatedAdditionalCosts, convokeCreatureIds);
                    continue;
                }
                queueMandatoryETBEffects(gameData, controllerId, card, targetId, targetIds,
                        List.of(may), null, extraTriggerCopies, etbMode, xValue,
                        repeatedAdditionalCosts, convokeCreatureIds);
            }

            // Held back while the entering permanent causes a legend rule violation: state-based actions
            // come before targets are chosen, and the auto-pass after the legend rule choice resumes this.
            if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)
                    && !gameData.interaction.isAwaitingInput()
                    && (legendRuleService == null || !legendRuleService.hasLegendRuleViolation(gameData, controllerId))) {
                triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
            }


            List<OwnEntryAbility> printedMandatory = new ArrayList<>();
            for (OwnEntryAbility ability : printedAbilities) {
                List<CardEffect> abilityEffects = mandatoryEffects(etbCtx, ability.effects());
                if (!abilityEffects.isEmpty() || ability.triggersWithoutMandatoryEffects()) {
                    triggered.add(ability);
                }
                if (!abilityEffects.isEmpty()) {
                    printedMandatory.add(new OwnEntryAbility(ability.ability(), abilityEffects,
                            ability == modalAbility));
                }
            }
            if (printedMandatory.size() == 1) {
                // The card's one ability takes the targets the entering event supplied. It never inherits a target
                // a granted ability declares, so losing that target cannot make it fizzle.
                List<CardEffect> abilityEffects = printedMandatory.getFirst().effects();
                boolean declaresNoTarget = grantedMandatoryEffects.stream()
                        .anyMatch(effect -> declaresTarget(card, effect))
                        && abilityEffects.stream().noneMatch(effect -> declaresTarget(card, effect));
                queueMandatoryETBEffects(gameData, controllerId, card,
                        declaresNoTarget ? null : targetId,
                        declaresNoTarget ? List.of() : targetIds,
                        abilityEffects,
                        declaresNoTarget || !printedMandatory.getFirst().modal() ? null : modeTargetFilter,
                        extraTriggerCopies, etbMode, xValue, repeatedAdditionalCosts, convokeCreatureIds);
            } else {
                // In the order the card prints them, as the trigger pipeline puts a source's abilities on the stack;
                // their controller orders them (CR 603.3b).
                for (OwnEntryAbility ability : printedMandatory) {
                    queueSeparateOwnEntryAbility(gameData, controllerId, card, targetId, targetIds,
                            ability.effects(), ability.modal() ? modeTargetFilter : null, extraTriggerCopies,
                            etbMode, xValue, kicked, enteringPermanent, repeatedAdditionalCosts, convokeCreatureIds);
                }
            }
            if (enteringPermanent != null) {
                for (OwnEntryAbility ability : triggered) {
                    if (ability.ability() != null && ability.ability().options().limit() != TriggerLimit.NONE) {
                        TriggeredAbilityStackService.markLimit(gameData, ability.ability().options().limit(),
                                enteringPermanent.getId(), ability.ability().limitKey());
                    }
                }
            }
        }

        processCreatureEntersTriggers(gameData, controllerId, card, extraEtbTriggers, false);
    }

    /**
     * One "when this enters" ability as the collector reads it: the ability ({@code null} for a perpetual grant,
     * which is only its effect), its effects, and whether it is the ability whose cast-time mode chose the target
     * filter.
     */
    private record OwnEntryAbility(TriggeredAbility ability, List<CardEffect> effects, boolean modal) {

        OwnEntryAbility(TriggeredAbility ability, List<CardEffect> effects) {
            this(ability, effects, false);
        }

        OwnEntryAbility map(java.util.function.UnaryOperator<CardEffect> operator) {
            return new OwnEntryAbility(ability, effects.stream().map(operator).toList(), modal);
        }

        /** Whether the ability goes on the stack through the may, modal or evoke paths rather than as effects. */
        boolean triggersWithoutMandatoryEffects() {
            return effects.size() == 1 && effects.getFirst() instanceof MayEffect
                    || effects.stream().anyMatch(effect -> effect instanceof ChooseOneAtTriggerTimeEffect
                    || effect instanceof com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect);
        }
    }

    /**
     * Whether {@code ability}'s event condition (CR 603.2) and intervening "if" (CR 603.4) let it trigger as the
     * permanent enters ({@link EtbEffectResolver#ownEntryConditionMet}). A perpetual grant has neither.
     */
    private boolean ownEntryOptionsMet(EtbEffectContext etbCtx, OwnEntryAbility ability) {
        if (ability.ability() == null) {
            return true;
        }
        TriggerOptions options = ability.ability().options();
        return (options.eventCondition() == null
                || etbEffectResolver.ownEntryConditionMet(etbCtx, options.eventCondition()))
                && (options.interveningIf() == null
                || etbEffectResolver.ownEntryConditionMet(etbCtx, options.interveningIf()));
    }

    private static <T> List<T> concat(List<T> first, List<T> second) {
        List<T> all = new ArrayList<>(first);
        all.addAll(second);
        return all;
    }

    /**
     * The effects of one "when this enters" ability that go on the stack as that ability, in their trigger-time form
     * (modal unwrap, value materialisation, intervening-if gating, CR 603.4): not an ability that is one "you may"
     * (the may paths put it there), a trigger-time mode choice (CR 603.3c) or the evoke sacrifice, which have paths
     * of their own. Empty when none is left, and then the ability doesn't trigger.
     */
    private List<CardEffect> mandatoryEffects(EtbEffectContext etbCtx, List<CardEffect> effects) {
        if (effects.size() == 1 && effects.getFirst() instanceof MayEffect) {
            return List.of();
        }
        return effects.stream()
                .filter(e -> !(e instanceof ChooseOneAtTriggerTimeEffect))
                .filter(e -> !(e instanceof com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect))
                .map(e -> e instanceof ChooseOneEffect chooseOne && chooseOne.choicesRequired() > 1
                        ? e : etbEffectResolver.resolve(etbCtx, e))
                .filter(Objects::nonNull)
                .toList();
    }

    /** Whether {@code effect} targets: it is bound to one of the card's target groups or declares a target. */
    private static boolean declaresTarget(Card card, CardEffect effect) {
        return card.getEffectTargetIndex(effect) >= 0
                || !com.github.laxika.magicalvibes.model.effect.TargetSpec.NONE.equals(effect.targetSpec());
    }

    /**
     * Puts one "when this enters" ability of a card with several, or a granted one, on the stack as an object of its
     * own (CR 603.3) with its own targets: those of its target group among the targets the entering event supplied,
     * none when it declares no target (so losing another ability's target cannot make it fizzle), and otherwise the
     * entering event's target.
     */
    private void queueSeparateOwnEntryAbility(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                              List<UUID> targetIds, List<CardEffect> abilityEffects,
                                              TargetFilter modeTargetFilter, int extraTriggerCopies, int etbMode,
                                              int xValue, boolean kicked, Permanent enteringPermanent,
                                              List<String> repeatedAdditionalCosts, List<UUID> convokeCreatureIds) {
        int abilityGroup = abilityEffects.stream().mapToInt(card::getEffectTargetIndex)
                .filter(index -> index >= 0).findFirst().orElse(-1);
        boolean declaresTarget = abilityEffects.stream()
                .anyMatch(effect -> !com.github.laxika.magicalvibes.model.effect.TargetSpec.NONE.equals(effect.targetSpec()));
        TargetFilter abilityFilter = modeTargetFilter;
        List<UUID> abilityTargets = targetIds;
        if (abilityGroup < 0 && !declaresTarget) {
            abilityFilter = null;
            abilityTargets = List.of();
        } else if (abilityGroup >= 0) {
            int offset = 0;
            abilityTargets = List.of();
            for (TargetGroup group : card.getTargetGroups()) {
                int maximum = kicked ? group.getKickedMaxTargets() : group.getMaxTargets();
                if (group.getDynamicMaxTargets() != null) {
                    maximum = amountEvaluationService.evaluate(gameData, group.getDynamicMaxTargets(),
                            new AmountContext(controllerId, enteringPermanent, null, xValue, 0, false,
                                    null, repeatedAdditionalCosts == null ? List.of() : repeatedAdditionalCosts,
                                    card));
                }
                if (group.isXScaled()) maximum = Math.min(maximum, xValue);
                int size = Math.min(Math.max(0, maximum), targetIds.size() - offset);
                if (group.getIndex() == abilityGroup) {
                    abilityFilter = kicked ? group.getKickedFilter() : group.getFilter();
                    abilityTargets = List.copyOf(targetIds.subList(offset, offset + size));
                    break;
                }
                offset += size;
            }
        }
        if (abilityFilter == null && abilityEffects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))) {
            abilityFilter = new com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter(
                    new com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate(),
                    "Target must satisfy this ability's restrictions");
        }
        UUID abilityTargetId = abilityTargets.isEmpty()
                ? abilityGroup == 0 || (abilityGroup < 0 && declaresTarget) ? targetId : null
                : abilityTargets.getFirst();
        queueMandatoryETBEffects(gameData, controllerId, card, abilityTargetId, abilityTargets, abilityEffects,
                abilityFilter, extraTriggerCopies, etbMode, xValue, repeatedAdditionalCosts, convokeCreatureIds);
    }

    /**
     * Whether the "when this enters" ability {@code ability} of {@code enteringPermanent} can trigger as far as its
     * limit goes ("triggers only once each turn", the only limit such an ability takes): its key isn't marked yet on
     * the permanent. The collector marks it once the ability went on the stack. Without the entering permanent
     * nothing is judged, as nothing could be marked.
     */
    private static boolean ownEntryLimitAvailable(GameData gameData, Permanent enteringPermanent,
                                                  TriggeredAbility ability) {
        TriggerLimit limit = ability.options().limit();
        if (limit == TriggerLimit.NONE || enteringPermanent == null) {
            return true;
        }
        return !TriggeredAbilityStackService.limitReached(gameData, limit, enteringPermanent.getId(),
                ability.limitKey());
    }

    /**
     * Adds to {@code abilities} each of {@code granted}, "when this enters" abilities a resolved effect or a static
     * ability gave the entering permanent, with its effects in the form {@link TriggeredAbility#ownEntryEffects()}
     * gives, if its limit lets it trigger ({@link #ownEntryLimitAvailable}).
     */
    private static void addGrantedOwnEntryAbilities(GameData gameData, Permanent enteringPermanent,
                                                    List<TriggeredAbility> granted, List<OwnEntryAbility> abilities) {
        for (TriggeredAbility ability : granted) {
            if (ownEntryLimitAvailable(gameData, enteringPermanent, ability)) {
                abilities.add(new OwnEntryAbility(ability, ability.ownEntryEffects().stream()
                        .map(EffectRegistration::effect).toList()));
            }
        }
    }

    private int countMultikickerPayments(Card card, List<String> repeatedAdditionalCosts) {
        return card.getCastingCosts().stream()
                .filter(RepeatableAdditionalManaCost.class::isInstance)
                .map(RepeatableAdditionalManaCost.class::cast)
                .mapToInt(cost -> cost.multikickerPaymentCount(repeatedAdditionalCosts))
                .sum();
    }

    private void snapshotAttachedPermanent(GameData gameData, StackEntry entry, Permanent sourcePermanent) {
        if (!sourcePermanent.isAttached() || sourcePermanent.getAttachedTo() == null) return;

        Permanent attached = gameQueryService.findPermanentById(gameData, sourcePermanent.getAttachedTo());
        if (attached == null) return;

        entry.setAttachedPermanentSnapshot(new Permanent(attached));
        entry.setTriggeringPermanentId(attached.getId());
        entry.setTriggeringPermanentControllerId(
                gameQueryService.findPermanentController(gameData, attached.getId()));
        entry.setTriggeringPermanentPowerAtTrigger(gameQueryService.getEffectivePower(gameData, attached));
    }

    private void processCreatureEntersTriggers(GameData gameData, UUID controllerId, Card card,
                                               int extraEtbTriggers, boolean faceDown) {
        int stackSizeBeforeEntersTriggers = gameData.stack.size();
        collectEntersTriggers(gameData, controllerId, card, extraEtbTriggers, faceDown);
        // Each triggered ability the entering permanent caused (constellation, landfall, ...) is taxed
        // like its own enters-the-battlefield ability.
        List<StackEntry> causedTriggers = new ArrayList<>(gameData.stack.subList(
                Math.min(stackSizeBeforeEntersTriggers, gameData.stack.size()), gameData.stack.size()));
        for (StackEntry causedTrigger : causedTriggers) {
            if (causedTrigger.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
                queueTriggeredAbilityCounters(gameData, causedTrigger);
            }
        }
    }

    /**
     * What the entering permanent triggers: the card abilities that see it enter, matched by the trigger pipeline,
     * then evolve, watchers, boons, emblems, abilities a spell registered until end of turn, face-up planes and
     * exiled voyages.
     */
    private void collectEntersTriggers(GameData gameData, UUID controllerId, Card card,
                                       int extraEtbTriggers, boolean faceDown) {
        Permanent entering = findEnteringPermanent(gameData, card);
        if (entering != null && rulesEventPipeline != null) {
            rulesEventPipeline.triggerEnteringObservers(gameData, entering, controllerId);
        }
        triggerCollectionService.checkAllyPermanentEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkAllyCreatureEntersTriggers(gameData, controllerId, card, extraEtbTriggers);
        Permanent enteringPermanent = findEnteringPermanent(gameData, card);
        if (!faceDown || enteringPermanent != null && gameQueryService.isArtifact(gameData, enteringPermanent)) {
            triggerCollectionService.checkAllyArtifactEntersTriggers(gameData, controllerId, card);
        }
        triggerCollectionService.checkOpponentCreatureEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkPlanarCreatureEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkPlanarAllyPermanentEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkCreatureEntersThisTurnTriggers(gameData, controllerId, card);
        triggerCollectionService.checkAnyPermanentEntersTriggers(gameData, controllerId, card);
        if (!faceDown && (enteringPermanent == null ? card.hasType(CardType.LAND)
                : gameQueryService.isLand(gameData, enteringPermanent))) {
            triggerCollectionService.checkAllyLandEntersTriggers(gameData, controllerId, card);
            triggerCollectionService.checkPlanarLandEntersTriggers(gameData, controllerId, card);
        }
    }

    /**
     * True when a "may" ETB ability targets a permanent (and only a permanent) via a concrete
     * predicate filter but no permanent on the battlefield satisfies it — meaning the targeted
     * triggered ability has no legal target and must not be put onto the stack (CR 603.3d).
     *
     * <p>Deliberately narrow: it mirrors the pure permanent-target branch of
     * {@code MayAbilityHandlerService.handleTargetedMayAbilityAccepted}. Abilities that can also
     * target a player (a player is always a legal target), that target a graveyard card (resolved
     * on a separate path), or that lack a {@link PermanentPredicateTargetFilter} (e.g. Clone-style
     * copy effects, which don't target) are left untouched and queue as before.
     */
    private boolean mayEtbTargetsPermanentButHasNoLegalTarget(GameData gameData, UUID controllerId,
                                                              Card card, MayEffect may) {
        CardEffect wrapped = may.wrapped();
        TargetSpec wrappedSpec = wrapped.targetSpec();
        if (!wrappedSpec.admits(TargetPredicate.Kind.PERMANENT)
                || wrappedSpec.admits(TargetPredicate.Kind.PLAYER)
                || wrappedSpec.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            return false;
        }
        PermanentPredicate effectPredicate = EffectResolution.targetPredicateOf(wrapped);
        TargetFilter targetFilter = card.getTargetFilter();
        if (targetFilter == null && effectPredicate == null) {
            return false;
        }
        FilterContext ctx = FilterContext.of(gameData)
                .withSourceCardId(card.getId())
                .withSourceControllerId(controllerId);
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                boolean matches = effectPredicate == null
                        || predicateEvaluationService.matchesPermanentPredicate(p, effectPredicate, ctx);
                if (matches && targetFilter != null) {
                    matches = predicateEvaluationService.checkTargetFilter(targetFilter, p, ctx).isEmpty();
                }
                if (matches) return false;
            }
        }
        return true;
    }

    /**
     * Routes the already-resolved mandatory ETB effects to the stack or to the appropriate
     * interactive target-selection queue, and processes any pending queue that isn't already
     * awaiting input. The effects have already been unwrapped/gated by {@link EtbEffectResolver}.
     *
     * <p>Effects are partitioned by the kind of target selection they need at trigger time:
     * graveyard-exile (multi-target), graveyard-cast and grant-flashback (single graveyard target),
     * spell-targeting (choose a spell on the stack), and everything else ("other"), which either
     * goes straight onto the stack (targets supplied by the entering event, such as an Aura's
     * attachment or a reflexive put-onto-the-battlefield effect) or is queued to choose targets as
     * the ability goes on the stack (CR 603.3d). Trigger-copy effects apply to every path via
     * {@code extraTriggerCopies}.
     */
    private void queueMandatoryETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                          List<UUID> targetIds, List<CardEffect> mandatoryEffects,
                                          TargetFilter modeTargetFilter, int extraTriggerCopies,
                                          int etbMode, int xValue,
                                          List<String> repeatedAdditionalCosts,
                                          List<UUID> convokeCreatureIds) {
        Permanent enteringPermanent = findEnteringPermanent(gameData, card);
        // Separate graveyard exile effects (need multi-target selection at trigger time)
        List<CardEffect> graveyardExileEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ExileCardsFromGraveyardEffect).toList();
        // Separate targeted graveyard-card exile effects (Disposal Mummy: "exile target card from an
        // opponent's graveyard"). Distinct from the whole-set ExileCardsFromGraveyardEffect above: the
        // scope decides which graveyards are searched, and targets are chosen at trigger time.
        List<CardEffect> graveyardCardsExileEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ExileGraveyardCardsEffect ege && ege.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                .toList();
        // Separate mixed-zone exile effects (Angel of Serenity: "up to three other target creatures
        // from the battlefield and/or creature cards from graveyards"): one card pool spanning both
        // zones, chosen at trigger time.
        List<CardEffect> mixedZoneChoiceEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof BattlefieldAndGraveyardCardChoosingEffect).toList();
        // Separate graveyard cast effects (need single-target selection at trigger time)
        List<CardEffect> graveyardCastEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect)
                .filter(e -> card.getEffectTargetIndex(e) < 0)
                .toList();
        // Separate graveyard flashback-grant effects (need single-target selection at trigger time)
        List<CardEffect> graveyardFlashbackEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof GrantFlashbackToTargetGraveyardCardEffect).toList();
        // Separate graveyard exile-and-may-play effects (need single-target selection at trigger time)
        List<CardEffect> graveyardMayPlayEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect).toList();
        // Separate opponent-graveyard steal effects (need single-target selection at trigger time)
        List<CardEffect> graveyardStealEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect
                        || e instanceof PutCardFromOpponentGraveyardOntoBattlefieldEffect
                        || e instanceof MayEffect may
                        && may.wrapped() instanceof PutCardFromOpponentGraveyardOntoBattlefieldEffect).toList();
        // Separate graveyard return-to-hand effects (need multi-target selection at trigger time)
        List<CardEffect> graveyardReturnToHandEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ReturnTargetCardsFromGraveyardToHandEffect).toList();
        // Separate controller-graveyard top-of-library effects (multi-target selection at trigger time)
        List<CardEffect> graveyardPutOnTopEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof PutTargetCardsFromGraveyardOnTopOfLibraryEffect).toList();
        // Separate graveyard return-to-battlefield effects (need multi-target selection at trigger time)
        List<CardEffect> graveyardReturnToBattlefieldEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ReturnTargetCardsFromGraveyardToBattlefieldEffect).toList();
        // Separate effects that first target a player and then choose cards from that player's graveyard.
        List<CardEffect> targetPlayerGraveyardChoiceEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof GraveyardCardChoosingEffect choosingEffect
                        && choosingEffect.choosesGraveyardCards()
                        && e.targetSpec().admits(TargetPredicate.Kind.PLAYER)).toList();
        // Separate controller-graveyard shuffle-into-library effects (multi-target at trigger time)
        List<CardEffect> graveyardShuffleIntoLibraryEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect).toList();
        // Separate targeted graveyard-return effects (e.g. Bladewing the Risen: "return target Dragon
        // permanent card from your graveyard to the battlefield"): any remaining graveyard-target effect
        // not covered by the specialized paths above. Its target is chosen as the trigger goes on the
        // stack via the shared SpellGraveyardTargetTrigger flow (identified by target category, not by
        // concrete effect type, so a new graveyard-target effect needs no branch here).
        List<CardEffect> graveyardTargetReturnEffects = mandatoryEffects.stream()
                .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                // A target-group declaration describes the ETB ability's target, but the
                // graveyard card is chosen when that ability goes on the stack.
                .filter(e -> !(e instanceof TargetedGraveyardAndPlayerEffect))
                .filter(e -> !graveyardExileEffects.contains(e))
                .filter(e -> !graveyardCardsExileEffects.contains(e))
                .filter(e -> !(e instanceof CastTargetInstantOrSorceryFromGraveyardEffect))
                .filter(e -> !graveyardCastEffects.contains(e))
                .filter(e -> !graveyardFlashbackEffects.contains(e))
                .filter(e -> !graveyardMayPlayEffects.contains(e))
                .filter(e -> !graveyardStealEffects.contains(e))
                .filter(e -> !graveyardReturnToHandEffects.contains(e))
                .filter(e -> !graveyardPutOnTopEffects.contains(e))
                .filter(e -> !graveyardReturnToBattlefieldEffects.contains(e))
                .filter(e -> !graveyardShuffleIntoLibraryEffects.contains(e))
                .toList();
        List<CardEffect> otherEffects = graveyardExileEffects.isEmpty()
                ? mandatoryEffects.stream()
                        .filter(e -> !(e instanceof ExileCardsFromGraveyardEffect))
                        .filter(e -> !graveyardCastEffects.contains(e))
                        .filter(e -> !(e instanceof GrantFlashbackToTargetGraveyardCardEffect))
                        .filter(e -> !(e instanceof ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect))
                        .filter(e -> !graveyardStealEffects.contains(e))
                        .filter(e -> !graveyardPutOnTopEffects.contains(e))
                        .filter(e -> !(e instanceof ReturnTargetCardsFromGraveyardToHandEffect))
                        .filter(e -> !graveyardReturnToBattlefieldEffects.contains(e))
                        .filter(e -> !targetPlayerGraveyardChoiceEffects.contains(e))
                        .filter(e -> !(e instanceof ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect))
                        .filter(e -> !graveyardTargetReturnEffects.contains(e))
                        .filter(e -> !graveyardCardsExileEffects.contains(e))
                        .filter(e -> !mixedZoneChoiceEffects.contains(e))
                        .filter(e -> !EffectResolution.targetsSpellOnStack(e)
                                || isMixedPermanentAndSpellTarget(e)).toList()
                : List.of();
        boolean combinesGraveyardCardExileWithOtherEffects = graveyardCardsExileEffects.size() == 1
                && !otherEffects.isEmpty()
                && otherEffects.size() + graveyardCardsExileEffects.size() == mandatoryEffects.size()
                && targetId == null
                && targetIds.isEmpty();
        // Separate spell-targeting effects (need stack-target selection at trigger time)
        List<CardEffect> spellTargetEffects = mandatoryEffects.stream()
                .filter(EffectResolution::targetsSpellOnStack)
                .filter(e -> !isMixedPermanentAndSpellTarget(e)).toList();

        List<Permanent> sourceBattlefield = gameData.playerBattlefields.get(controllerId);
        boolean sourceWasCastForSpectacle = enteringPermanent != null
                && enteringPermanent.isSpectacle();
        boolean collectEvidenceCostPaid = enteringPermanent != null
                && enteringPermanent.isCollectEvidenceCostPaid();
        boolean waterbendCostPaid = enteringPermanent != null
                && enteringPermanent.isWaterbendCostPaid();
        boolean revealCardFromHandCostPaid = enteringPermanent != null
                && enteringPermanent.isRevealCardFromHandCostPaid();
        boolean controlledDragonAsCast = enteringPermanent != null
                && enteringPermanent.isControlledDragonAsCast();

        // Put non-special effects on the stack as before
        if (!otherEffects.isEmpty() && !combinesGraveyardCardExileWithOtherEffects) {
            List<UUID> activeTargetIds = targetsForActiveEtbGroups(card, otherEffects, targetIds);
            Map<UUID, Integer> dividedAssignments = otherEffects.stream().anyMatch(effect ->
                    effect instanceof com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect divided
                            && divided.etbAssignments()
                            || effect instanceof com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect prevention
                            && prevention.etbAssignments()
                            || effect instanceof com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect distribution
                            && distribution.etbAssignments()
                            || effect instanceof com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect
                            || effect instanceof com.github.laxika.magicalvibes.model.effect.MayEffect may
                            && may.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect)
                    ? new java.util.LinkedHashMap<>(gameData.pendingETBDamageAssignments) : Map.of();
            if (!dividedAssignments.isEmpty() && activeTargetIds.isEmpty()
                    && otherEffects.stream().noneMatch(
                    com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect.class::isInstance)) {
                activeTargetIds = List.copyOf(dividedAssignments.keySet());
            }
            boolean hasTarget = targetId != null || !activeTargetIds.isEmpty();

            // An ETB ability with no target supplied by the entering event chooses its targets as it
            // is put on the stack (CR 603.3d). That covers every resolved permanent spell, token
            // copies, reanimated permanents and lands alike, including "up to N" abilities, whose
            // controller may still choose zero targets at that point.
            boolean hasDynamicTargetCount = card.hasDynamicTargetCount();
            // An effect bound to one of the card's declared target groups targets through that group's
            // filter even when its own spec names no target kind (an Aura's group 0 is its enchant
            // target, never an ETB target).
            boolean etbNeedsTarget = otherEffects.stream()
                    .filter(e -> !e.targetsChosenByReflexiveAbility())
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            || e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                            || e.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD)
                            || card.getEffectTargetIndex(e) > (card.isAura() ? 0 : -1));

            // A surviving gate-conditional ETB (Metalcraft, Morbid, Raid, … — the gate was met
            // as the permanent entered) that targets picks the target as the trigger goes on the
            // stack (CR 603.3d), even when the entering event supplied a target for another ETB.
            boolean gateConditionalNeedsTarget = otherEffects.stream()
                    .anyMatch(e -> e instanceof ConditionalEffect ce && ce.condition().isEtbTriggerGate()
                            && (!card.isAura() || card.getEffectTargetIndex(e) != 0)
                            && (ce.targetSpec().admits(TargetPredicate.Kind.PLAYER) || ce.targetSpec().admits(TargetPredicate.Kind.PERMANENT)));

            // MayPayManaEffect ETBs never take a target from the entering event, so a
            // targeting pay/else ability (Knight of the Mists) must choose as the trigger goes on the
            // stack — including the just-entered permanent as a legal choice.
            boolean mayPayManaNeedsTarget = otherEffects.stream()
                    .anyMatch(e -> e instanceof MayPayManaEffect
                            && (e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            || e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)));

            boolean auraETBTargetNeedsSelection = card.isAura()
                    && targetIds.isEmpty()
                    && otherEffects.stream().anyMatch(e -> card.getEffectTargetIndex(e) != 0
                    && (e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)));

            if ((hasDynamicTargetCount && etbNeedsTarget && !hasTarget)
                    || hasUnselectedDynamicEtbTargetGroup(card, otherEffects, targetId, targetIds)
                    || gateConditionalNeedsTarget
                    || mayPayManaNeedsTarget
                    || auraETBTargetNeedsSelection
                    || (etbNeedsTarget && !hasTarget)) {
                // CR 603.3d: the controller chooses the ETB targets as the triggered ability is put
                // on the stack.
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                UUID sourcePermanentId = enteringPermanent != null ? enteringPermanent.getId() : null;

                if (card.getTargetGroups().size() > 1 || etbTokenTargetService.needsSlotBySlotTargetSelection(card)) {
                    // Multi-target ETB (e.g. Burning Sun's Avatar, or a single group with
                    // "up to N" targets): choose slot-by-slot at trigger time,
                    // accumulating into targetIds.
                    List<UUID> initialTargets = card.isAura() || targetId == null
                            ? List.of() : List.of(targetId);
                    int initialGroupIndex = card.isAura() ? 1 : targetId == null ? 0 : 1;
                    List<Integer> initialGroupSizes = card.isAura()
                            ? List.of(0) : targetId == null ? List.of() : List.of(1);
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                            card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId,
                            initialTargets, initialGroupIndex, 0,
                            initialGroupSizes, xValue,
                            repeatedAdditionalCosts == null ? List.of() : List.copyOf(repeatedAdditionalCosts)));
                    for (int i = 0; i < extraTriggerCopies; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId,
                                initialTargets, initialGroupIndex, 0,
                                initialGroupSizes, xValue,
                                repeatedAdditionalCosts == null ? List.of() : List.copyOf(repeatedAdditionalCosts)));
                    }
                    gameLogService.append(gameData,
                            GameLog.cardThen(card, "'s enter-the-battlefield ability triggers — choose targets."));
                    log.info("Game {} - {} ETB multi-target trigger queued for target selection",
                            gameData.id, card.getName());
                } else {
                    TargetFilter etbTargetFilter = modeTargetFilter != null ? modeTargetFilter
                            : card.isAura() && otherEffects.stream().noneMatch(effect -> card.getEffectTargetIndex(effect) >= 0)
                            ? null : card.getTargetFilter();

                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                            card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, etbTargetFilter, xValue));
                    for (int i = 0; i < extraTriggerCopies; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                                card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, etbTargetFilter, xValue));
                    }
                    gameLogService.append(gameData,
                            GameLog.cardThen(card, "'s enter-the-battlefield ability triggers — choose a target."));
                    log.info("Game {} - {} ETB trigger queued for target selection",
                            gameData.id, card.getName());
                }
            } else if (extraTriggerCopies == 0 && dividedAssignments.isEmpty() && activeTargetIds.size() > 1
                    && otherEffects.size() == 1
                    && otherEffects.getFirst() instanceof com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect divided
                    && divided.mode() == com.github.laxika.magicalvibes.model.effect.DivisionMode.CHOSEN
                    && divided.etbAssignments() && divided.targetRestriction() != null) {
                // CR 601.2d / CR 603.3d: the division of damage among several chosen targets is announced
                // as the ability is put on the stack, before anyone can respond. The multi-target trigger
                // flow asks for each target's share and then pushes the entry carrying that division.
                UUID sourcePermanentId = enteringPermanent != null ? enteringPermanent.getId() : null;
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId,
                        new ArrayList<>(activeTargetIds), card.getTargetGroups().size(), 0,
                        List.of(activeTargetIds.size()), xValue,
                        repeatedAdditionalCosts == null ? List.of() : List.copyOf(repeatedAdditionalCosts)));
                gameLogService.append(gameData,
                        GameLog.cardThen(card, "'s enter-the-battlefield ability triggers — divide its damage."));
                log.info("Game {} - {} ETB trigger queued for damage division", gameData.id, card.getName());
            } else if (!etbNeedsTarget || hasTarget) {
                if (!dividedAssignments.isEmpty()) {
                    gameData.pendingETBDamageAssignments = Map.of();
                }
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                UUID sourcePermanentId = enteringPermanent != null ? enteringPermanent.getId() : null;

                // Snapshot the paid X onto the ETB stack entry so DynamicAmount XValue effects read
                // the cast context on resolution.
                StackEntry etbEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ETB ability",
                        new ArrayList<>(otherEffects),
                        xValue,
                        targetId,
                        sourcePermanentId,
                        dividedAssignments,
                        null,
                        List.of(),
                        dividedAssignments.isEmpty() ? activeTargetIds : new ArrayList<>(dividedAssignments.keySet())
                );
                if (repeatedAdditionalCosts != null && !repeatedAdditionalCosts.isEmpty()) {
                    etbEntry.setRepeatedAdditionalCosts(List.copyOf(repeatedAdditionalCosts));
                }
                etbEntry.setConvokeCreatureIds(convokeCreatureIds);
                // Dynamic amounts such as "gain 3 life for each time it was kicked" read the kicked
                // state from the resolving entry, not from the trigger-time condition context.
                if (enteringPermanent != null && enteringPermanent.isKicked()) {
                    etbEntry.setKicked(true);
                }
                if (enteringPermanent != null && enteringPermanent.isEvoked() && card.hasSeveralOwnEntryAbilities()) {
                    etbEntry.setTriggeringCardId(card.getId());
                }
                if (card.isAura() && !etbNeedsTarget
                        && otherEffects.stream().noneMatch(effect -> effect.targetSpec().declaredTarget() != null
                        || EffectResolution.targetsSpellOnStack(effect))) {
                    etbEntry.setNonTargeting(true);
                }
                if (targetId != null && otherEffects.stream().anyMatch(EffectResolution::targetsSpellOnStack)
                        && gameQueryService.findStackEntryByCardId(gameData, targetId) != null) {
                    etbEntry.setTargetZone(com.github.laxika.magicalvibes.model.Zone.STACK);
                }
                if (modeTargetFilter != null) {
                    etbEntry.setTargetFilter(modeTargetFilter);
                }
                Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                if (sourcePermanent != null) {
                    etbEntry.setSourcePermanentSnapshot(new Permanent(sourcePermanent));
                    etbEntry.setSacrificedToughness(sourcePermanent.getAlternateCostSacrificedToughness());
                    snapshotAttachedPermanent(gameData, etbEntry, sourcePermanent);
                }
                etbEntry.setSpectacle(sourceWasCastForSpectacle);
                etbEntry.setCollectEvidenceCostPaid(collectEvidenceCostPaid);
                etbEntry.setWaterbendCostPaid(waterbendCostPaid);
                etbEntry.setRevealCardFromHandCostPaid(revealCardFromHandCostPaid);
                etbEntry.setControlledDragonAsCast(controlledDragonAsCast);
                etbEntry.setGiftPromised(enteringPermanent != null && enteringPermanent.isGiftPromised());
                gameData.stack.add(etbEntry);
                queueTriggeredAbilityCounters(gameData, etbEntry);
                gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability triggers."));
                log.info("Game {} - {} ETB ability pushed onto stack", gameData.id, card.getName());
                // Naban: extra triggers for Wizard ETB. A copy of a single-target ETB chooses its own
                // target as it goes on the stack (CR 603.3d), so it does not reuse the cast-time target.
                boolean extraCopiesChooseTargets = extraTriggerCopies > 0 && etbNeedsTarget && targetId != null
                        && !card.isAura() && !hasDynamicTargetCount && card.getTargetGroups().size() <= 1;
                if (extraCopiesChooseTargets) {
                    TargetFilter copyTargetFilter = modeTargetFilter != null ? modeTargetFilter : card.getTargetFilter();
                    for (int i = 0; i < extraTriggerCopies; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                                card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, copyTargetFilter, xValue));
                    }
                }
                for (int i = 0; i < (extraCopiesChooseTargets ? 0 : extraTriggerCopies); i++) {
                    StackEntry extraEtbEntry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            controllerId,
                            card.getName() + "'s ETB ability",
                            new ArrayList<>(otherEffects),
                            xValue,
                            targetId,
                            sourcePermanentId,
                            dividedAssignments,
                            null,
                            List.of(),
                            dividedAssignments.isEmpty() ? activeTargetIds : new ArrayList<>(dividedAssignments.keySet())
                    );
                    if (repeatedAdditionalCosts != null && !repeatedAdditionalCosts.isEmpty()) {
                        extraEtbEntry.setRepeatedAdditionalCosts(List.copyOf(repeatedAdditionalCosts));
                    }
                    extraEtbEntry.setConvokeCreatureIds(convokeCreatureIds);
                    extraEtbEntry.setTriggeringCardId(etbEntry.getTriggeringCardId());
                    extraEtbEntry.setNonTargeting(etbEntry.isNonTargeting());
                    extraEtbEntry.setTargetZone(etbEntry.getTargetZone());
                    if (modeTargetFilter != null) {
                        extraEtbEntry.setTargetFilter(modeTargetFilter);
                    }
                    if (sourcePermanent != null) {
                        extraEtbEntry.setSourcePermanentSnapshot(new Permanent(sourcePermanent));
                        extraEtbEntry.setSacrificedToughness(sourcePermanent.getAlternateCostSacrificedToughness());
                        snapshotAttachedPermanent(gameData, extraEtbEntry, sourcePermanent);
                    }
                    extraEtbEntry.setSpectacle(sourceWasCastForSpectacle);
                    extraEtbEntry.setCollectEvidenceCostPaid(collectEvidenceCostPaid);
                    extraEtbEntry.setWaterbendCostPaid(waterbendCostPaid);
                    extraEtbEntry.setRevealCardFromHandCostPaid(revealCardFromHandCostPaid);
                    extraEtbEntry.setControlledDragonAsCast(controlledDragonAsCast);
                    extraEtbEntry.setGiftPromised(etbEntry.isGiftPromised());
                    gameData.stack.add(extraEtbEntry);
                    queueTriggeredAbilityCounters(gameData, extraEtbEntry);
                    gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability triggers."));
                    log.info("Game {} - {} ETB ability pushed onto stack (Wizard ETB extra trigger)", gameData.id, card.getName());
                }
            }
        }

        // Handle effects that target a player and then choose cards from that player's graveyard.
        for (CardEffect effect : targetPlayerGraveyardChoiceEffects) {
            List<Permanent> enteredBattlefield = gameData.playerBattlefields.get(controllerId);
            UUID sourcePermanentId = enteringPermanent == null ? null : enteringPermanent.getId();
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                TargetFilter etbTargetFilter = modeTargetFilter != null ? modeTargetFilter : card.getTargetFilter();
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                        card, controllerId, List.of(effect), sourcePermanentId, etbTargetFilter, xValue));
            }
        }

        // Handle graveyard exile effects: targets must be chosen at trigger time
        List<Permanent> enteredBattlefield = gameData.playerBattlefields.get(controllerId);
        UUID graveyardSourcePermanentId = enteringPermanent == null ? null : enteringPermanent.getId();
        for (CardEffect effect : graveyardExileEffects) {
            ExileCardsFromGraveyardEffect exile = (ExileCardsFromGraveyardEffect) effect;
            int multikickerPaymentCount = exile.maxTargetsFromMultikicker()
                    ? countMultikickerPayments(card, repeatedAdditionalCosts) : 0;
            graveyardTargetingService.handleGraveyardExileETBTargeting(
                    gameData, controllerId, card, mandatoryEffects, graveyardSourcePermanentId, exile,
                    multikickerPaymentCount);
            // Each additional copy chooses its own targets, one after another, once the previous
            // trigger's targets have been chosen (processNextSpellGraveyardTargetTrigger).
            for (int t = 0; t < extraTriggerCopies; t++) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        card, controllerId, new ArrayList<>(mandatoryEffects), null, 0,
                        multikickerPaymentCount, 0, null, false, null, graveyardSourcePermanentId));
            }
        }

        // Handle targeted graveyard-card exile effects (opponent's/any graveyard, e.g. Disposal Mummy):
        // choose the graveyard target as the trigger goes on the stack.
        for (CardEffect effect : graveyardCardsExileEffects) {
            ExileGraveyardCardsEffect exile = (ExileGraveyardCardsEffect) effect;
            int maximumTargets = maximumGraveyardCardExileTargets(card, exile, gameData, controllerId,
                    xValue, repeatedAdditionalCosts);
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                List<CardEffect> effects = combinesGraveyardCardExileWithOtherEffects
                        ? mandatoryEffects : List.of(effect);
                graveyardTargetingService.handleGraveyardCardsExileETBTargeting(
                        gameData, controllerId, card, effects, exile, maximumTargets);
            }
        }

        // Handle mixed-zone exile effects: one selection across battlefield creatures and graveyard
        // creature cards, chosen as the trigger goes on the stack.
        for (CardEffect effect : mixedZoneChoiceEffects) {
            List<Permanent> mixedZoneBf = gameData.playerBattlefields.get(controllerId);
            UUID mixedZoneSourceId = enteringPermanent != null ? enteringPermanent.getId() : null;
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleBattlefieldAndGraveyardExileETBTargeting(gameData, controllerId,
                        card, List.of(effect), mixedZoneSourceId,
                        (BattlefieldAndGraveyardCardChoosingEffect) effect);
            }
        }

        // Handle graveyard cast effects: target instant/sorcery in opponent's graveyard
        for (CardEffect effect : graveyardCastEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleGraveyardCastETBTargeting(gameData, controllerId, card, List.of(effect));
            }
        }

        // Handle graveyard flashback-grant effects: target instant/sorcery in controller's graveyard
        for (CardEffect effect : graveyardFlashbackEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleGrantFlashbackETBTargeting(gameData, controllerId, card, List.of(effect));
            }
        }

        // Handle graveyard exile-and-may-play effects: target card in controller's graveyard
        for (CardEffect effect : graveyardMayPlayEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                UUID sourcePermanentId = enteringPermanent != null ? enteringPermanent.getId() : null;
                graveyardTargetingService.handleGraveyardMayPlayETBTargeting(
                        gameData, controllerId, card, List.of(effect), sourcePermanentId);
            }
        }

        // Handle opponent-graveyard steal effects: target creature card in an opponent's graveyard
        for (CardEffect effect : graveyardStealEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handlePutCreatureFromOpponentGraveyardETBTargeting(gameData, controllerId, card, List.of(effect));
            }
        }

        // Handle graveyard return-to-hand effects: up to N target cards in controller's graveyard
        for (CardEffect effect : graveyardReturnToHandEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleReturnToHandETBTargeting(gameData, controllerId, card,
                        List.of(effect), (ReturnTargetCardsFromGraveyardToHandEffect) effect);
            }
        }

        // Handle putting up to N cards from the controller's graveyard on top of the library
        for (CardEffect effect : graveyardPutOnTopEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handlePutOnTopOfLibraryETBTargeting(gameData, controllerId, card,
                        List.of(effect), (PutTargetCardsFromGraveyardOnTopOfLibraryEffect) effect);
            }
        }

        // Handle graveyard return-to-battlefield effects: up to the cast-context cap of target
        // creature cards from the controller's graveyard.
        for (CardEffect effect : graveyardReturnToBattlefieldEffects) {
            ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect =
                    (ReturnTargetCardsFromGraveyardToBattlefieldEffect) effect;
            int maxTargets = returnEffect.dynamicMaxTargets() == null
                    ? returnEffect.maxTargets()
                    : Math.max(0, amountEvaluationService.evaluate(gameData, returnEffect.dynamicMaxTargets(),
                            new AmountContext(controllerId, null, null, xValue, 0, false, null,
                                    repeatedAdditionalCosts == null ? List.of() : repeatedAdditionalCosts, card)));
            Integer maxTotalManaValue = null;
            if (returnEffect.hasTotalManaValueCap()) {
                maxTotalManaValue = returnEffect.dynamicMaxTotalManaValue() == null
                        ? returnEffect.maxTotalManaValue()
                        : Math.max(0, amountEvaluationService.evaluate(gameData,
                                returnEffect.dynamicMaxTotalManaValue(),
                                new AmountContext(controllerId, null, null, xValue, 0, false, null,
                                        repeatedAdditionalCosts == null ? List.of() : repeatedAdditionalCosts, card)));
            }
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleReturnToBattlefieldETBTargeting(gameData, controllerId, card,
                        List.of(effect), returnEffect, maxTargets, maxTotalManaValue, xValue,
                        returnEffect.attachToSourceHost() ? graveyardSourcePermanentId : null);
            }
        }

        // Handle shuffle-into-library effects: up to N target cards in controller's graveyard
        for (CardEffect effect : graveyardShuffleIntoLibraryEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleShuffleIntoLibraryETBTargeting(gameData, controllerId, card,
                        List.of(effect), (ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect) effect);
            }
        }

        // Handle targeted graveyard-return effects (return target card from your graveyard to the
        // battlefield/hand): choose the graveyard target as the trigger goes on the stack, reusing the
        // shared SpellGraveyardTargetTrigger flow. Optional effects use an up-to-one selection.
        for (CardEffect effect : graveyardTargetReturnEffects) {
            int minimumGraveyardTargets = minimumGraveyardTargets(card, effect);
            GraveyardTargetingSupport.Target target = graveyardTargetingSupport.findTarget(List.of(effect));
            int targetXValue = enteringPermanent != null && target != null
                    && target.maximumManaValue() instanceof com.github.laxika.magicalvibes.model.amount.ManaSpentToCast
                    ? enteringPermanent.getManaSpentToCast() : xValue;
            // A cap that counts the spell's repeated additional costs ("up to X target cards, where X
            // is the number of times it was kicked") is evaluated here, where those payments are known.
            int dynamicMaxTargets = target != null && target.maximumTargetCount() != null
                    && repeatedAdditionalCosts != null && !repeatedAdditionalCosts.isEmpty()
                    ? Math.max(0, amountEvaluationService.evaluate(gameData, target.maximumTargetCount(),
                            new AmountContext(controllerId, null, null, targetXValue, 0, false, null,
                                    repeatedAdditionalCosts, card)))
                    : 0;
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        card, controllerId, List.of(effect), null, minimumGraveyardTargets, targetXValue,
                        dynamicMaxTargets));
            }
        }
        // State-based actions come before a triggered ability's targets are chosen, so a legend rule
        // violation the entering permanent causes is settled first; the auto-pass that follows the
        // legend rule choice picks the held-back target choice up.
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()
                && (legendRuleService == null || !legendRuleService.hasLegendRuleViolation(gameData, controllerId))) {
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
        }

        // Handle spell-targeting ETB effects: target must be chosen from spells on the stack
        for (CardEffect effect : spellTargetEffects) {
            StackEntryPredicate spellFilter = null;
            boolean includeAbilities = false;
            if (effect instanceof CopySpellEffect cse) {
                spellFilter = cse.spellFilter();
            } else if (card.getTargetFilter() instanceof StackEntryPredicateTargetFilter sf) {
                // "counter target spell with mana value X or less" (Spellstutter Sprite): the
                // legal-spell restriction lives on the card's target filter, not the effect.
                spellFilter = sf.predicate();
                // "target spell or ability" (Mizzium Meddler) is signalled by a has-target filter.
                includeAbilities = TriggerCollectionService.predicateContainsHasTarget(sf.predicate());
            }
            List<Permanent> entered = gameData.playerBattlefields.get(controllerId);
            UUID sourcePermanentId = entered == null ? null : entered.stream()
                    .filter(p -> p.getCard().getId().equals(card.getId()))
                    .map(Permanent::getId)
                    .findFirst().orElse(null);
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                gameData.queueInteraction(new PermanentChoiceContext.ETBSpellTargetTrigger(
                        card, controllerId, List.of(effect), spellFilter, includeAbilities, sourcePermanentId));
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBSpellTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
        }
    }

    private int maximumGraveyardCardExileTargets(Card card, ExileGraveyardCardsEffect effect,
                                                  GameData gameData, UUID controllerId, int xValue,
                                                  List<String> repeatedAdditionalCosts) {
        int maximumTargets = effect.count();
        int targetIndex = card.getEffectTargetIndex(effect);
        if (targetIndex < 0 || targetIndex >= card.getTargetGroups().size()) {
            return maximumTargets;
        }
        TargetGroup target = card.getTargetGroups().get(targetIndex);
        if (target.getDynamicMaxTargets() == null) {
            return maximumTargets;
        }
        return Math.min(maximumTargets, Math.max(0, amountEvaluationService.evaluate(gameData,
                target.getDynamicMaxTargets(), new AmountContext(controllerId, null, null, xValue, 0,
                        false, null, repeatedAdditionalCosts == null ? List.of() : repeatedAdditionalCosts, card))));
    }

    private int minimumGraveyardTargets(Card card, CardEffect effect) {
        int targetIndex = card.getEffectTargetIndex(effect);
        if (targetIndex >= 0 && targetIndex < card.getTargetGroups().size()) {
            TargetGroup target = card.getTargetGroups().get(targetIndex);
            return target.getMinTargets();
        }
        if (effect instanceof ReturnCardFromGraveyardEffect returnEffect) {
            return returnEffect.upTo() ? 0 : 1;
        }
        GraveyardTargetingSupport.Target graveyardTarget =
                graveyardTargetingSupport.findTarget(List.of(effect));
        return graveyardTarget == null ? 1 : graveyardTarget.minTargets();
    }

    private static boolean isMixedPermanentAndSpellTarget(CardEffect effect) {
        TargetSpec targetSpec = effect.targetSpec();
        return targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                && targetSpec.admits(TargetPredicate.Kind.SPELL);
    }

    /**
     * Each triggered ability a permanent entering caused is taxed like its own enters ability by abilities such as
     * Strict Proctor's ("counter it unless its controller pays").
     */
    public void queueTriggeredAbilityCounters(GameData gameData, StackEntry triggeredAbility) {
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : gameQueryService.getActiveStaticEffects(gameData, permanent)) {
                    if (!(effect instanceof TriggeredAbilityCounterEffect counterEffect)) {
                        continue;
                    }
                    StackEntry counterTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            permanent.getCard(),
                            controllerId,
                            permanent.getCard().getName() + "'s ability",
                            List.of(new CounterUnlessPaysEffect(counterEffect.counterCost())),
                            triggeredAbility.getTargetableId(),
                            Zone.STACK,
                            permanent.getId());
                    gameData.stack.add(counterTrigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                }
            }
        });
    }

    private List<UUID> targetsForActiveEtbGroups(Card card, List<CardEffect> effects, List<UUID> targetIds) {
        if (targetIds == null || targetIds.isEmpty() || card.getTargetGroups().isEmpty()) {
            return targetIds == null ? List.of() : targetIds;
        }
        List<UUID> activeTargets = new ArrayList<>();
        int consumed = 0;
        for (TargetGroup group : card.getTargetGroups()) {
            if (card.isAura() && group.getIndex() == 0) {
                continue;
            }
            int size = Math.min(group.getMaxTargets(), targetIds.size() - consumed);
            if (size <= 0) {
                break;
            }
            boolean active = !card.bindsEffectToTargetGroup(group.getIndex())
                    || effects.stream().anyMatch(effect -> card.getEffectTargetIndex(effect) == group.getIndex());
            if (active) {
                activeTargets.addAll(targetIds.subList(consumed, consumed + size));
            }
            consumed += size;
        }
        return List.copyOf(activeTargets);
    }

    private boolean hasUnselectedDynamicEtbTargetGroup(Card card, List<CardEffect> effects,
                                                         UUID targetId, List<UUID> targetIds) {
        if (targetId == null || targetIds == null || !targetIds.isEmpty()) {
            return false;
        }
        for (TargetGroup group : card.getTargetGroups()) {
            if (group.getDynamicMaxTargets() == null) {
                continue;
            }
            boolean hasActiveTargetEffect = effects.stream()
                    .filter(effect -> card.getEffectTargetIndex(effect) == group.getIndex())
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                            || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            if (hasActiveTargetEffect) {
                return true;
            }
        }
        return false;
    }
    private Permanent findEnteringPermanent(GameData gameData, Card card) {
        return gameData.playerBattlefields.values().stream().flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getId().equals(card.getId())
                        || permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst().orElse(null);
    }

}
