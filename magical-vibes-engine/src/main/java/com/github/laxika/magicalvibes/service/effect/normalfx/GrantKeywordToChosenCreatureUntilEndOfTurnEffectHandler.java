package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantKeywordToChosenCreatureUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordToChosenCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantKeywordToChosenCreatureUntilEndOfTurnEffect) effect;
        if (e.chosenCreatureId() == null) {
            log.info("Game {} - {} ability has no chosen creature", gameData.id,
                    entry.getCard() != null ? entry.getCard().getName() : "Unknown");
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, e.chosenCreatureId());
        if (target == null) {
            log.info("Game {} - Chosen creature no longer on battlefield", gameData.id);
            return;
        }

        target.getGrantedKeywords().add(e.keyword());
        String keywordName = e.keyword().name().charAt(0) + e.keyword().name().substring(1).toLowerCase().replace('_', ' ');
        String logEntry = target.getCard().getName() + " gains " + keywordName + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gains {} (chosen creature)", gameData.id, target.getCard().getName(), e.keyword());
    }
}
