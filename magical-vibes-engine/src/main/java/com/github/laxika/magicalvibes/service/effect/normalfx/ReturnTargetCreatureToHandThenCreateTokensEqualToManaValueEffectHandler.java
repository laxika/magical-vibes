package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandThenCreateTokensEqualToManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Aether Mutation's bounce and mana-value token rider. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReturnTargetCreatureToHandThenCreateTokensEqualToManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureToHandThenCreateTokensEqualToManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCreatureToHandThenCreateTokensEqualToManaValueEffect) effect;
        Permanent target = entry.getTargetId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        int manaValue = target.getCard().getManaValue();
        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                    " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id,
                    target.getCard().getName(), entry.getCard().getName());
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (manaValue <= 0) {
            return;
        }
        CreateTokenEffect tokenEffect = e.tokenTemplate().withAmount(manaValue);
        EffectHandler handler = effectHandlerRegistry.getHandler(tokenEffect);
        if (handler != null) {
            handler.resolve(gameData, entry, tokenEffect);
        }
    }
}
