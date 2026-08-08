package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PlayerNextDamageRedirectShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectNextDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RedirectNextDamageEffect e = (RedirectNextDamageEffect) effect;
        UUID protectedId = resolveRole(gameData, entry, e.protectedRole());
        UUID destinationId = resolveRole(gameData, entry, e.destinationRole());
        // Without both ends of the redirection there is nothing to install.
        if (protectedId == null || destinationId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }

        Permanent protectedPermanent = resolvePermanent(gameData, protectedId);
        Permanent destinationPermanent = resolvePermanent(gameData, destinationId);
        // A permanent that has left the battlefield can neither be shielded nor receive the
        // redirected damage; the shield would be inert either way.
        if (protectedPermanent == null && !gameData.playerIds.contains(protectedId)) {
            return;
        }
        if (destinationPermanent == null && !gameData.playerIds.contains(destinationId)) {
            return;
        }

        if (protectedPermanent == null) {
            gameData.playerNextDamageRedirectShields.add(
                    new PlayerNextDamageRedirectShield(protectedId, amount, destinationId));
        } else {
            gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                    protectedId, null, amount, destinationId));
        }

        GameLog.Builder logEntry = GameLog.builder()
                .text("The next " + amount + " damage that would be dealt to ");
        appendActor(logEntry, gameData, protectedId, protectedPermanent);
        logEntry.text(" this turn is dealt to ");
        appendActor(logEntry, gameData, destinationId, destinationPermanent);
        gameLogService.append(gameData, logEntry.text(" instead.").build());
        log.info("Game {} - registered next-{}-damage redirect from {} to {}", gameData.id, amount,
                describe(gameData, protectedId, protectedPermanent),
                describe(gameData, destinationId, destinationPermanent));
    }

    private UUID resolveRole(GameData gameData, StackEntry entry, RedirectRole role) {
        return switch (role) {
            case SOURCE_PERMANENT -> entry.getSourcePermanentId();
            case TARGET -> entry.getTargetId();
            case CONTROLLER -> entry.getControllerId();
            case ENCHANTED_PERMANENT -> enchantedPermanentId(gameData, entry);
        };
    }

    /** The permanent the Aura source is attached to, or {@code null} when it is not attached. */
    private UUID enchantedPermanentId(GameData gameData, StackEntry entry) {
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return null;
        }
        Permanent aura = gameQueryService.findPermanentById(gameData, sourceId);
        return aura == null ? null : aura.getAttachedTo();
    }

    /** The permanent behind an id, or {@code null} when the id is a player's (or it has left play). */
    private Permanent resolvePermanent(GameData gameData, UUID id) {
        return gameData.playerIds.contains(id) ? null : gameQueryService.findPermanentById(gameData, id);
    }

    private void appendActor(GameLog.Builder logEntry, GameData gameData, UUID id, Permanent permanent) {
        if (permanent != null) {
            logEntry.card(permanent.getCard());
        } else {
            logEntry.text(gameData.playerIdToName.get(id));
        }
    }

    private String describe(GameData gameData, UUID id, Permanent permanent) {
        return permanent != null ? permanent.getCard().getName() : gameData.playerIdToName.get(id);
    }
}
