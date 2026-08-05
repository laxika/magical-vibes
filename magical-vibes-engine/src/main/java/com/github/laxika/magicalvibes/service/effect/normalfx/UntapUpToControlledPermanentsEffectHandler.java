package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapUpToControlledPermanentsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UntapUpToControlledPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UntapUpToControlledPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (UntapUpToControlledPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        List<UUID> validIds = new ArrayList<>();
        for (Permanent p : battlefield) {
            if (!p.isTapped()) continue;
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter())) continue;

            validIds.add(p.getId());
        }
        if (validIds.isEmpty()) {
            return;
        }

        // "Untap up to N ..." — the controller chooses which permanents to untap at resolution;
        // picking none is legal. Untapping the first N in battlefield order would deny that choice.
        int maxCount = Math.min(e.count(), validIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validIds, maxCount,
                new MultiPermanentChoiceContext.UntapChosenPermanents(entry.getCard().getName()),
                entry.getCard().getName() + " — Choose up to " + maxCount + " permanent"
                        + (maxCount == 1 ? "" : "s") + " to untap.");
    }
}
