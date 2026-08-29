package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

/** Shared draw-replacement and resolution flow for cards with dredge. */
@Component
@RequiredArgsConstructor
public class DredgeSupport {

    private final GraveyardService graveyardService;
    private final ObjectProvider<PermanentRemovalService> permanentRemovalServiceProvider;
    private final GameLogService gameLogService;
    private final ObjectProvider<InputCompletionService> inputCompletionServiceProvider;
    private final ObjectProvider<DrawService> drawServiceProvider;

    public List<Integer> eligibleGraveyardIndices(GameData gameData, UUID playerId) {
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
        List<Card> library = gameData.playerDecks.getOrDefault(playerId, List.of());
        return IntStream.range(0, graveyard.size())
                .filter(index -> dredgeEffect(graveyard.get(index))
                        .map(effect -> effect.millCount() <= library.size())
                        .orElse(false))
                .boxed()
                .toList();
    }

    public void handleChoice(GameData gameData, Player player, int cardIndex) {
        UUID playerId = player.getId();
        if (cardIndex == -1) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " declines to dredge."));
            drawServiceProvider.getObject().resolveDrawCardWithoutStaticReplacementCheck(gameData, playerId);
            inputCompletionServiceProvider.getObject().processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, new ArrayList<>());
        Card card = graveyard.get(cardIndex);
        DredgeEffect effect = dredgeEffect(card)
                .orElseThrow(() -> new IllegalStateException("Selected card cannot dredge"));

        graveyardService.resolveMillPlayer(gameData, playerId, effect.millCount());
        permanentRemovalServiceProvider.getObject().removeCardFromGraveyardById(gameData, card.getId());
        permanentRemovalServiceProvider.getObject().addCardToHandFromGraveyard(
                gameData, playerId, playerId, card);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " returns ", card, " to hand with dredge."));

        inputCompletionServiceProvider.getObject().processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private Optional<DredgeEffect> dredgeEffect(Card card) {
        return card.getEffects(EffectSlot.GRAVEYARD_DRAW_REPLACEMENT).stream()
                .filter(DredgeEffect.class::isInstance)
                .map(DredgeEffect.class::cast)
                .findFirst();
    }
}
