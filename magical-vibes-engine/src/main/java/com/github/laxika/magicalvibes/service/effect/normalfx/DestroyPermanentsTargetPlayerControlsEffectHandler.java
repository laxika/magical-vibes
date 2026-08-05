package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentsTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyPermanentsTargetPlayerControlsEffect}: destroys every permanent the
 * targeted player controls that matches the effect's filter, honouring indestructible and
 * regeneration. Used by Ajani Vengeant's ultimate to destroy all of a player's lands.
 */
@Component
@RequiredArgsConstructor
public class DestroyPermanentsTargetPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyPermanentsTargetPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyPermanentsTargetPlayerControlsEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> toDestroy = new ArrayList<>();
        for (Permanent perm : List.copyOf(battlefield)) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, perm, e.filter())) {
                toDestroy.add(perm);
            }
        }

        // One effect destroys them all simultaneously; destroyBatchCollecting owns the indestructible
        // snapshot, regeneration and the simultaneous-death bookkeeping.
        destructionSupport.destroyBatchCollecting(gameData, toDestroy, entry.getCard().getName(), false);
    }
}
