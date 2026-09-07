package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves triggered mana that survives step and phase transitions until end of turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardPersistentManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardPersistentManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardPersistentManaEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }

        UUID recipientId = switch (e.recipient()) {
            case TARGET_PLAYER -> entry.getTargetId() != null
                    ? entry.getTargetId() : entry.getControllerId();
            case ENCHANTED_PERMANENT_CONTROLLER -> entry.getTargetId() != null
                    ? entry.getTargetId()
                    : source != null && source.getAttachedTo() != null
                    ? gameQueryService.findPermanentController(gameData, source.getAttachedTo())
                    : entry.getControllerId();
            case CONTROLLER -> entry.getControllerId();
        };
        ManaPool pool = gameData.playerManaPools.get(recipientId);
        pool.addPersistentMana(e.color(), amount);

        String playerName = gameData.playerIdToName.get(recipientId);
        gameLogService.append(gameData, GameLog.text(
                playerName + " adds " + amount + " " + e.color().getCode() + "."));
        log.info("Game {} - {} adds {} {} that persists until end of turn",
                gameData.id, playerName, amount, e.color());
    }
}
