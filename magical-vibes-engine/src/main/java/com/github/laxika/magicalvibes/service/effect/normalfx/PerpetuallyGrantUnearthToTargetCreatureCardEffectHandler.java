package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantUnearthToTargetCreatureCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetuallyGrantUnearthToTargetCreatureCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantUnearthToTargetCreatureCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetCardIds().isEmpty()
                ? entry.getTargetZone() == Zone.GRAVEYARD ? entry.getTargetId() : null
                : entry.getTargetCardIds().getFirst();
        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null
                || !entry.getControllerId().equals(gameQueryService.findGraveyardOwnerById(gameData, targetCardId))
                || !targetCard.hasType(CardType.CREATURE)
                || gameQueryService.cardHasUnearthAbility(gameData, entry.getControllerId(), targetCard)) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target is no longer a valid creature card without unearth)."));
            return;
        }

        gameData.cardsGrantedPerpetualUnearth.add(targetCard.getId());
        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " perpetually grants unearth to ",
                targetCard, "."));
    }
}
