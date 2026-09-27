package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomNonlandCardFromDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Rahilda's random nonland-library exile trigger. */
@Component
@RequiredArgsConstructor
public class ExileRandomNonlandCardFromDamagedPlayerLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomNonlandCardFromDamagedPlayerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        if (damagedPlayerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(damagedPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> candidates = library.stream()
                .filter(card -> !card.isToken())
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        Card chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        if (!library.remove(chosen)) {
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            exileService.exileCard(gameData, damagedPlayerId, chosen);
        } else {
            exileService.exileCard(gameData, damagedPlayerId, chosen, sourcePermanentId);
        }

        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(damagedPlayerId) + " exiles ")
                .card(chosen)
                .text(" at random from their library.")
                .build());
    }
}
