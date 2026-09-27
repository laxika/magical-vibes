package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilLandOfTargetOpponentEffect;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTopUntilLandOfTargetOpponentEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilLandOfTargetOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        if (opponentId == null || !gameData.playerIds.contains(opponentId) || opponentId.equals(controllerId)) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(opponentId);
        if (library == null) {
            return;
        }

        while (!library.isEmpty()) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, opponentId, card);
            if (card.hasType(CardType.LAND)) {
                break;
            }
        }
    }
}
