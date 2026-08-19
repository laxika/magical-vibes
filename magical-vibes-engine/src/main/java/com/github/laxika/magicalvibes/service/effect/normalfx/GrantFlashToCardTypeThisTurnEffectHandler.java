package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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
        gameData.addCardPredicateFlashGrant(entry.getControllerId(), grant.filter());
        String typeName = grant.filter() instanceof CardTypePredicate type
                ? type.cardType().name().toLowerCase(Locale.ROOT) : "matching";
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" lets its controller cast " + typeName
                        + " spells this turn as though they had flash.")
                .build());
        log.info("Game {} - {} grants flash to {} spells for player {} this turn",
                gameData.id, entry.getCard().getName(), typeName, entry.getControllerId());
    }
}
