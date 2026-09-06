package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DamageForCardsStillExiledAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepAndDamageForEachStillExiledEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsMayPlayUntilNextEndStepAndDamageForEachStillExiledEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsMayPlayUntilNextEndStepAndDamageForEachStillExiledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTopCardsMayPlayUntilNextEndStepAndDamageForEachStillExiledEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);

        if (sourcePermanent == null && sourcePermanentId == null) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (permanent.getCard().getId().equals(entry.getCard().getId())) {
                        sourcePermanent = permanent;
                        sourcePermanentId = permanent.getId();
                        break;
                    }
                }
            }
        }
        if (sourcePermanent == null) {
            sourcePermanent = entry.getSourcePermanentSnapshot();
        }

        int count = Math.max(0, amountEvaluationService.evaluate(gameData, exileEffect.count(),
                AmountContext.forStackEntry(entry, sourcePermanent)));
        List<UUID> exiledCardIds = new ArrayList<>();
        List<String> exiledNames = new ArrayList<>();
        List<Card> library = gameData.playerDecks.get(controllerId);
        int toExile = library == null ? 0 : Math.min(count, library.size());
        for (int i = 0; i < toExile; i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, controllerId, card);
            exileSupport.grantPlayUntilOwnersNextEndStep(gameData, card.getId(), controllerId);
            exiledCardIds.add(card.getId());
            exiledNames.add(card.getName());
        }

        gameData.queueDelayedAction(new DamageForCardsStillExiledAtNextEndStep(
                controllerId, sourcePermanentId, entry.getCard(), List.copyOf(exiledCardIds),
                exileEffect.damagePerCard()));

        if (!exiledNames.isEmpty()) {
            String controllerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(controllerName + " exiles "
                    + String.join(", ", exiledNames)
                    + " from the top of their library (may play until their next end step)."));
            log.info("Game {} - {} exiles {} cards from library top (may play until next end step)",
                    gameData.id, controllerName, exiledNames.size());
        }
    }
}
