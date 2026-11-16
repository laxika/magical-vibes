package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggerCause;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.ReturnVoyagingCardFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.PutVoyageCounterOnExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.CreatureEntersTriggerWatcher;
import com.github.laxika.magicalvibes.model.BoonTrigger;
import com.github.laxika.magicalvibes.model.Boon;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.model.effect.DraftProteanWarEngineSpellbookEffect;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.rules.EntryCausedTriggerEvent;
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
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectContext;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectResolver;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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
    private final GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final EtbEffectResolver etbEffectResolver;
    private LegendRuleService legendRuleService;
    private DraftProteanWarEngineSpellbookEffectHandler proteanWarEngineSpellbookDraft;
    private DraftCardFromSpellbookToExileEffectHandler spellbookDraft;
    private FlipCoinOnEnterEffectHandler flipCoinOnEnterEffectHandler;
    private RulesEventPipeline rulesEventPipeline;

    public EtbTriggerService(GameQueryService gameQueryService,
                             GameLogService gameLogService,
                             PlayerInputService playerInputService,
                             @Lazy GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport,
                             @Lazy TriggeredAbilityQueueService triggeredAbilityQueueService,
                             EtbEffectResolver etbEffectResolver) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.grantedTriggeredAbilitySupport = grantedTriggeredAbilitySupport;
        this.triggeredAbilityQueueService = triggeredAbilityQueueService;
        this.etbEffectResolver = etbEffectResolver;
    }

    @Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;

    @Autowired
    @Lazy
    private PredicateEvaluationService predicateEvaluationService;

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

    /** The boons the tokens {@code permanentIds} entering together under {@code controllerId}'s control use. */
    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId,
                                              List<UUID> permanentIds) {
        collectAllyBoonTriggers(gameData, controllerId, permanentIds, true);
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
                triggeredAbilityQueueService.beginStackDoorChoices(gameData);
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
            abilities.addAll(grantedTriggeredAbilitySupport.grantedOwnEntryAbilities(gameData, enteringPermanent));
        }
        if (!printedAbilitiesRemoved) {
            abilities.addAll(card.getOwnEntryAbilities());
        }
        ConditionContext ifContext = etbEffectResolver.entryContext(entryFacts);
        TriggerCause.Entered event = new TriggerCause.Entered(card, controllerId,
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
                    .withCause(event))) {
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
     * ...): each that reached the stack since {@code stackSizeBefore} is an {@link EntryCausedTriggerEvent} of its
     * own, which "whenever a permanent entering causes a triggered ability to trigger" abilities (Strict Proctor)
     * trigger on, after the ability that caused them.
     */
    private void taxTriggeredAbilitiesTheEntryCaused(GameData gameData, int stackSizeBefore) {
        List<StackEntry> causedTriggers = new ArrayList<>(gameData.stack.subList(
                Math.min(stackSizeBefore, gameData.stack.size()), gameData.stack.size()));
        for (StackEntry causedTrigger : causedTriggers) {
            if (causedTrigger.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
                gameData.rulesEvents.occur(new EntryCausedTriggerEvent(causedTrigger));
            }
        }
    }

    /**
     * What the entering permanent triggers: the abilities that see it enter, matched by the trigger pipeline, face-up
     * planes, emblems, schemes and delayed abilities among them, then the watchers, boons and exiled voyages kept
     * outside it.
     */
    private void collectEntersTriggers(GameData gameData, UUID controllerId, Card card, boolean faceDown) {
        Permanent entering = findEnteringPermanent(gameData, card);
        if (entering != null && rulesEventPipeline != null) {
            rulesEventPipeline.triggerEnteringObservers(gameData, entering, controllerId);
        }
        checkAllyPermanentEntersTriggers(gameData, controllerId, card);
        checkAllyCreatureEntersTriggers(gameData, controllerId, card);
        Permanent enteringPermanent = findEnteringPermanent(gameData, card);
        if (!faceDown && (enteringPermanent == null ? card.hasType(CardType.LAND)
                : gameQueryService.isLand(gameData, enteringPermanent))) {
            collectExiledVoyageTriggers(gameData, controllerId);
        }
    }

    /**
     * What a creature entering under {@code controllerId}'s control triggers besides the abilities the trigger pipeline
     * matches: the "whenever a creature you control enters this turn" watchers a resolved spell left (First Day of
     * Class), each with the creature as its non-targeting object, and the boons it uses.
     */
    private void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature) {
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        boolean creature = enteringPermanent == null ? enteringCreature.hasType(CardType.CREATURE)
                : gameQueryService.isCreature(gameData, enteringPermanent);
        if (!creature || enteringPermanent == null) return;

        for (CreatureEntersTriggerWatcher watcher : List.copyOf(gameData.allyCreatureEntersTriggerWatchers)) {
            if (!watcher.controllerId().equals(controllerId)) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(), watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(enteringPermanent.getId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }

        if (!gameData.deferCreatureEntersBoons) {
            collectAllyBoonTriggers(gameData, controllerId, List.of(enteringPermanent.getId()), true);
        }
    }

    /** The boons a noncreature permanent entering under {@code controllerId}'s control uses. */
    private void checkAllyPermanentEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCard);
        if (enteringPermanent != null && !gameQueryService.isCreature(gameData, enteringPermanent)) {
            collectAllyBoonTriggers(gameData, controllerId, List.of(enteringPermanent.getId()), false);
        }
    }

    /**
     * Puts the controller's boons that fire when permanents enter on the stack. {@link BoonTrigger#CREATURE_ENTERS}
     * boons watch creatures only; {@link BoonTrigger#PERMANENT_ENTERS} boons watch every permanent, so the creature
     * entries ({@code creatureEntries}) and the noncreature entries are offered separately.
     */
    private void collectAllyBoonTriggers(GameData gameData, UUID controllerId,
                                         List<UUID> enteringPermanentIds, boolean creatureEntries) {
        if (enteringPermanentIds == null || enteringPermanentIds.isEmpty()) return;

        for (Boon boon : List.copyOf(gameData.boons)) {
            if ((boon.trigger() != BoonTrigger.CREATURE_ENTERS
                    && boon.trigger() != BoonTrigger.ONE_OR_MORE_CREATURES_ENTER
                    && boon.trigger() != BoonTrigger.PERMANENT_ENTERS)
                    || !boon.controllerId().equals(controllerId)) continue;

            Permanent enteringPermanent = null;
            CardEffect resolved = null;
            List<UUID> matchingEnteringIds = new ArrayList<>();
            for (UUID enteringPermanentId : enteringPermanentIds) {
                Permanent candidate = gameQueryService.findPermanentById(gameData, enteringPermanentId);
                if (candidate == null) continue;
                boolean candidateIsCreature = gameQueryService.isCreature(gameData, candidate);
                if ((boon.trigger() == BoonTrigger.CREATURE_ENTERS
                        || boon.trigger() == BoonTrigger.ONE_OR_MORE_CREATURES_ENTER)
                        ? !candidateIsCreature
                        : candidateIsCreature != creatureEntries) {
                    continue;
                }
                // "Whenever one or more Merfolk enter under your control": the boon's filter on the entering card.
                if (boon.filter() != null && !predicateEvaluationService.matchesCardPredicate(candidate.getCard(),
                        boon.filter(), boon.sourceCard().getId(), gameData, controllerId)) {
                    continue;
                }
                if (enteringPermanent == null) {
                    enteringPermanent = candidate;
                    resolved = boon.effect();
                }
                matchingEnteringIds.add(candidate.getId());
            }
            if (enteringPermanent == null || resolved == null) continue;

            if (matchingEnteringIds.size() > 1 && boon.targetFilter() == null
                    && boon.trigger() != BoonTrigger.ONE_OR_MORE_CREATURES_ENTER
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                // "Whenever a creature you control enters, [do something to] it": every creature that entered
                // together triggers the boon separately and consumes its own use.
                gameData.boons.remove(boon);
                int usesLeft = boon.remainingUses();
                for (UUID matchingId : matchingEnteringIds) {
                    if (usesLeft <= 0) break;
                    Permanent matching = gameQueryService.findPermanentById(gameData, matchingId);
                    if (matching == null) continue;
                    usesLeft--;
                    StackEntry perCreatureEntry = TriggeredAbilityStackService.madeAbility(boon.sourceCard(),
                            boon.controllerId(), boon.sourceCard().getName() + "'s boon", List.of(resolved));
                    perCreatureEntry.setTargetId(matching.getId());
                    perCreatureEntry.setTriggeringPermanentId(matching.getId());
                    perCreatureEntry.setTriggeringCardId(matching.getCard().getId());
                    perCreatureEntry.setNonTargeting(true);
                    triggeredAbilityStackService.putDelayed(gameData, perCreatureEntry);
                    gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
                }
                if (usesLeft > 0) {
                    gameData.boons.add(boon.withRemainingUses(usesLeft));
                }
                continue;
            }

            gameData.boons.remove(boon);
            if (boon.remainingUses() > 1) {
                gameData.boons.add(boon.withRemainingUses(boon.remainingUses() - 1));
            }

            Permanent sourcePermanent = findPermanentByCard(gameData, boon.sourceCard());
            // "attached to one of them": when several creatures entered together the controller picks which.
            TargetFilter boonTargetFilter = boon.targetFilter();
            if (matchingEnteringIds.size() > 1
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    && (boonTargetFilter == null || boonTargetFilter instanceof PermanentPredicateTargetFilter)) {
                PermanentPredicate entered = new PermanentAnyOfPredicate(matchingEnteringIds.stream()
                        .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new).toList());
                boonTargetFilter = new PermanentPredicateTargetFilter(
                        boonTargetFilter instanceof PermanentPredicateTargetFilter existing
                                ? new PermanentAllOfPredicate(List.of(existing.predicate(), entered)) : entered,
                        "Target must be one of the creatures that entered");
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(boon.sourceCard(), boon.controllerId(),
                    boon.sourceCard().getName() + "'s boon", List.of(resolved), enteringPermanent.getId(),
                    sourcePermanent == null ? null : sourcePermanent.getId());
            entry.setTriggeringPermanentId(enteringPermanent.getId());
            entry.setTriggeringPermanentControllerId(controllerId);
            entry.setTriggeringCardId(enteringPermanent.getCard().getId());
            entry.rememberLastKnownPermanentCard(enteringPermanent.getId(), enteringPermanent.getCard());
            entry.setNonTargeting(true);
            if (boonTargetFilter != null
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                // "Attached to one of them": its target, restricted by the boon's filter, is chosen as it goes on the
                // stack (CR 603.3d).
                entry.setTargetFilter(boonTargetFilter);
                triggeredAbilityStackService.putMade(gameData, entry);
            } else {
                triggeredAbilityStackService.putDelayed(gameData, entry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
        }
    }

    /**
     * The exiled voyages that see a land enter under {@code landControllerId}'s control: the ability Cosima, God of the
     * Voyage gained as it was exiled ("whenever a land you control enters, if Cosima is exiled, you may put a voyage
     * counter on it. If you don't, return Cosima ..."), of the card in exile.
     */
    private void collectExiledVoyageTriggers(GameData gameData, UUID landControllerId) {
        for (var exiled : new ArrayList<>(gameData.exiledCards)) {
            UUID cardId = exiled.card().getId();
            UUID controllerId = gameData.exiledVoyageControllerIds.get(cardId);
            if (!gameData.exiledVoyageCounters.containsKey(cardId)) {
                continue;
            }
            if (gameData.findExiledCard(cardId) == null || controllerId == null) {
                gameData.exiledVoyageCounters.remove(cardId);
                gameData.exiledVoyageControllerIds.remove(cardId);
                continue;
            }
            if (!landControllerId.equals(controllerId)) {
                continue;
            }

            StackEntry voyage = TriggeredAbilityStackService.madeAbility(exiled.card(), controllerId,
                    exiled.card().getName() + "'s ability", List.of(new MayEffect(
                            new PutVoyageCounterOnExiledCardEffect(),
                            "Put a voyage counter on Cosima?",
                            new ReturnVoyagingCardFromExileEffect())));
            voyage.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, voyage);
            gameLogService.append(gameData, GameLog.abilityTriggers(exiled.card()));
        }
    }

    /** The permanent whose card is {@code card} itself, or {@code null}. */
    private static Permanent findPermanentByCard(GameData gameData, Card card) {
        Permanent[] found = new Permanent[1];
        gameData.forEachPermanent((playerId, permanent) -> {
            if (found[0] == null && permanent.getCard() == card) {
                found[0] = permanent;
            }
        });
        return found[0];
    }

    private Permanent findEnteringPermanent(GameData gameData, Card card) {
        return gameData.playerBattlefields.values().stream().flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getId().equals(card.getId())
                        || permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst().orElse(null);
    }

}
