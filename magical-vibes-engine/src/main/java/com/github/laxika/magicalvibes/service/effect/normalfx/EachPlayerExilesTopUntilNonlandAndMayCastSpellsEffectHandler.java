package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Etali's enter-the-battlefield library dig and free-cast choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect exileEffect =
                (EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect) effect;
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        List<UUID> castableSpellIds = new ArrayList<>();
        List<UUID> nonlandCardIds = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> library = gameData.playerDecks.get(playerId);
            while (library != null && !library.isEmpty()) {
                Card card = library.removeFirst();
                gameData.addToExile(playerId, card);
                gameLogService.append(gameData, GameLog.builder()
                        .text(gameData.playerIdToName.get(playerId) + " exiles ")
                        .card(card)
                        .text(" from the top of their library (" + sourceName + ").")
                        .build());

                if (!card.hasType(CardType.LAND)) {
                    nonlandCardIds.add(card.getId());
                    if (isSpell(card)) {
                        castableSpellIds.add(card.getId());
                    }
                    break;
                }
            }
        }

        if (exileEffect.opponentChoosesCard()) {
            if (nonlandCardIds.isEmpty()) {
                return;
            }
            List<UUID> opponentIds = gameData.orderedPlayerIds.stream()
                    .filter(playerId -> !playerId.equals(controllerId))
                    .toList();
            if (opponentIds.size() == 1) {
                beginOpponentCardChoice(gameData, controllerId, opponentIds.getFirst(), nonlandCardIds,
                        exileEffect.maxCastCount());
            } else {
                interactionHandlerRegistry.begin(gameData,
                        new PendingInteraction.PlarggAndNassariOpponentChoice(
                                controllerId, opponentIds, nonlandCardIds, exileEffect.maxCastCount()));
            }
            return;
        }

        if (castableSpellIds.isEmpty()) {
            log.info("Game {} - {} found no castable spells among the exiled cards",
                    gameData.id, sourceName);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        controllerId, castableSpellIds,
                        Math.min(exileEffect.maxCastCount(), castableSpellIds.size())));
        log.info("Game {} - {} awaiting cast choices for {} exiled spells",
                gameData.id, sourceName, castableSpellIds.size());
    }

    private void beginOpponentCardChoice(GameData gameData, UUID controllerId, UUID opponentId,
                                         List<UUID> nonlandCardIds, int maxCastCount) {
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.PlarggAndNassariCardChoice(
                        opponentId, controllerId, nonlandCardIds, maxCastCount));
    }

    private static boolean isSpell(Card card) {
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        return card.getType().isPermanentType() && !card.hasType(CardType.LAND);
    }
}
