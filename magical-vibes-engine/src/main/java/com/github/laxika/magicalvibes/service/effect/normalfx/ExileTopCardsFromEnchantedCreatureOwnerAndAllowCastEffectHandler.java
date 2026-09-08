package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect e =
                (ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect) effect;
        if (e.count() <= 0 || e.libraryOwnerId() == null || entry.getSourcePermanentSnapshot() == null) {
            return;
        }

        UUID auraId = entry.getSourcePermanentSnapshot().getId();
        List<Card> library = gameData.playerDecks.get(e.libraryOwnerId());
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> exiled = new ArrayList<>();
        for (int i = 0; i < e.count() && !library.isEmpty(); i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, e.libraryOwnerId(), card, auraId);
            exiled.add(card);

            if (!card.hasType(CardType.LAND)) {
                gameData.exilePlayPermissions.put(card.getId(), entry.getControllerId());
                gameData.exilePlayAnyManaTypeWhileExiled.add(card.getId());
            }
        }

        GameLog.Builder logEntry = GameLog.builder().text("Exiles ");
        for (int i = 0; i < exiled.size(); i++) {
            if (i > 0) {
                logEntry.text(", ");
            }
            logEntry.card(exiled.get(i));
        }
        gameLogService.append(gameData, logEntry.text(" from the top of the creature owner's library.").build());
        log.info("Game {} - {} exiles {} cards from the enchanted creature owner's library",
                gameData.id, entry.getCard().getName(), exiled.size());
    }
}
