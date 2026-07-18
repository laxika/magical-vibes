package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect) effect;

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
        String cardName = entry.getCard().getName();
        UUID controllerId = entry.getControllerId();

        List<Permanent> destroyed = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;

            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent permanent : new ArrayList<>(battlefield)) {
                boolean isCreature = gameQueryService.isCreature(gameData, permanent);
                boolean isPlaneswalker = permanent.getCard().hasType(CardType.PLANESWALKER);
                if (!isCreature && !isPlaneswalker) continue;
                if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, permanent, entry.getCard())) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(cardName + "'s damage to ", permanent.getCard(), " is prevented."));
                    continue;
                }
                damageSupport.dealCreatureDamage(gameData, entry, permanent, rawDamage);
            }
        }
        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
