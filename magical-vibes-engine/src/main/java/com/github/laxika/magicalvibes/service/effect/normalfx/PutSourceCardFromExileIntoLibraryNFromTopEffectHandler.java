package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromExileIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutSourceCardFromExileIntoLibraryNFromTopEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutSourceCardFromExileIntoLibraryNFromTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int position = ((PutSourceCardFromExileIntoLibraryNFromTopEffect) effect).position();
        UUID cardId = entry.getCard().getId();
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in exile)."));
            log.info("Game {} - {} tuck-from-exile trigger fizzles (card {} not in exile)",
                    gameData.id, entry.getCard().getName(), cardId);
            return;
        }

        Card sourceCard = exiled.card();
        UUID ownerId = exiled.ownerId();
        if (!gameData.removeFromExile(cardId)) {
            return;
        }
        List<Card> library = gameData.playerDecks.get(ownerId);
        library.add(Math.min(position, library.size()), sourceCard);

        String ownerName = gameData.playerIdToName.get(ownerId);
        String ordinal = switch (position) {
            case 0 -> "on top of";
            case 1 -> "second from the top of";
            case 2 -> "third from the top of";
            default -> (position + 1) + "th from the top of";
        };
        gameLogService.append(gameData,
                GameLog.builder().card(sourceCard)
                        .text(" is put " + ordinal + " " + ownerName + "'s library.").build());
        log.info("Game {} - {} put {} {}'s library from exile (position {})",
                gameData.id, sourceCard.getName(), ordinal, ownerName, position);
    }
}
