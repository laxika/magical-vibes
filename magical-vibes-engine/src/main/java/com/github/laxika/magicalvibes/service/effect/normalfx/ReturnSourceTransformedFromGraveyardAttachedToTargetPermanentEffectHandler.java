package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardAttachedToTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTransformedReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceTransformedFromGraveyardAttachedToTargetPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GraveyardTransformedReturnService graveyardTransformedReturnService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceTransformedFromGraveyardAttachedToTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        UUID targetControllerId = target == null ? null : gameData.findControllerOf(target);
        if (target == null
                || (!gameQueryService.isCreature(gameData, target) && !gameQueryService.isPlaneswalker(gameData, target))
                || entry.getControllerId() == null
                || entry.getControllerId().equals(targetControllerId)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability fizzles (no legal permanent to attach to)."));
            log.info("Game {} - {} fizzles, attach target missing or illegal",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Card card = entry.getCard();
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (ownerId == null) {
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is no longer in a graveyard; it doesn't return."));
            log.info("Game {} - Transformed return for {} fizzles (not in a graveyard)", gameData.id, card.getName());
            return;
        }

        graveyardTransformedReturnService.returnTransformed(
                gameData, card.getId(), ownerId, entry.getControllerId(), targetId);
    }
}
