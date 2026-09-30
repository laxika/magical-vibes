package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetCreatureCardIntoGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Chitinous Crawler's targeted graveyard duplicate. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTargetCreatureCardIntoGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTargetCreatureCardIntoGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetCardIds().isEmpty()
                ? entry.getTargetId()
                : entry.getTargetCardIds().getFirst();
        Card targetCard = targetCardId == null
                ? null
                : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null
                || !targetCard.hasType(CardType.CREATURE)
                || !entry.getControllerId().equals(gameQueryService.findGraveyardOwnerById(gameData, targetCardId))) {
            return;
        }

        Card duplicate = targetCard.createConjuredCopy();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.freeze();
        graveyardService.addCardToGraveyard(gameData, entry.getControllerId(), duplicate);
        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", targetCard, " is conjured into your graveyard."));
    }
}
