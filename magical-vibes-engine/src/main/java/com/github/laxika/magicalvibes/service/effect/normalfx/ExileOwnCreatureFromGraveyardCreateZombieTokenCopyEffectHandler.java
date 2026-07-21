package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCreatureFromGraveyardCreateZombieTokenCopyEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolution-time up-to-one choice of a creature card in the controller's graveyard. Completion is
 * handled by {@code GraveyardChoiceHandlerService} via
 * {@code graveyardTargetOperation.resolutionTimeExileCreateZombieTokenCopyResume}.
 */
@Component
@RequiredArgsConstructor
public class ExileOwnCreatureFromGraveyardCreateZombieTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOwnCreatureFromGraveyardCreateZombieTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(controllerId, List.of());
        List<Card> creatureCards = new ArrayList<>();
        for (Card card : graveyard) {
            if (card.hasType(CardType.CREATURE)) {
                creatureCards.add(card);
            }
        }

        if (creatureCards.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no creature cards in its controller's graveyard to exile."));
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeExileCreateZombieTokenCopyResume = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, creatureCards, 1,
                "You may exile a creature card from your graveyard.");
    }
}
