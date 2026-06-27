package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwardRestrictedManaEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardRestrictedManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardRestrictedManaEffect) effect;
        UUID controllerId = entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        if (e.color() == ManaColor.RED) {
            pool.addRestrictedRed(e.amount());
        } else if (e.color() == ManaColor.COLORLESS) {
            pool.addInstantSorceryOnlyColorless(e.amount());
        } else {
            pool.add(e.color(), e.amount());
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " adds " + e.amount() + " " + e.color().getCode()
                + " (" + e.allowedSpellTypes() + " spells only).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} adds {} {} (restricted to {})", gameData.id, playerName, e.amount(), e.color(), e.allowedSpellTypes());
    }
}
