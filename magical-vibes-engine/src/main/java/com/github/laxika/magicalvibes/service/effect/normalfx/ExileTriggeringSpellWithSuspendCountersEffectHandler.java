package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithSuspendCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTriggeringSpellWithSuspendCountersEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final StateTriggerService stateTriggerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringSpellWithSuspendCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTriggeringSpellWithSuspendCountersEffect suspendEffect =
                (ExileTriggeringSpellWithSuspendCountersEffect) effect;
        if (suspendEffect.counters() <= 0) return;

        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) return;

        StackEntry spell = gameData.stack.stream()
                .filter(candidate -> candidate != entry)
                .filter(candidate -> triggeringCardId.equals(candidate.getCard().getId()))
                .filter(candidate -> !candidate.isCopy())
                .findFirst()
                .orElse(null);
        if (spell == null) return;

        Card card = spell.getCard();
        UUID ownerId = card.getOwnerId() != null ? card.getOwnerId() : spell.getControllerId();
        gameData.stack.remove(spell);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, spell);
        exileService.exileCard(gameData, ownerId, card);
        gameData.suspendedSpellExiles.add(new GameData.SuspendedSpellExile(
                card.getId(), ownerId, suspendEffect.counters()));

        gameLogService.append(gameData, GameLog.cardThen(card,
                " is exiled with " + suspendEffect.counters() + " time counters and gains suspend."));
        log.info("Game {} - {} exiled with {} suspend counters", gameData.id,
                card.getName(), suspendEffect.counters());
    }
}
