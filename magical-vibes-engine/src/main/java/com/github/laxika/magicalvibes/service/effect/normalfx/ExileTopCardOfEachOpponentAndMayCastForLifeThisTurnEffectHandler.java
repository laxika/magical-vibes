package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachOpponentAndMayCastForLifeThisTurnEffect;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTopCardOfEachOpponentAndMayCastForLifeThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfEachOpponentAndMayCastForLifeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(controllerId)) {
                continue;
            }
            var library = gameData.playerDecks.get(opponentId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            Card card = library.removeFirst();
            exileService.exileCard(gameData, opponentId, card);
            if (!card.hasType(CardType.LAND)) {
                gameData.exilePlayPermissions.put(card.getId(), controllerId);
                gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
                gameData.exilePlayForLifeEqualToManaValue.add(card.getId());
            }
        }
    }
}
