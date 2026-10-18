package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID chosenCardId = ((ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect) effect).chosenCardId();
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(controllerId, List.of());
        Card exiled = graveyard.stream()
                .filter(card -> card.getId().equals(chosenCardId))
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .findFirst()
                .orElse(null);
        if (exiled == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, exiled.getId());
        exileService.exileCard(gameData, controllerId, exiled);
        gameData.exileInsteadOfGraveyard.add(exiled.getId());
        gameData.queueDelayedAction(new ReboundAtNextUpkeep(controllerId, controllerId, exiled));

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles ").card(exiled)
                .text(" at random from their graveyard for a free cast at their next upkeep.")
                .build());
        log.info("Game {} - {} exiles {} at random from their graveyard for a free cast at next upkeep",
                gameData.id, playerName, exiled.getName());
    }
}
