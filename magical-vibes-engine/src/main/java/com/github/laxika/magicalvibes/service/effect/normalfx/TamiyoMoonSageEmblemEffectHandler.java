package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardPutIntoGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TamiyoMoonSageEmblemEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Creates Tamiyo, the Moon Sage's emblem. The "you have no maximum hand size" half is granted by a
 * companion {@code GrantPermanentNoMaxHandSizeEffect} on the same ability, so the emblem only needs
 * to carry the graveyard-return trigger marker.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TamiyoMoonSageEmblemEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TamiyoMoonSageEmblemEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        gameData.emblems.add(new Emblem(controllerId, List.of(
                new ReturnCardPutIntoGraveyardToHandEffect()
        ), entry.getCard()));

        gameLogService.append(gameData, GameLog.text(playerName + " gets an emblem with \"Whenever a card"
                + " is put into your graveyard from anywhere, you may return it to your hand.\"."));
        log.info("Game {} - {} gets Tamiyo, the Moon Sage emblem", gameData.id, playerName);
    }
}
