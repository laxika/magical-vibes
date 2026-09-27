package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCreatureCardFromDamagedPlayerLibraryAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Fear of Ridicule's random creature-card exile and token-copy trigger. */
@Component
@RequiredArgsConstructor
public class ExileRandomCreatureCardFromDamagedPlayerLibraryAndCreateTokenCopyEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomCreatureCardFromDamagedPlayerLibraryAndCreateTokenCopyEffect.class;
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

        List<Card> creatureCards = library.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .toList();
        if (creatureCards.isEmpty()) {
            return;
        }

        Card chosen = creatureCards.get(ThreadLocalRandom.current().nextInt(creatureCards.size()));
        if (!library.removeIf(card -> card.getId().equals(chosen.getId()))) {
            return;
        }

        exileService.exileCard(gameData, damagedPlayerId, chosen);
        if (gameData.findExiledCard(chosen.getId()) == null) {
            return;
        }

        tokenCopySupport.createTokenCopies(
                gameData, entry, List.of(chosen), null, entry.getControllerId(),
                ((ExileRandomCreatureCardFromDamagedPlayerLibraryAndCreateTokenCopyEffect) effect)
                        .tokenCopyEffect());
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(entry.getControllerId()) + " exiles ")
                .card(chosen)
                .text(" at random from " + gameData.playerIdToName.get(damagedPlayerId)
                        + "'s library and creates a token copy.")
                .build());
    }
}
