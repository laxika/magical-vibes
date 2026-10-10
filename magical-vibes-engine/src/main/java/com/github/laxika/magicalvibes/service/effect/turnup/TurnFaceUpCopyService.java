package com.github.laxika.magicalvibes.service.effect.turnup;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessFormChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TurnFaceUpCopyEffect;
import com.github.laxika.magicalvibes.model.effect.TurnFaceUpAttachAuraEffect;
import com.github.laxika.magicalvibes.model.effect.TurnFaceUpReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurnFaceUpCopyService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final TurnProgressionService turnProgressionService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final AuraAttachmentService auraAttachmentService;

    public void turnFaceUpWithoutCost(GameData gameData, Permanent permanent) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        if (controllerId == null) {
            return;
        }

        Card physicalCard = permanent.getOriginalCard();
        if (physicalCard.hasType(CardType.INSTANT) || physicalCard.hasType(CardType.SORCERY)) {
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " reveals ", physicalCard,
                    " and leaves it face down."));
            return;
        }
        permanent.turnFaceUp();
        List<TurnFaceUpReplacementEffect> replacements = permanent.getCard()
                .getEffects(EffectSlot.ON_TURNED_FACE_UP).stream()
                .filter(TurnFaceUpReplacementEffect.class::isInstance)
                .map(TurnFaceUpReplacementEffect.class::cast)
                .toList();
        for (TurnFaceUpReplacementEffect replacement : replacements) {
            if (!replacement.appliesWithoutPayingCost()) {
                continue;
            }
            int counterCount = amountEvaluationService.evaluate(gameData, replacement.counterAmount(),
                    AmountContext.forEnteringPermanent(controllerId, permanent, 0));
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, null, permanent, replacement.counterType(), counterCount);
        }
        if (prepareChoice(gameData, permanent, controllerId)) {
            return;
        }
        finishTurnFaceUp(gameData, controllerId, permanent.getId(), false);
    }

    public boolean prepareChoice(GameData gameData, Permanent source, UUID controllerId) {
        if (source.getCard().getEffects(EffectSlot.ON_TURNED_FACE_UP).stream()
                .anyMatch(TurnFaceUpAttachAuraEffect.class::isInstance)) {
            List<UUID> hosts = new ArrayList<>();
            gameData.forEachPermanent((ignored, permanent) -> {
                if (!permanent.getId().equals(source.getId())
                        && auraAttachmentService.canEnchant(gameData, source.getCard(), controllerId, permanent)) {
                    hosts.add(permanent.getId());
                }
            });
            if (hosts.isEmpty()) {
                return false;
            }
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.TurnFaceUpCopy(source.getId(), controllerId));
            playerInputService.beginAnyTargetChoice(gameData, controllerId, hosts, List.of(controllerId),
                    "Choose a creature to enchant, or choose yourself not to attach this Aura.");
            return true;
        }
        TurnFaceUpCopyEffect effect = findCopyEffect(source.getCard());
        if (effect == null) {
            return preparePowerToughnessChoice(gameData, source, controllerId);
        }

        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
        List<UUID> validTargets = new ArrayList<>();
        gameData.forEachPermanent((ignored, permanent) -> {
            if (!permanent.getId().equals(source.getId())
                    && predicateEvaluationService.matchesPermanentPredicate(
                    permanent, effect.filter(), filterContext)) {
                validTargets.add(permanent.getId());
            }
        });
        if (validTargets.isEmpty()) {
            return preparePowerToughnessChoice(gameData, source, controllerId);
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TurnFaceUpCopy(source.getId(), controllerId));
        playerInputService.beginAnyTargetChoice(gameData, controllerId, validTargets, List.of(controllerId),
                "Choose another creature to copy, or choose yourself not to copy.");
        return true;
    }

    public void completeChoice(GameData gameData, UUID sourcePermanentId, UUID controllerId, UUID chosenId) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        if (controllerId != null && !controllerId.equals(chosenId)) {
            Permanent target = gameQueryService.findPermanentById(gameData, chosenId);
            if (target != null && source.getCard().getEffects(EffectSlot.ON_TURNED_FACE_UP).stream()
                    .anyMatch(TurnFaceUpAttachAuraEffect.class::isInstance)
                    && auraAttachmentService.canEnchant(gameData, source.getCard(), controllerId, target)) {
                source.setAttachedTo(target.getId());
                source.setTimestamp(gameData.nextTimestamp());
                triggerCollectionService.checkAuraAttachedTriggers(gameData, source, target.getId());
            }
            TurnFaceUpCopyEffect effect = findCopyEffect(source.getCard());
            if (target != null && effect != null && gameQueryService.isCreature(gameData, target)) {
                permanentCopierService.applyCloneCopy(source, target, null, null);
                addSourceCopyException(source, effect);
                gameLogService.append(gameData,
                        GameLog.textCardText(source.getCard().getName() + " becomes a copy of ",
                                target.getCard(), "."));
            }
        }

        if (preparePowerToughnessChoice(gameData, source, controllerId)) {
            return;
        }
        finishTurnFaceUp(gameData, controllerId, sourcePermanentId);
    }

    public void finishTurnFaceUp(GameData gameData, UUID controllerId, UUID sourcePermanentId) {
        finishTurnFaceUp(gameData, controllerId, sourcePermanentId, true);
    }

    private void finishTurnFaceUp(GameData gameData, UUID controllerId, UUID sourcePermanentId,
                                  boolean resolveAutoPass) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " is turned face up."));
        triggerCollectionService.checkGraveyardAllyPermanentTurnsFaceUpTriggers(gameData, controllerId, source);
        triggerCollectionService.checkSelfOrAllyCreatureTurnsFaceUpTriggers(gameData, controllerId, source);
        triggerCollectionService.checkSelfOrAnyPermanentTurnsFaceUpTriggers(gameData, controllerId, source);
        triggerCollectionService.checkSelfOrAllyPermanentTurnsFaceUpTriggers(gameData, controllerId, source);

        List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_TURNED_FACE_UP).stream()
                .filter(effect -> !(effect instanceof ReplacementEffect))
                .filter(effect -> turnFaceUpTriggerConditionIsMet(gameData, source, controllerId, effect))
                .toList();
        if (!effects.isEmpty()) {
            if (effects.size() == 1 && effects.getFirst() instanceof ChooseOneEffect modal) {
                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        source.getCard(), controllerId, modal, source.getId()));
                turnProgressionService.resolveAutoPass(gameData);
                return;
            }
            boolean usesMultiTargetSelection = effects.stream()
                    .anyMatch(source.getCard()::hasEffectTargetIndex)
                    && (source.getCard().getSpellTargets().size() > 1
                    || triggerCollectionService.needsSlotBySlotTargetSelection(source.getCard()));
            if (usesMultiTargetSelection) {
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        source.getCard(), controllerId, effects, source.getId(), List.of(), 0, 0));
            } else {
                boolean targetsSpell = effects.stream()
                        .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.SPELL));
                boolean targetsPlayer = effects.stream()
                        .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                boolean targetsPermanent = effects.stream()
                        .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
                boolean targetsGraveyard = effects.stream()
                        .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
                if (targetsGraveyard) {
                    int minimumGraveyardTargets = effects.stream()
                            .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                            .anyMatch(effect -> !effect.hasOptionalTarget()) ? 1 : 0;
                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                            source.getCard(), controllerId, effects, null, minimumGraveyardTargets, 0));
                    triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
                } else if (targetsSpell) {
                    StackEntryPredicate spellFilter = null;
                    boolean includeAbilities = false;
                    if (source.getCard().getTargetFilter() instanceof StackEntryPredicateTargetFilter filter) {
                        spellFilter = filter.predicate();
                        includeAbilities = TriggerCollectionService.predicateContainsHasTarget(filter.predicate());
                    }
                    gameData.queueInteraction(new PermanentChoiceContext.ETBSpellTargetTrigger(
                            source.getCard(), controllerId, effects, spellFilter, includeAbilities, source.getId()));
                } else if (targetsPlayer || targetsPermanent) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            source.getCard(), controllerId, effects, !targetsPermanent,
                            source.getCard().getTargetFilter(), 0, source.getId()));
                } else {
                    gameData.stack.add(new com.github.laxika.magicalvibes.model.StackEntry(
                            com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY,
                            source.getCard(), controllerId, source.getCard().getName() + "'s ability",
                            effects, source.getId(), List.of()));
                }
            }
        }
        if (resolveAutoPass) {
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void addSourceCopyException(Permanent source, TurnFaceUpCopyEffect effect) {
        Card copiedCard = source.getCard();
        if (!copiedCard.getEffects(EffectSlot.ON_TURNED_FACE_UP).contains(effect)) {
            copiedCard.addEffect(EffectSlot.ON_TURNED_FACE_UP, effect);
        }
        effect.additionalSlotEffects().forEach((slot, effects) -> effects.forEach(additional -> {
            if (!copiedCard.getEffects(slot).contains(additional)) {
                copiedCard.addEffect(slot, additional);
            }
        }));
    }

    private TurnFaceUpCopyEffect findCopyEffect(Card card) {
        return card.getEffects(EffectSlot.ON_TURNED_FACE_UP).stream()
                .filter(TurnFaceUpCopyEffect.class::isInstance)
                .map(TurnFaceUpCopyEffect.class::cast)
                .findFirst()
                .orElse(null);
    }

    private boolean preparePowerToughnessChoice(GameData gameData, Permanent source, UUID controllerId) {
        PowerToughnessFormChoiceEffect effect = source.getCard().getEffects(EffectSlot.ON_TURNED_FACE_UP).stream()
                .filter(PowerToughnessFormChoiceEffect.class::isInstance)
                .map(PowerToughnessFormChoiceEffect.class::cast)
                .findFirst().orElse(null);
        if (effect == null) {
            return false;
        }
        playerInputService.beginPowerToughnessFormChoice(gameData, controllerId, source.getId(),
                effect.forms(), true);
        return true;
    }

    private boolean turnFaceUpTriggerConditionIsMet(GameData gameData, Permanent source,
                                                    UUID controllerId, CardEffect effect) {
        if (!(effect instanceof ConditionalEffect conditional) || !conditional.interveningIf()) {
            return true;
        }
        return conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forPermanent(source, controllerId));
    }
}
