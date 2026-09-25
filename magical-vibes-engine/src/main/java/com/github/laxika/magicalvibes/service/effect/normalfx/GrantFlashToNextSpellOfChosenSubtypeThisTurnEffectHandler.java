package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextSpellOfChosenSubtypeThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantFlashToNextSpellOfChosenSubtypeThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFlashToNextSpellOfChosenSubtypeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null || source.getChosenSubtype() == null) {
            return;
        }

        CardSubtype chosenSubtype = source.getChosenSubtype();
        gameData.addNextSpellFlashGrantForChosenSubtype(entry.getControllerId(), chosenSubtype);
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" lets its controller cast their next " + chosenSubtype.getDisplayName()
                        + " spell this turn as though it had flash.")
                .build());
        log.info("Game {} - {} grants flash to the next {} spell for player {} this turn",
                gameData.id, entry.getCard().getName(), chosenSubtype.getDisplayName(), entry.getControllerId());
    }
}
