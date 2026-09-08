package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandThenEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a non-targeting permanent return with an "if you do" follow-up. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnPermanentControlledByPlayerToHandThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnPermanentControlledByPlayerToHandThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnPermanentControlledByPlayerToHandThenEffect) effect;
        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId);

        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                    validIds.add(permanent.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData,
                    GameLog.text(playerName + " controls no " + e.permanentDescription() + " to return."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BouncePermanentThen(
                controllerId, entry.getCard(), entry.getSourcePermanentId(), e.thenEffect()));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                entry.getCard().getName() + " — Choose " + e.permanentDescription()
                        + " to return to its owner's hand.");
        log.info("Game {} - {} choosing {} to return for {}", gameData.id, playerName(controllerId, gameData),
                e.permanentDescription(), entry.getCard().getName());
    }

    private String playerName(UUID playerId, GameData gameData) {
        return gameData.playerIdToName.get(playerId);
    }
}
