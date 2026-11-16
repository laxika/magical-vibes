package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnForXValueEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CopyNextInstantOrSorceryCastThisTurnForXValueEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextInstantOrSorceryCastThisTurnForXValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int copies = entry.getXValue();
        if (copies <= 0) {
            return;
        }

        // "Copy that spell X times": one delayed triggered ability making X copies, X fixed as it is made (CR 603.7b).
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.nextThisTurn(
                entry.getControllerId(), entry.getCard(),
                new CopyNextInstantOrSorceryCastThisTurnEffect().delayedAbility(copies), entry.getSourcePermanentId()));
        log.info("Game {} - {} will copy their next instant or sorcery spell {} times this turn",
                gameData.id, entry.getControllerId(), copies);
    }
}
