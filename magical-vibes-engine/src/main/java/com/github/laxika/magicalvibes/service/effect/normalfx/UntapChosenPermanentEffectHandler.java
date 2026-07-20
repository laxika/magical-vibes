package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapChosenPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link UntapChosenPermanentEffect}: the controller chooses one permanent matching the
 * effect's predicate across every battlefield, and it is untapped when the choice is answered
 * (see {@code MultiPermanentChoiceHandlerService.handleUntapChosenPermanent}).
 */
@Component
@RequiredArgsConstructor
public class UntapChosenPermanentEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
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
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability resolves, but there is nothing to untap."));
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), validIds, 1,
                new MultiPermanentChoiceContext.UntapChosenPermanent(entry.getCard().getName()),
                entry.getCard().getName() + "'s ability — Choose a creature or land to untap.");
    }
}
