package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, UUID> before = new LinkedHashMap<>();
        gameData.playerGraveyards.forEach((ownerId, graveyard) -> graveyard.forEach(card -> before.put(card.getId(),
                ownerId)));
        returnCard(gameData, entry, (ReturnCardFromGraveyardEffect) effect);
        recordReturned(gameData, entry, before);
    }

    /**
     * The step's result: the cards that were in a graveyard before it and are now in a hand (the card) or on the
     * battlefield (the permanent it became), and their owners. A return that waits for its player's choice records
     * none: the choice's completion returns the card, so this handler is not an earlier step that may be read.
     */
    private void recordReturned(GameData gameData, StackEntry entry, Map<UUID, UUID> before) {
        Set<UUID> inGraveyards = new HashSet<>();
        gameData.playerGraveyards.values().forEach(graveyard -> graveyard.forEach(card -> inGraveyards.add(card.getId())));
        Set<UUID> inHands = new HashSet<>();
        gameData.playerHands.values().forEach(hand -> hand.forEach(card -> inHands.add(card.getId())));
        Map<UUID, UUID> permanentsByCard = new HashMap<>();
        gameData.forEachPermanent((playerId, permanent) ->
                permanentsByCard.put(permanent.getOriginalCard().getId(), permanent.getId()));
        List<UUID> returned = new ArrayList<>();
        List<UUID> owners = new ArrayList<>();
        before.forEach((cardId, ownerId) -> {
            if (inGraveyards.contains(cardId)) {
                return;
            }
            UUID returnedId = inHands.contains(cardId) ? cardId : permanentsByCard.get(cardId);
            if (returnedId != null) {
                returned.add(returnedId);
                owners.add(ownerId);
            }
        });
        entry.recordStep(StepKind.RETURN, StepOutcome.ofObjects(returned, owners));
    }

    private void returnCard(GameData gameData, StackEntry entry, ReturnCardFromGraveyardEffect e) {
        CardEffect effect = e;
        if (e.filter() instanceof CardIsSelfPredicate
                && entry.sourceCardGraveyardEntryVersion() >= 0
                && gameData.graveyardEntryVersion(entry.getCard().getId())
                != entry.sourceCardGraveyardEntryVersion()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        UUID sourceCardId = entry.getCard().getId();

        if (e.cards() != null) {
            Map<UUID, Set<UUID>> cardIdsByGraveyard = new LinkedHashMap<>();
            for (SubjectResolutionService.LocatedCard card
                    : subjectResolutionService.locatedCards(gameData, entry, e, e.cards())) {
                if (card.zone() == Zone.GRAVEYARD && card.ownerId() != null) {
                    cardIdsByGraveyard.computeIfAbsent(card.ownerId(), ignored -> new LinkedHashSet<>())
                            .add(card.card().getId());
                }
            }
            graveyardReturnSupport.resolveReturnOfCards(gameData, entry, e, controllerId, sourceCardId,
                    cardIdsByGraveyard);
            return;
        }

        // Case 0: Choose permanent type first (e.g. Creeping Renaissance)
        if (e.choosePermanentType()) {
            playerInputService.beginPermanentTypeChoice(gameData, controllerId, e.destination(), entry.getDescription());
            return;
        }

        // Skip if targeted graveyard e but no target was provided ("up to one" with 0 chosen)
        if (e.targetGraveyard() && entry.getTargetId() == null
                && (entry.getTargetCardIds() == null || entry.getTargetCardIds().isEmpty())) {
            return;
        }

        if (e.targetGraveyard()) {
            graveyardReturnSupport.recordTargetPowerToughnessBeforeReturn(gameData, entry, effect);
        }

        if (e.targetGraveyard() && e.targetGroup() >= 0) {
            UUID targetCardId = entry.getTargetCardIdsForEffect(effect).stream().findFirst().orElse(null);
            if (targetCardId == null) {
                return;
            }
            graveyardReturnSupport.resolvePreTargetedById(gameData, entry, e, controllerId, sourceCardId, targetCardId);
            return;
        }

        if (e.targetGraveyard()) {
            List<UUID> boundTargets = entry.targetsForBoundEffectGroup(effect);
            // A sequence bound to a permanent's target group (Ragnarok: destroy target permanent and
            // return target card) has its card chosen separately as targetCardIds.
            boolean boundToOtherTarget = boundTargets != null && !boundTargets.isEmpty()
                    && entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()
                    && !entry.getTargetCardIds().contains(boundTargets.getFirst())
                    && gameQueryService.findCardInGraveyardById(gameData, boundTargets.getFirst()) == null;
            if (boundTargets != null && !boundTargets.isEmpty() && !boundToOtherTarget) {
                graveyardReturnSupport.resolvePreTargetedById(
                        gameData, entry, e, controllerId, sourceCardId, boundTargets.getFirst());
                return;
            }
        }

        // Pre-targeted via targetCardIds (from triggered abilities, e.g. Teshar).
        // Combat-damage triggers also use targetId for the damaged player, so the graveyard card
        // target must take precedence when both target fields are present.
        if (e.targetGraveyard() && entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()) {
            UUID targetCardId = entry.getTargetCardIdsForEffect(effect).stream().findFirst().orElse(null);
            if (targetCardId == null) {
                return;
            }
            graveyardReturnSupport.resolvePreTargetedById(gameData, entry, e, controllerId, sourceCardId, targetCardId);
            return;
        }

        // Pre-targeted from a spell cast or activated ability.
        if (e.targetGraveyard() && entry.getTargetId() != null) {
            graveyardReturnSupport.resolvePreTargetedById(gameData, entry, e, controllerId, sourceCardId, entry.getTargetId());
            return;
        }

        // Case 1c: Topmost matching card of the controller's ordered graveyard (Shallow Grave).
        // No choice at all, so it resolves through the pre-targeted path with the card resolved here.
        if (e.topmost()) {
            graveyardReturnSupport.resolveTopmostFromControllersGraveyard(gameData, entry, e, controllerId, sourceCardId);
            return;
        }

        // Case 2: Return all matching cards (no choice)
        if (e.returnAll()) {
            graveyardReturnSupport.resolveReturnAll(gameData, entry, e, controllerId, sourceCardId);
            return;
        }

        // Case 3: Return a random matching card (no choice)
        if (e.returnAtRandom()) {
            graveyardReturnSupport.resolveReturnAtRandom(gameData, entry, e, controllerId, sourceCardId);
            return;
        }

        // Case 4: Search and choose at resolution
        if (e.source() == GraveyardSearchScope.ALL_GRAVEYARDS) {
            graveyardReturnSupport.resolveFromAllGraveyards(gameData, entry, e, controllerId, sourceCardId);
        } else {
            graveyardReturnSupport.resolveFromControllersGraveyard(gameData, entry, e, controllerId, sourceCardId);
        }
    }
}
