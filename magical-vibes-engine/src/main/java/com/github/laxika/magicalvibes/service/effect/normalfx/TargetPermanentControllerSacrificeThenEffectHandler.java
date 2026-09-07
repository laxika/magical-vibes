package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerSacrificeThenEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPermanentControllerSacrificeThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPermanentControllerSacrificeThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrificeEffect = (TargetPermanentControllerSacrificeThenEffect) effect;
        UUID targetControllerId = entry.getTargetId() == null
                ? null
                : gameQueryService.findPermanentController(gameData, entry.getTargetId());
        if (targetControllerId == null) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(targetControllerId)
                .withSourcePermanentId(entry.getSourcePermanentId());
        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(
                        permanent, sacrificeEffect.filter(), filterContext)) {
                    validIds.add(permanent.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(targetControllerId) + " has no "
                            + sacrificeEffect.permanentDescription() + " to sacrifice."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificePermanentThen(
                targetControllerId, entry.getCard(), sacrificeEffect.thenEffect(), false));
        playerInputService.beginPermanentChoice(
                gameData,
                targetControllerId,
                validIds,
                entry.getCard().getName() + " — Choose " + sacrificeEffect.permanentDescription() + " to sacrifice.");
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(targetControllerId) + " is choosing "
                        + sacrificeEffect.permanentDescription() + " to sacrifice."));
        log.info("Game {} - {} choosing {} to sacrifice for {}",
                gameData.id,
                gameData.playerIdToName.get(targetControllerId),
                sacrificeEffect.permanentDescription(),
                entry.getCard().getName());
    }
}
