package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformAnyNumberOfPermanentsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a controller's choice of zero or more matching permanents to transform. */
@Component
@RequiredArgsConstructor
public class TransformAnyNumberOfPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransformAnyNumberOfPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TransformAnyNumberOfPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> eligibleIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, e.filter())) {
                    eligibleIds.add(permanent.getId());
                }
            }
        }

        if (eligibleIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds, eligibleIds.size(),
                new MultiPermanentChoiceContext.TransformAnyNumber(entry, e.filter()),
                "Choose any number of permanents to transform.");
    }
}
