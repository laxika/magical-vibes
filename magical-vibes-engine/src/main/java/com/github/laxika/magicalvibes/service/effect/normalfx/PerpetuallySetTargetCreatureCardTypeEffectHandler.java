package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetTargetCreatureCardTypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetuallySetTargetCreatureCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallySetTargetCreatureCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallySetTargetCreatureCardTypeEffect e =
                (PerpetuallySetTargetCreatureCardTypeEffect) effect;
        UUID targetCardId = !entry.getTargetCardIds().isEmpty()
                ? entry.getTargetCardIds().getFirst()
                : entry.getTargetId();
        if (targetCardId == null) {
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null || !targetCard.hasType(CardType.CREATURE)) {
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (graveyardOwnerId == null || !graveyardOwnerId.equals(entry.getControllerId())) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
        if (graveyard == null) {
            return;
        }

        for (int i = 0; i < graveyard.size(); i++) {
            if (!graveyard.get(i).getId().equals(targetCardId)) {
                continue;
            }
            Card modifiedCard = targetCard.createRuntimeCopy();
            modifiedCard.setType(e.cardType());
            modifiedCard.setAdditionalTypes(Set.of());
            modifiedCard.freeze();
            graveyard.set(i, modifiedCard);
            gameLogService.append(gameData, GameLog.cardTextCard(
                    entry.getCard(), " perpetually changes ", targetCard,
                    " to only the " + e.cardType().getDisplayName() + " card type."));
            return;
        }
    }
}
