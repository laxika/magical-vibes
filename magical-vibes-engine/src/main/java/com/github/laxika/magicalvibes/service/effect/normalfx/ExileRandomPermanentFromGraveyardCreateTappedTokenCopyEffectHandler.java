package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var controllerId = entry.getControllerId();

        while (true) {
            List<Card> permanentCards = gameData.playerGraveyards
                    .getOrDefault(controllerId, List.of()).stream()
                    .filter(card -> card.getType().isPermanentType())
                    .toList();
            if (permanentCards.isEmpty()) {
                return;
            }

            Card exiledCard = permanentCards.get(ThreadLocalRandom.current().nextInt(permanentCards.size()));
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, exiledCard.getId());
            exileService.exileCard(gameData, controllerId, exiledCard);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " exiles ", exiledCard,
                    " at random from their graveyard."));

            graveyardReturnSupport.createTokenCopyFromCard(
                    gameData, entry, exiledCard, List.of(), false, false,
                    null, null, null, false, false, new ArrayList<>(), Set.of(), true);

            if (!exiledCard.hasType(CardType.LAND)) {
                return;
            }
        }
    }
}
