package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DomriRadeEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomriRadeEmblemEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DomriRadeEmblemEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        Emblem emblem = new Emblem(controllerId, List.of(
                grant(Keyword.DOUBLE_STRIKE),
                grant(Keyword.TRAMPLE),
                grant(Keyword.HEXPROOF),
                grant(Keyword.HASTE)
        ), entry.getCard());

        gameData.emblems.add(emblem);

        gameLogService.append(gameData, GameLog.text(playerName
                + " gets an emblem with \"Creatures you control have double strike, trample, hexproof, and haste.\"."));

        log.info("Game {} - {} gets Domri Rade emblem", gameData.id, playerName);
    }

    private GrantKeywordEffect grant(Keyword keyword) {
        return new GrantKeywordEffect(keyword, GrantScope.OWN_PERMANENTS, new PermanentIsCreaturePredicate());
    }
}
