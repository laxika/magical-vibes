package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapChosenPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link UntapChosenPermanentEffect}: the controller — or the entry's target player when
 * the effect sets {@code targetPlayerChooses} — chooses one permanent matching the effect's
 * predicate across every battlefield, and it is untapped when the choice is answered
 * (see {@code MultiPermanentChoiceHandlerService.handleUntapChosenPermanent}).
 */
@Component
@RequiredArgsConstructor
public class UntapChosenPermanentEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UntapChosenPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (UntapChosenPermanentEffect) effect;

        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((ownerId, perm) -> {
            if (e.predicate() == null
                    || predicateEvaluationService.matchesPermanentPredicate(gameData, perm, e.predicate())) {
                validIds.add(perm.getId());
            }
        });

        if (validIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability resolves, but there is nothing to untap."));
            return;
        }

        UUID chooserId = e.targetPlayerChooses() && entry.getTargetId() != null
                ? entry.getTargetId()
                : entry.getControllerId();

        playerInputService.beginMultiPermanentChoice(gameData, chooserId, validIds, 1,
                new MultiPermanentChoiceContext.UntapChosenPermanent(entry.getCard().getName()),
                entry.getCard().getName() + "'s ability — Choose a permanent to untap.");
    }
}
