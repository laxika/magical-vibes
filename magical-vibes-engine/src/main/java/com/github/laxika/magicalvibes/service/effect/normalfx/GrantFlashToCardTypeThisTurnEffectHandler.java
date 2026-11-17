package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantFlashToCardTypeThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFlashToCardTypeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantFlashToCardTypeThisTurnEffect grant = (GrantFlashToCardTypeThisTurnEffect) effect;
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), grant, entry.getControllerId(),
                grant.duration()));
        String typeName = grant.filter() instanceof CardTypePredicate type
                ? type.cardType().name().toLowerCase(Locale.ROOT) : "matching";
        String durationText = Duration.UNTIL_YOUR_NEXT_TURN.equals(grant.duration())
                ? "until their next turn" : "this turn";
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" lets its controller cast " + typeName
                        + " spells " + durationText + " as though they had flash.")
                .build());
        log.info("Game {} - {} grants flash to {} spells for player {} {}",
                gameData.id, entry.getCard().getName(), typeName, entry.getControllerId(), durationText);
    }
}
