package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromGraveyardAndMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a random graveyard instant-or-sorcery exile followed by an immediate free-cast offer. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileRandomInstantOrSorceryFromGraveyardAndMayCastFreeEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomInstantOrSorceryFromGraveyardAndMayCastFreeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileRandomInstantOrSorceryFromGraveyardAndMayCastFreeEffect exileEffect =
                (ExileRandomInstantOrSorceryFromGraveyardAndMayCastFreeEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> candidates = gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                        && card.getManaValue() <= exileEffect.maxManaValue())
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        Card exiled = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, exiled.getId());
        exileService.exileCard(gameData, controllerId, exiled);
        gameData.exileInsteadOfGraveyard.add(exiled.getId());
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                "Cast " + exiled.getName() + " without paying its mana cost?",
                exiled.getId()));

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles ").card(exiled)
                .text(" at random from their graveyard and may cast it without paying its mana cost.")
                .build());
        log.info("Game {} - {} may cast {} exiled at random from their graveyard without paying its mana cost",
                gameData.id, playerName, exiled.getName());
    }
}
