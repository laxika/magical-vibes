package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final VaanExileCastSupport vaanExileCastSupport;

    public ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffectHandler(
            ExileService exileService,
            GameLogService gameLogService,
            @Lazy PlayerInputService playerInputService,
            VaanExileCastSupport vaanExileCastSupport) {
        this.exileService = exileService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.vaanExileCastSupport = vaanExileCastSupport;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (damagedPlayerId == null) {
            return;
        }
        List<Card> library = gameData.playerDecks.get(damagedPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.removeFirst();
        exileService.exileCard(gameData, damagedPlayerId, topCard);
        gameLogService.append(gameData, GameLog.cardThen(topCard, " is exiled from the top of the library."));
        if (topCard.hasType(CardType.LAND)) {
            vaanExileCastSupport.createTreasureDuringResolution(
                    gameData, controllerId, entry.getCard(), entry.getSourcePermanentId());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(effect),
                "Cast " + topCard.getName() + " for its mana cost?",
                topCard.getId(),
                null,
                entry.getSourcePermanentId()
        ));
        log.info("Game {} - {} exiles {} from the damaged player's library for a may-cast choice",
                gameData.id, gameData.playerIdToName.get(controllerId), topCard.getName());
        playerInputService.processNextMayAbility(gameData);
    }
}
