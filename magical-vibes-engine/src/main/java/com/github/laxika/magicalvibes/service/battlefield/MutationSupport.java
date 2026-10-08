package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves targeted mutate casts while preserving the existing permanent and its physical cards. */
@Service
@RequiredArgsConstructor
public class MutationSupport {

    private final GameQueryService gameQueryService;
    private final TargetLegalityService targetLegalityService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    /** Validates the mutate target before any casting costs are paid. */
    public void validateCastTarget(GameData gameData, Card spell, UUID controllerId, UUID targetId) {
        UUID ownerId = spell.getOwnerId() == null ? controllerId : spell.getOwnerId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (!isOwnedNonHumanCreature(gameData, target, ownerId)) {
            throw new IllegalStateException("Mutate requires a non-Human creature with the spell's owner");
        }
        targetLegalityService.validateSpellTargeting(gameData, spell, targetId, null, controllerId, true);
    }

    /** Returns false when the spell instead resolves as an ordinary creature spell. */
    public boolean beginResolvingMutation(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (!isOwnedNonHumanCreature(gameData, target, entry.getOwnerId())) {
            return false;
        }
        try {
            targetLegalityService.validateSpellTargeting(gameData, entry.getCard(), entry.getTargetId(),
                    null, entry.getControllerId(), true, entry.getXValue());
        } catch (IllegalStateException | IllegalArgumentException illegalTarget) {
            return false;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), target.getId(), null, new ChoiceContext.MutateOrderChoice(entry),
                List.of("TOP", "BOTTOM"), "Put the mutating creature on top or on bottom?"));
        return true;
    }

    /** Completes the resolving spell's merge without a battlefield-entry event. */
    public void merge(GameData gameData, StackEntry entry, boolean onTop) {
        Permanent host = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (host == null) {
            throw new IllegalStateException("The creature being mutated is no longer on the battlefield");
        }
        Card component = entry.getPhysicalCard();
        if (entry.isCopy()) {
            component = entry.getCard().createRuntimeCopy();
            component.setToken(true);
            component.freeze();
        }
        if (!host.isMergedByMutation()) {
            Card originalComponent = host.getOriginalCard();
            if (host.getCard().isToken() && !originalComponent.isToken()) {
                originalComponent = originalComponent.createRuntimeCopy();
                originalComponent.setToken(true);
                originalComponent.freeze();
            }
            host.getMutatedComponentCards().add(originalComponent);
        }
        if (onTop) {
            host.getMutatedComponentCards().addFirst(component);
        } else {
            host.getMutatedComponentCards().add(component);
        }

        Card merged = mergeCharacteristics(host.getCard(), entry.getCard(), onTop);
        merged = withTokenStatus(merged, host.getMutatedComponentCards().getFirst().isToken());
        host.setCard(merged);
        if (host.getPreCopyCard() != null) {
            host.setPreCopyCard(withTokenStatus(mergeCharacteristics(host.getPreCopyCard(), entry.getCard(), onTop),
                    host.getMutatedComponentCards().getFirst().isToken()));
        }
        if (host.getWhileAttachedPreCopyCard() != null) {
            host.setWhileAttachedPreCopyCard(
                    withTokenStatus(mergeCharacteristics(host.getWhileAttachedPreCopyCard(), entry.getCard(), onTop),
                            host.getMutatedComponentCards().getFirst().isToken()));
        }
        if (host.getUntilNextTurnPreCopyCard() != null) {
            host.setUntilNextTurnPreCopyCard(
                    withTokenStatus(mergeCharacteristics(host.getUntilNextTurnPreCopyCard(), entry.getCard(), onTop),
                            host.getMutatedComponentCards().getFirst().isToken()));
        }
        if (onTop && host.isFaceDown()) {
            host.turnFaceUp();
        }
        UUID hostControllerId = gameQueryService.findPermanentController(gameData, host.getId());
        if (!host.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, host)) {
            triggerCollectionService.checkMutateTriggers(gameData, host, List.of(merged), hostControllerId);
        } else {
            host.recordMutation();
        }
        gameLogService.append(gameData, GameLog.cardThen(merged, " mutates."));
    }

    private boolean isOwnedNonHumanCreature(GameData gameData, Permanent target, UUID ownerId) {
        if (target == null || !gameQueryService.isCreature(gameData, target)
                || gameQueryService.hasEffectiveSubtype(gameData, target, CardSubtype.HUMAN)) {
            return false;
        }
        UUID targetOwner = gameData.stolenCreatures.get(target.getId());
        if (targetOwner == null) {
            targetOwner = target.getOriginalCard().getOwnerId();
        }
        if (targetOwner == null) {
            targetOwner = gameQueryService.findPermanentController(gameData, target.getId());
        }
        return ownerId.equals(targetOwner);
    }

    private Card mergeCharacteristics(Card existing, Card mutating, boolean onTop) {
        Card top = onTop ? mutating : existing;
        Card underlying = onTop ? existing : mutating;
        Card merged = top.createRuntimeCopy();
        merged.setCastTimeTargetFilter(null);
        var keywords = new HashSet<Keyword>(top.getKeywords());
        keywords.addAll(underlying.getKeywords());
        merged.setKeywords(keywords);
        for (var ability : underlying.getActivatedAbilities()) {
            merged.addActivatedAbility(ability);
        }
        List<CardEffect> inheritedEffects = new ArrayList<>();
        for (EffectSlot slot : EffectSlot.values()) {
            for (var registration : underlying.getEffectRegistrations(slot)) {
                merged.addEffect(slot, registration.effect(), registration.triggerMode());
                inheritedEffects.add(registration.effect());
            }
        }
        merged.appendSpellTargetingForEffectsFrom(underlying, inheritedEffects);
        String topText = top.getCardText() == null ? "" : top.getCardText();
        String underlyingText = underlying.getCardText() == null ? "" : underlying.getCardText();
        merged.setCardText(topText + (underlyingText.isBlank() ? "" : "\n" + underlyingText));
        merged.freeze();
        return merged;
    }

    private Card withTokenStatus(Card merged, boolean token) {
        if (merged.isToken() == token) {
            return merged;
        }
        Card result = merged.createRuntimeCopy();
        result.setToken(token);
        result.freeze();
        return result;
    }
}
