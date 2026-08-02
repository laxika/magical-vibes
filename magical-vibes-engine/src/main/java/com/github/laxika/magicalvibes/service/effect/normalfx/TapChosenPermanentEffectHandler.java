package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapChosenPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TapChosenPermanentEffect}: the controller chooses one permanent matching the
 * effect's predicate across every battlefield, and it is tapped (and optionally untap-locked while
 * the source stays tapped) when the choice is answered
 * (see {@code MultiPermanentChoiceHandlerService.handleTapChosenPermanent}).
 */
@Component
@RequiredArgsConstructor
public class TapChosenPermanentEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapChosenPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TapChosenPermanentEffect) effect;

        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((ownerId, perm) -> {
            if (e.predicate() == null
                    || predicateEvaluationService.matchesPermanentPredicate(gameData, perm, e.predicate())) {
                validIds.add(perm.getId());
            }
        });

        if (validIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability resolves, but there is nothing to tap."));
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), validIds, 1,
                new MultiPermanentChoiceContext.TapChosenPermanent(entry.getCard().getName(),
                        entry.getSourcePermanentId(), e.preventUntapWhileSourceTapped()),
                entry.getCard().getName() + "'s ability — Choose a permanent to tap.");
    }
}
