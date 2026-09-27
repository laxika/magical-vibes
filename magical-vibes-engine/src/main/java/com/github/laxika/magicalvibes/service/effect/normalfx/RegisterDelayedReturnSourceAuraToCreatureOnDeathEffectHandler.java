package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedReturnSourceAuraToCreature;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnSourceAuraToCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Registers Ghoulish Impetus's next-end-step Aura return. */
@Component
@RequiredArgsConstructor
public class RegisterDelayedReturnSourceAuraToCreatureOnDeathEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnSourceAuraToCreatureOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, entry.getCard().getId());
        if (auraCard == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s delayed return fizzles (card not in a graveyard)."));
            return;
        }

        gameData.queueDelayedAction(new DelayedReturnSourceAuraToCreature(
                auraCard.getId(), entry.getControllerId()));
        gameLogService.append(gameData,
                GameLog.cardThen(auraCard, " will return to the battlefield at the beginning of the next end step."));
    }
}
