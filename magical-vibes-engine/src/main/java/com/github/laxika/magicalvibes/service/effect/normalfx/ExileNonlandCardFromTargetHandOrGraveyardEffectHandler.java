package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNonlandCardFromTargetHandOrGraveyardEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileNonlandCardFromTargetHandOrGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileNonlandCardFromTargetHandOrGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileNonlandCardFromTargetHandOrGraveyardEffect exileEffect =
                (ExileNonlandCardFromTargetHandOrGraveyardEffect) effect;
        if (entry.getTargetId() == null) {
            return;
        }

        UUID targetPlayerId = entry.getTargetId();
        cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);

        List<Card> candidates = new ArrayList<>();
        addNonlands(candidates, gameData.playerHands.getOrDefault(targetPlayerId, List.of()));
        addNonlands(candidates, gameData.playerGraveyards.getOrDefault(targetPlayerId, List.of()));
        if (candidates.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice(
                        entry.getControllerId(), targetPlayerId,
                        candidates.stream().map(Card::getId).toList(),
                        exileEffect.grantPlayPermission()));
    }

    private static void addNonlands(List<Card> candidates, List<Card> cards) {
        cards.stream()
                .filter(card -> !card.hasType(com.github.laxika.magicalvibes.model.CardType.LAND))
                .forEach(candidates::add);
    }
}
