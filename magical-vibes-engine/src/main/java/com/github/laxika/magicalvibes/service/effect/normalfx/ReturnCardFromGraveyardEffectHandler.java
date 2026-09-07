package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnCardFromGraveyardEffect) effect;

        UUID controllerId = entry.getControllerId();
        UUID sourceCardId = entry.getCard().getId();

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
            if (boundTargets != null && !boundTargets.isEmpty()) {
                graveyardReturnSupport.resolvePreTargetedById(
                        gameData, entry, e, controllerId, sourceCardId, boundTargets.getFirst());
                return;
            }
        }

        // Pre-targeted via targetCardIds (from triggered abilities, e.g. Teshar).
        // Combat-damage triggers also use targetId for the damaged player, so the graveyard card
        // target must take precedence when both target fields are present.
        if (e.targetGraveyard() && entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()) {
            UUID targetCardId = entry.getTargetCardIds().getFirst();
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
