package com.github.laxika.magicalvibes.service.effect.turnup;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessFormChoiceEffect;
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
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BattlefieldAndGraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.rules.PermanentChange;
import com.github.laxika.magicalvibes.model.rules.PermanentChangeEvent;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurnFaceUpCopyService {

    @Autowired
    @Lazy
    private GraveyardTargetingService graveyardTargetingService;

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
        applyTurnFaceUpReplacements(gameData, controllerId, permanent, false);
        if (prepareChoice(gameData, permanent, controllerId)) {
            return;
        }
        finishTurnFaceUp(gameData, controllerId, permanent.getId(), false);
    }

    public boolean prepareChoice(GameData gameData, Permanent source, UUID controllerId) {
        if (source.getCard().getStaticEffects().stream()
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
            if (target != null && source.getCard().getStaticEffects().stream()
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
                // The copied creature's "as this is turned face up" replacement applies to the
                // permanent still being turned face up (Hooded Hydra copied by Vesuvan Shapeshifter).
                applyTurnFaceUpReplacements(gameData, controllerId, source,
                        gameData.permanentsTurnedFaceUpThisTurn.contains(source.getId()));
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
        turnedFaceUp(gameData, source, controllerId, 0);
        if (resolveAutoPass) {
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    /**
     * {@code permanent} was turned face up, its "as this is turned face up" replacements applied (CR 614.1e): a rules
     * event, which the trigger pipeline matches to "when this is turned face up" and "whenever a permanent you control
     * is turned face up" abilities.
     *
     * @param xValue the X paid to turn it face up, or 0
     */
    public void turnedFaceUp(GameData gameData, Permanent permanent, UUID controllerId, int xValue) {
        gameData.rulesEvents.emit(new PermanentChangeEvent(PermanentChange.TURNED_FACE_UP, new Permanent(permanent),
                controllerId, xValue));
    }

    /**
     * Puts {@code permanent}'s "when this is turned face up" ability, of {@code effects}, on the stack as the trigger
     * pipeline matched it, once more for each effect that makes it trigger an additional time: a modal ability
     * chooses its mode and a targeted one waits for its targets (CR 603.3d). The pipeline judged the ability's
     * intervening "if" as it triggered (CR 603.4); its effects are wrapped in it, so it is checked again as they
     * resolve.
     *
     * @param xValue the X paid to turn it face up, or 0
     * @return whether it triggered
     */
    public boolean putTurnedFaceUpAbilityOnStack(GameData gameData, Permanent permanent, UUID controllerId,
                                                 List<CardEffect> effects, int xValue) {
        if (effects.isEmpty()) {
            return false;
        }
        Card card = permanent.getCard();
        int triggerCopies = 1 + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                gameData, controllerId, permanent, false,
                new TriggerContext.PermanentTurnsFaceUp(permanent, controllerId));
        if (effects.size() == 1 && effects.getFirst() instanceof ChooseOneEffect modal) {
            for (int copy = 0; copy < triggerCopies; copy++) {
                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        card, controllerId, modal, permanent.getId()));
            }
            return true;
        }
        BattlefieldAndGraveyardCardChoosingEffect mixedZoneChoice = effects.stream()
                .filter(BattlefieldAndGraveyardCardChoosingEffect.class::isInstance)
                .map(BattlefieldAndGraveyardCardChoosingEffect.class::cast)
                .findFirst().orElse(null);
        if (mixedZoneChoice != null) {
            graveyardTargetingService.handleBattlefieldAndGraveyardExileETBTargeting(
                    gameData, controllerId, card, effects, permanent.getId(), mixedZoneChoice, xValue);
            return true;
        }
        boolean targetsSpell = effects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.SPELL));
        boolean targetsGraveyard = effects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        boolean targetsPlayer = effects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        boolean targetsPermanent = effects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        if (targetsGraveyard) {
            int minimumGraveyardTargets = effects.stream()
                    .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                    .anyMatch(effect -> !effect.hasOptionalTarget()) ? 1 : 0;
            for (int copy = 0; copy < triggerCopies; copy++) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        card, controllerId, effects, null, minimumGraveyardTargets, xValue));
            }
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
        } else if (targetsSpell) {
            StackEntryPredicate spellFilter = null;
            boolean includeAbilities = false;
            if (card.getTargetFilter() instanceof StackEntryPredicateTargetFilter filter) {
                spellFilter = filter.predicate();
                includeAbilities = TriggerCollectionService.predicateContainsHasTarget(filter.predicate());
            }
            for (int copy = 0; copy < triggerCopies; copy++) {
                gameData.queueInteraction(new PermanentChoiceContext.ETBSpellTargetTrigger(
                        card, controllerId, effects, spellFilter, includeAbilities, permanent.getId()));
            }
        } else if (targetsPlayer || targetsPermanent || effects.stream().anyMatch(card::hasEffectTargetIndex)) {
            boolean multiTarget = card.getTargetGroups().size() > 1
                    || card.getTargetGroups().stream()
                    .anyMatch(group -> group.getMaxTargets() > 1 || group.getMinTargets() == 0
                            || group.getDynamicMinTargets() != null)
                    || effects.stream().anyMatch(card::hasEffectTargetIndex)
                    && triggerCollectionService.needsSlotBySlotTargetSelection(card);
            for (int copy = 0; copy < triggerCopies; copy++) {
                if (multiTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                            card, controllerId, effects, permanent.getId(), List.of(), 0, 0));
                } else {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            card, controllerId, effects, !targetsPermanent, card.getTargetFilter(), 0,
                            permanent.getId()));
                }
            }
        } else {
            for (int copy = 0; copy < triggerCopies; copy++) {
                gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                        card.getName() + "'s ability", new ArrayList<>(effects), xValue, permanent.getId()));
            }
        }
        return true;
    }

    private void applyTurnFaceUpReplacements(GameData gameData, UUID controllerId, Permanent permanent,
                                             boolean paidTurnFaceUpCost) {
        List<TurnFaceUpReplacementEffect> replacements = permanent.getCard()
                .getStaticEffects().stream()
                .filter(TurnFaceUpReplacementEffect.class::isInstance)
                .map(TurnFaceUpReplacementEffect.class::cast)
                .toList();
        for (TurnFaceUpReplacementEffect replacement : replacements) {
            if (!paidTurnFaceUpCost && !replacement.appliesWithoutPayingCost()) {
                continue;
            }
            int counterCount = amountEvaluationService.evaluate(gameData, replacement.counterAmount(),
                    AmountContext.forEnteringPermanent(controllerId, permanent, 0));
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, null, permanent, replacement.counterType(), counterCount);
        }
    }

    private void addSourceCopyException(Permanent source, TurnFaceUpCopyEffect effect) {
        Card copiedCard = source.getCard();
        if (!copiedCard.getStaticEffects().contains(effect)) {
            copiedCard.addStaticEffect(effect);
        }
        for (Ability ability : effect.additionalAbilities()) {
            if (ability instanceof StaticAbility statik) {
                statik.effects().stream()
                        .filter(additional -> !copiedCard.getStaticEffects().contains(additional))
                        .forEach(additional -> copiedCard.addStaticEffect(statik.zones(), additional));
                continue;
            }
            if (ability instanceof TriggeredAbility triggered && LegacySlots.slotOf(ability) == null) {
                if (!copiedCard.getTriggeredAbilities().contains(triggered)) {
                    copiedCard.addTriggeredAbility(triggered);
                }
                continue;
            }
            EffectSlot slot = LegacySlots.slotOf(ability);
            LegacySlots.effectsOf(ability).stream()
                    .filter(additional -> !copiedCard.getEffects(slot).contains(additional))
                    .forEach(additional -> copiedCard.addEffect(slot, additional));
        }
    }

    private TurnFaceUpCopyEffect findCopyEffect(Card card) {
        return card.getStaticEffects().stream()
                .filter(TurnFaceUpCopyEffect.class::isInstance)
                .map(TurnFaceUpCopyEffect.class::cast)
                .findFirst()
                .orElse(null);
    }

    private boolean preparePowerToughnessChoice(GameData gameData, Permanent source, UUID controllerId) {
        PowerToughnessFormChoiceEffect effect = source.getCard().getStaticEffects().stream()
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
}
