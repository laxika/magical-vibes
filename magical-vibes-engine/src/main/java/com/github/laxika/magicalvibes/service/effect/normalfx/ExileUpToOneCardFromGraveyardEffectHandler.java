package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "You may exile target card from a graveyard." Gathers every card in every graveyard and prompts the
 * controller to choose up to one to exile (they may choose none). Completion is handled by
 * {@code GraveyardChoiceHandlerService.handleMultipleCardsChosen} via
 * {@code graveyardTargetOperation.resolutionTimeExileResume}, which exiles the chosen card and
 * resumes any remaining effects on the same ability (e.g. the cycling draw). Mirrors
 * {@link ExileUpToNAttackingCreaturesEffectHandler}, but over graveyards instead of attackers.
 */
@Component
@RequiredArgsConstructor
public class ExileUpToOneCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUpToOneCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        List<Card> graveyardCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard != null) {
                graveyardCards.addAll(graveyard);
            }
        }

        if (graveyardCards.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no cards in any graveyard to exile."));
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeExileResume = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, graveyardCards, 1,
                "Choose up to one target card from a graveyard to exile.");
    }
}
