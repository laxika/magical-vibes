package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStormToNextInstantOrSorceryCastThisTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Gives the controller's next instant or sorcery spell this turn storm: the storm is held until that spell is cast, and
 * ends with the turn if none is (CR 603.7b).
 */
@Slf4j
@Component
public class GrantStormToNextInstantOrSorceryCastThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantStormToNextInstantOrSorceryCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(
                entry.getControllerId(), entry.getCard(),
                ((GrantStormToNextInstantOrSorceryCastThisTurnEffect) effect).grantedAbility(),
                entry.getSourcePermanentId()));
        log.info("Game {} - {} will give their next instant or sorcery spell Storm this turn",
                gameData.id, entry.getControllerId());
    }
}
