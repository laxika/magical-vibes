package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAbilityToNextSpellThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Gives the controller's next matching spell this turn an ability of its own: the ability is held until that spell is
 * cast, and ends with the turn if none is (CR 603.7b).
 */
@Component
@RequiredArgsConstructor
public class GrantAbilityToNextSpellThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAbilityToNextSpellThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantAbilityToNextSpellThisTurnEffect) effect;
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(
                entry.getControllerId(), entry.getCard(), grant.grantedAbility(), entry.getSourcePermanentId()));
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" grants " + grant.keyword() + " to its controller's next matching spell this turn.")
                .build());
    }
}
