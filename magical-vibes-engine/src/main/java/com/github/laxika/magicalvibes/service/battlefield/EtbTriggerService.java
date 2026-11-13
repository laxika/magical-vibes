package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.model.effect.DraftProteanWarEngineSpellbookEffect;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.service.trigger.MiscAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.MiscWatch;
import com.github.laxika.magicalvibes.service.effect.entryfx.FlipCoinOnEnterEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DraftCardFromSpellbookToExileEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DraftProteanWarEngineSpellbookEffectHandler;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseIndependentModesOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeChoiceOnEnterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectContext;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectResolver;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.RulesEventPipeline;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class EtbTriggerService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final EtbEffectResolver etbEffectResolver;
    private LegendRuleService legendRuleService;
    private DraftProteanWarEngineSpellbookEffectHandler proteanWarEngineSpellbookDraft;
    private DraftCardFromSpellbookToExileEffectHandler spellbookDraft;
    private FlipCoinOnEnterEffectHandler flipCoinOnEnterEffectHandler;
    private RulesEventPipeline rulesEventPipeline;

    public EtbTriggerService(GameQueryService gameQueryService,
                             GameLogService gameLogService,
                             PlayerInputService playerInputService,
                             @Lazy TriggerCollectionService triggerCollectionService,
                             EtbEffectResolver etbEffectResolver) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.etbEffectResolver = etbEffectResolver;
    }

    @Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;

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

        int stackSizeBeforeEntersTriggers = gameData.stack.size();
        collectEntersTriggers(gameData, controllerId, card, true);
        taxTriggeredAbilitiesTheEntryCaused(gameData, stackSizeBeforeEntersTriggers);
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

        int stackSizeBeforeEntryAbilities = gameData.stack.size();
        if (!gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            EtbEffectContext entryFacts = new EtbEffectContext(gameData, card, controllerId, wasCastFromHand, etbMode,
                    kicked, enteringPermanent != null && enteringPermanent.isEvoked(),
                    enteringPermanent != null && enteringPermanent.isProwl(),
                    enteringPermanent != null && enteringPermanent.isMadness(),
                    enteringPermanent != null && enteringPermanent.isAlternateCost(), enteringPermanent,
                    repeatedAdditionalCosts, targetId, xValue);
            putOwnEntryAbilities(gameData, controllerId, card, enteringPermanent, printedAbilitiesRemoved,
                    entryFacts, convokeCreatureIds);
            // State-based actions come before a triggered ability's choices are made, so a legend rule violation the
            // entering permanent causes is settled first; the auto-pass that follows the legend rule choice begins the
            // held-back choices.
            if (legendRuleService == null || !legendRuleService.hasLegendRuleViolation(gameData, controllerId)) {
                triggerCollectionService.beginStackDoorChoices(gameData);
            }
        }

        collectEntersTriggers(gameData, controllerId, card, false);
        taxTriggeredAbilitiesTheEntryCaused(gameData, stackSizeBeforeEntryAbilities);
    }

    /**
     * Puts the entering permanent's own "when this enters" abilities on the stack through the stack door, each one
     * object of its effects (CR 603.3): those a resolved effect or a static ability granted it and its perpetual
     * grants, then the card's printed ones (none once it lost its printed abilities as it entered). Each ability's
     * event condition is judged here with the facts fixed as the permanent entered (CR 603.2); the door judges its
     * intervening "if" with those facts (CR 603.4) and its effects' facts as it triggers
     * ({@code CardEffect.asItTriggersOnEntering}: "if it was kicked", evoke's sacrifice, tribute), checks its "triggers
     * only once each turn", adds the times it triggers again (an entering artifact or creature, Panharmonicon), and
     * chooses its mode and targets as it goes on the stack (CR 603.3c, 603.3d).
     */
    private void putOwnEntryAbilities(GameData gameData, UUID controllerId, Card card, Permanent enteringPermanent,
                                      boolean printedAbilitiesRemoved, EtbEffectContext entryFacts,
                                      List<UUID> convokeCreatureIds) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        if (enteringPermanent != null) {
            abilities.addAll(enteringPermanent.getGrantedOwnEntryAbilities());
        }
        if (!printedAbilitiesRemoved) {
            // A perpetual "when this enters" grant is an ability of its own.
            for (CardEffect effect : gameData.perpetualEnterEffectsByCardId.getOrDefault(card.getId(), List.of())) {
                abilities.add(new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), effect));
            }
        }
        if (enteringPermanent != null && gameData.playerBattlefields.getOrDefault(controllerId, List.of())
                .contains(enteringPermanent)) {
            abilities.addAll(triggerCollectionService.grantedOwnEntryAbilities(gameData, enteringPermanent));
        }
        if (!printedAbilitiesRemoved) {
            abilities.addAll(card.getOwnEntryAbilities());
        }
        ConditionContext ifContext = etbEffectResolver.entryContext(entryFacts);
        TriggerContext.PermanentEnters event = new TriggerContext.PermanentEnters(card, controllerId, null, 1,
                enteringPermanent == null ? null : enteringPermanent.getId());
        TriggeredAbilityRequest.TriggerRecord record = ownEntryRecord(gameData, card, enteringPermanent, entryFacts,
                convokeCreatureIds);
        for (TriggeredAbility ability : abilities) {
            Condition eventCondition = ability.options().eventCondition();
            if (eventCondition != null && !etbEffectResolver.ownEntryConditionMet(entryFacts, eventCondition)) {
                continue;
            }
            if (triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card,
                    enteringPermanent, controllerId, card.getName() + "'s ETB ability", ifContext, record)
                    .withEvent(event))) {
                gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability triggers."));
            }
        }
    }

    /**
     * The record of the entering permanent's own "when this enters" abilities, whatever their effects are: the paid X
     * and the costs paid as it was cast (the repeated additional costs, convoked creatures, kicker, spectacle, collected
     * evidence, waterbending, a revealed card, a Dragon controlled, a gift promised), the permanent as it is as the
     * ability triggers and what it is attached to, and for an Aura what it enchants, which an effect of its enchant
     * group acts on without targeting it (CR 303.4a).
     */
    private TriggeredAbilityRequest.TriggerRecord ownEntryRecord(GameData gameData, Card card,
                                                                 Permanent enteringPermanent,
                                                                 EtbEffectContext entryFacts,
                                                                 List<UUID> convokeCreatureIds) {
        return entry -> {
            entry.setXValue(entryFacts.xValue());
            if (card.isAura() && entryFacts.targetId() != null) {
                entry.setTargetId(entryFacts.targetId());
                entry.setNonTargeting(true);
            }
            List<String> repeatedAdditionalCosts = entryFacts.repeatedAdditionalCosts();
            if (repeatedAdditionalCosts != null && !repeatedAdditionalCosts.isEmpty()) {
                entry.setRepeatedAdditionalCosts(List.copyOf(repeatedAdditionalCosts));
            }
            entry.setConvokeCreatureIds(convokeCreatureIds);
            if (enteringPermanent == null) {
                return;
            }
            // Dynamic amounts such as "gain 3 life for each time it was kicked" read the kicked state from the entry.
            if (enteringPermanent.isKicked()) {
                entry.setKicked(true);
            }
            if (enteringPermanent.isEvoked() && card.hasSeveralOwnEntryAbilities()) {
                entry.setTriggeringCardId(card.getId());
            }
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, enteringPermanent.getId());
            if (sourcePermanent != null) {
                entry.setSourcePermanentSnapshot(new Permanent(sourcePermanent));
                entry.setSacrificedToughness(sourcePermanent.getAlternateCostSacrificedToughness());
                snapshotAttachedPermanent(gameData, entry, sourcePermanent);
            }
            entry.setSpectacle(enteringPermanent.isSpectacle());
            entry.setCollectEvidenceCostPaid(enteringPermanent.isCollectEvidenceCostPaid());
            entry.setWaterbendCostPaid(enteringPermanent.isWaterbendCostPaid());
            entry.setRevealCardFromHandCostPaid(enteringPermanent.isRevealCardFromHandCostPaid());
            entry.setControlledDragonAsCast(enteringPermanent.isControlledDragonAsCast());
            entry.setGiftPromised(enteringPermanent.isGiftPromised());
        };
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

    /**
     * Taxes each triggered ability the entering permanent caused, its own and the others' (constellation, landfall,
     * ...): the abilities that trigger when a permanent entering causes a triggered ability to trigger
     * ({@link #queueTriggeredAbilityCounters}), for each that reached the stack since {@code stackSizeBefore}.
     */
    private void taxTriggeredAbilitiesTheEntryCaused(GameData gameData, int stackSizeBefore) {
        List<StackEntry> causedTriggers = new ArrayList<>(gameData.stack.subList(
                Math.min(stackSizeBefore, gameData.stack.size()), gameData.stack.size()));
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
    private void collectEntersTriggers(GameData gameData, UUID controllerId, Card card, boolean faceDown) {
        Permanent entering = findEnteringPermanent(gameData, card);
        if (entering != null && rulesEventPipeline != null) {
            rulesEventPipeline.triggerEnteringObservers(gameData, entering, controllerId);
        }
        triggerCollectionService.checkAllyPermanentEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkAllyCreatureEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkEmblemEntersTriggers(gameData, controllerId, card);
        Permanent enteringPermanent = findEnteringPermanent(gameData, card);
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
     * Collects the abilities that trigger when a permanent entering causes a triggered ability to trigger ("whenever a
     * permanent entering causes a triggered ability to trigger, counter that ability unless its controller pays {2}",
     * Strict Proctor), once {@code triggeredAbility} is on the stack: each goes on the stack as written, after the
     * ability that caused it, recording that ability as its triggering spell, its controller as the triggering player
     * and its stack object as the non-targeting referent "that ability", whatever the effects are. A permanent that
     * lost its abilities or is face down doesn't trigger.
     */
    public void queueTriggeredAbilityCounters(GameData gameData, StackEntry triggeredAbility) {
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent permanent : List.copyOf(battlefield)) {
                if (permanent.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, permanent)) {
                    continue;
                }
                // Each ability is one object of all its effects (CR 603.3), through the stack door.
                for (TriggeredAbility ability : MiscAbilityReader.printed(permanent.getCard(),
                        MiscWatch.ON_PERMANENT_ENTERING_CAUSES_ABILITY_TO_TRIGGER)) {
                    boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                            permanent.getCard(), permanent, controllerId, permanent.getCard().getName() + "'s ability",
                            ConditionContext.forPermanent(permanent, controllerId), entry -> {
                                entry.setNonTargeting(true);
                                entry.setTargetId(triggeredAbility.getTargetableId());
                                entry.setTargetZone(Zone.STACK);
                                entry.setTriggeringCardId(triggeredAbility.getTargetableId());
                                entry.setTriggeringSpell(triggeredAbility);
                                entry.setTriggeringPlayerId(triggeredAbility.getControllerId());
                            }));
                    if (triggered) {
                        gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                    }
                }
            }
        });
    }

    private Permanent findEnteringPermanent(GameData gameData, Card card) {
        return gameData.playerBattlefields.values().stream().flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getId().equals(card.getId())
                        || permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst().orElse(null);
    }

}
