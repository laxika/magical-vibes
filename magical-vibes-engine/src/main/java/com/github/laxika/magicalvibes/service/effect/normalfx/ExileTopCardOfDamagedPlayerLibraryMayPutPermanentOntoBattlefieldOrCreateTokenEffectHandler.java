package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Fishing Gear's combat-damage library exile and Fish fallback. */
@Component
@RequiredArgsConstructor
public class ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenEffectHandler
        implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typed = (ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenEffect) effect;
        UUID damagedPlayerId = entry.getTargetId();
        if (damagedPlayerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(damagedPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.removeFirst();
        exileService.exileCard(gameData, damagedPlayerId, topCard);
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(damagedPlayerId) + " exiles ")
                .card(topCard)
                .text(" from the top of their library (" + entry.getCard().getName() + ").")
                .build());

        if (!isPermanentCard(topCard)) {
            createTokenEffectHandler.resolve(gameData, entry, typed.fallbackToken());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(typed),
                entry.getCard().getName() + " — Put " + topCard.getName()
                        + " onto the battlefield under your control?",
                topCard.getId(),
                null,
                entry.getSourcePermanentId()));
    }

    private boolean isPermanentCard(Card card) {
        if (card.getType() != null && card.getType().isPermanentType()
                && card.getType() != CardType.KINDRED) {
            return true;
        }
        return card.getAdditionalTypes().stream()
                .anyMatch(type -> type.isPermanentType() && type != CardType.KINDRED);
    }
}
