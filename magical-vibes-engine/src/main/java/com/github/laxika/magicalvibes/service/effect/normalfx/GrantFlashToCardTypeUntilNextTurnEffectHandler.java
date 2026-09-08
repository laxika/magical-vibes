package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class GrantFlashToCardTypeUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFlashToCardTypeUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantFlashToCardTypeUntilNextTurnEffect grant =
                (GrantFlashToCardTypeUntilNextTurnEffect) effect;
        gameData.cardTypeFlashGrantsUntilNextTurn
                .computeIfAbsent(entry.getControllerId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(grant.filter());
        String typeName = grant.filter() instanceof CardTypePredicate type
                ? type.cardType().name().toLowerCase(Locale.ROOT) : "matching";
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" lets its controller cast " + typeName
                        + " spells as though they had flash until their next turn.")
                .build());
    }
}
