package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfMemoryCounterExiledCreaturesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopiesOfMemoryCounterExiledCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfMemoryCounterExiledCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> sourceCards = gameData.getPlayerExiledCards(entry.getControllerId()).stream()
                .filter(card -> gameData.exiledCardsWithMemoryCounters.contains(card.getId()))
                .filter(card -> !card.isToken() && card.hasType(CardType.CREATURE))
                .toList();
        if (sourceCards.isEmpty()) {
            return;
        }

        PermanentChoiceContext.CreateMemoryCounterTokenCopiesAttacking context =
                new PermanentChoiceContext.CreateMemoryCounterTokenCopiesAttacking(
                        entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(),
                        sourceCards, 0, List.of());
        gameData.interaction.setPermanentChoiceContext(context);

        List<UUID> opponentIds = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        List<UUID> planeswalkerIds = opponentIds.stream()
                .flatMap(opponentId -> gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream())
                .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        playerInputService.beginAnyTargetChoice(
                gameData, entry.getControllerId(), planeswalkerIds, opponentIds,
                "Choose the player or planeswalker for the next token to attack.");
    }
}
