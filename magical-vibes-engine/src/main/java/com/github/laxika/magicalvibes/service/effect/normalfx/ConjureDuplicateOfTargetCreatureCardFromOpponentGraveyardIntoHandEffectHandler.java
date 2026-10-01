package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetCreatureCardFromOpponentGraveyardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetualAnyManaTypeToCastSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Bind to Secrecy's opponent-graveyard creature duplicate. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTargetCreatureCardFromOpponentGraveyardIntoHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTargetCreatureCardFromOpponentGraveyardIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.targetsForEffect(effect).stream().findFirst().orElse(null);
        Card targetCard = targetCardId == null
                ? null
                : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID graveyardOwnerId = targetCardId == null
                ? null
                : gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (targetCard == null
                || !targetCard.hasType(CardType.CREATURE)
                || graveyardOwnerId == null
                || entry.getControllerId().equals(graveyardOwnerId)) {
            return;
        }

        Card duplicate = targetCard.createConjuredCopy();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.setToken(true);
        duplicate.setTokenCard(true);
        duplicate.addEffect(EffectSlot.STATIC, new PerpetualAnyManaTypeToCastSelfEffect());
        duplicate.freeze();
        gameData.addCardToHand(entry.getControllerId(), duplicate);
        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", duplicate, " is conjured into your hand."));
    }
}
