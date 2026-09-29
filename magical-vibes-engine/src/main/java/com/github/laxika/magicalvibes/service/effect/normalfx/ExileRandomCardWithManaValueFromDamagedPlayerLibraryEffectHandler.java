package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardWithManaValueFromDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Kamachal's random exact-mana-value library exile trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileRandomCardWithManaValueFromDamagedPlayerLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomCardWithManaValueFromDamagedPlayerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (damagedPlayerId == null || controllerId == null) {
            return;
        }

        int damageDealt = amountEvaluationService.evaluate(
                gameData,
                ((ExileRandomCardWithManaValueFromDamagedPlayerLibraryEffect) effect).combatDamageAmount(),
                AmountContext.forStackEntry(entry, null));
        List<Card> library = gameData.playerDecks.get(damagedPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> candidates = library.stream()
                .filter(card -> card.getManaValue() == damageDealt)
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        Card chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        if (!library.removeIf(card -> card.getId().equals(chosen.getId()))) {
            return;
        }

        exileService.exileCard(gameData, damagedPlayerId, chosen);
        gameData.exilePlayPermissions.put(chosen.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(chosen.getId());

        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(damagedPlayerId) + " exiles ")
                .card(chosen)
                .text(" at random from their library; "
                        + gameData.playerIdToName.get(controllerId) + " may cast it this turn.")
                .build());
        log.info("Game {} - {} exiles {} at random from {}'s library; {} may cast it this turn",
                gameData.id,
                gameData.playerIdToName.get(damagedPlayerId),
                chosen.getName(),
                gameData.playerIdToName.get(damagedPlayerId),
                gameData.playerIdToName.get(controllerId));
    }
}
