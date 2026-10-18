package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentReturnsCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves "choose an opponent. That player returns a card from their graveyard to their hand." */
@Component
@RequiredArgsConstructor
public class ChooseOpponentReturnsCardFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final TargetPlayerReturnsCardFromGraveyardToHandEffectHandler returnHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOpponentReturnsCardFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ChooseOpponentReturnsCardFromGraveyardToHandEffect) effect;
        List<UUID> opponents = opponentsOf(gameData, entry.getControllerId());
        if (opponents.isEmpty()) {
            return;
        }
        if (opponents.size() == 1) {
            returnHandler.returnCardFromGraveyard(gameData, entry.getCard(), opponents.getFirst(),
                    returnEffect.filter());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChooseOpponentReturnsCardFromGraveyard(
                        entry.getControllerId(), entry.getCard(), returnEffect.filter()));
        playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), opponents,
                entry.getCard().getName() + " — choose an opponent.");
    }

    public void completeChoice(GameData gameData, UUID chosenOpponentId,
                               PermanentChoiceContext.ChooseOpponentReturnsCardFromGraveyard context) {
        if (!opponentsOf(gameData, context.controllerId()).contains(chosenOpponentId)) {
            throw new IllegalStateException("Invalid opponent choice");
        }
        Card sourceCard = context.sourceCard();
        returnHandler.returnCardFromGraveyard(gameData, sourceCard, chosenOpponentId, context.filter());
    }

    private List<UUID> opponentsOf(GameData gameData, UUID controllerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }
}
