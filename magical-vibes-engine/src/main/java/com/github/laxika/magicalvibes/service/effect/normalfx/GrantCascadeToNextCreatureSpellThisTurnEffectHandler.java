package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCascadeToNextCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Gives the controller's next creature spell this turn cascade: the cascade is held until that spell is cast, and ends
 * with the turn if none is (CR 603.7b).
 */
@Component
@RequiredArgsConstructor
public class GrantCascadeToNextCreatureSpellThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantCascadeToNextCreatureSpellThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(
                entry.getControllerId(), entry.getCard(),
                ((GrantCascadeToNextCreatureSpellThisTurnEffect) effect).grantedAbility(),
                entry.getSourcePermanentId()));
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" grants cascade to its controller's next creature spell this turn.")
                .build());
    }
}
