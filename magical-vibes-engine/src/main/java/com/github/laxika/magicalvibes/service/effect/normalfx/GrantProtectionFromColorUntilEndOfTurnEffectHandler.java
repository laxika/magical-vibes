package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantProtectionFromColorUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromColorUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantProtectionFromColorUntilEndOfTurnEffect) effect;
        if (e.scope() == GrantScope.OWN_CREATURES) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            if (battlefield == null) {
                return;
            }
            battlefield.stream()
                    .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                    .forEach(permanent -> grantProtection(permanent, e.color(), gameData));
            return;
        }

        Permanent target = resolveRecipient(gameData, entry, e);
        if (target == null) {
            return;
        }

        grantProtection(target, e.color(), gameData);
    }

    private void grantProtection(Permanent target, CardColor color, GameData gameData) {
        target.getProtectionFromColorsUntilEndOfTurn().add(color);

        String colorName = color.name().toLowerCase();
        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(" gains protection from " + colorName + " until end of turn.")
                .build());

        log.info("Game {} - {} gains protection from {} until end of turn", gameData.id, target.getCard().getName(), colorName);
    }

    private Permanent resolveRecipient(GameData gameData, StackEntry entry,
                                       GrantProtectionFromColorUntilEndOfTurnEffect e) {
        if (e.scope() == GrantScope.SELF) {
            UUID selfId = entry.getSourcePermanentId() != null
                    ? entry.getSourcePermanentId()
                    : entry.getTargetId();
            return selfId != null ? gameQueryService.findPermanentById(gameData, selfId) : null;
        }
        return gameQueryService.findPermanentById(gameData, entry.getTargetId());
    }
}
