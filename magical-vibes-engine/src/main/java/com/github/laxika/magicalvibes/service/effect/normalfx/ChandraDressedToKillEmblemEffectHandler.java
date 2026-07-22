package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChandraDressedToKillEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToManaSpentToCastToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChandraDressedToKillEmblemEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChandraDressedToKillEmblemEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        Emblem emblem = new Emblem(controllerId, List.of(
                new DealDamageEqualToManaSpentToCastToAnyTargetEffect(new CardColorPredicate(CardColor.RED))
        ), entry.getCard());

        gameData.emblems.add(emblem);

        String logEntry = playerName + " gets an emblem with \"Whenever you cast a red spell, this "
                + "emblem deals X damage to any target, where X is the amount of mana spent to cast "
                + "that spell.\".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} gets Chandra, Dressed to Kill emblem", gameData.id, playerName);
    }
}
