package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillUntilCreatureThenReanimateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MillUntilCreatureThenReanimateEffect} (Helm of Obedience). Mills the target player
 * until a creature card actually lands in their graveyard or X cards have been milled. Each mill event
 * may put several cards into the graveyard; the creature cards from the event that stops the loop are
 * the candidates, and the controller chooses one when there is more than one. A creature card that
 * never reaches the graveyard (a replacement moved it elsewhere) does not stop the loop.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MillUntilCreatureThenReanimateEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final SacrificeSelfEffectHandler sacrificeSelfEffectHandler;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillUntilCreatureThenReanimateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        int x = entry.getXValue();
        if (targetPlayerId == null || x <= 0) {
            return;
        }

        List<Card> creatureCards = millUntilCreatures(gameData, targetPlayerId, x);
        if (creatureCards.isEmpty()) {
            return;
        }

        sacrificeSelfEffectHandler.resolve(gameData, entry, new SacrificeSelfEffect());

        if (creatureCards.size() == 1) {
            Card creatureCard = creatureCards.getFirst();
            permanentRemovalService.removeCardFromGraveyardById(gameData, creatureCard.getId());
            graveyardReturnSupport.putCardOntoBattlefield(gameData, entry.getControllerId(), creatureCard);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(entry.getControllerId()) + " puts ",
                    creatureCard, " onto the battlefield under their control."));
            return;
        }

        List<Integer> indices = IntStream.range(0, creatureCards.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice.builder(
                        entry.getControllerId(),
                        indices,
                        GraveyardChoiceDestination.BATTLEFIELD,
                        entry.getCard().getName() + " — choose a creature card to put onto the battlefield.")
                .cardPool(new ArrayList<>(creatureCards))
                .mandatory(true)
                .build());
    }

    /**
     * Mills up to {@code max} cards, one mill event at a time, stopping at the first event that puts a
     * creature card into the target player's graveyard. Returns every creature card that event put there,
     * or an empty list if none was put there.
     */
    private List<Card> millUntilCreatures(GameData gameData, UUID targetPlayerId, int max) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        int cardsPutIntoGraveyard = 0;
        while (cardsPutIntoGraveyard < max) {
            if (deck == null || deck.isEmpty()) {
                return List.of();
            }
            List<Card> milled = graveyardService.resolveMillPlayer(gameData, targetPlayerId, 1);
            List<Card> creatures = milled.stream()
                    .filter(card -> card.hasType(CardType.CREATURE))
                    .filter(card -> gameData.playerGraveyards.get(targetPlayerId).stream()
                            .anyMatch(graveyardCard -> graveyardCard.getId().equals(card.getId())))
                    .toList();
            if (!creatures.isEmpty()) {
                return creatures;
            }
            cardsPutIntoGraveyard += milled.stream().filter(card -> !card.isToken()).count();
        }
        return List.of();
    }
}
