package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfOpponentLibraryAndBecomeCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardOfOpponentLibraryAndBecomeCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardOfOpponentLibraryAndBecomeCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID activePlayerId = entry.getTargetId();
        if (activePlayerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(activePlayerId);
        String playerName = gameData.playerIdToName.get(activePlayerId);
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty."));
            return;
        }

        Card topCard = library.getFirst();
        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " reveals ", topCard, " from the top of their library."));
        if (!topCard.hasType(CardType.CREATURE)) {
            return;
        }

        graveyardService.resolveMillPlayer(gameData, activePlayerId, 1);

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        String originalName = source.getCard().getName();
        permanentCopierService.applyCloneCopy(source, topCard, null, null, Set.of());
        gameLogService.append(gameData, GameLog.textCardText(originalName + " becomes a copy of ", topCard, "."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalName, topCard.getName());
    }
}
