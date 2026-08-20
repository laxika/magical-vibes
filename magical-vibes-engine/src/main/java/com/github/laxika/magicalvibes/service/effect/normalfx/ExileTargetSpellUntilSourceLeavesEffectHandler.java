package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link ExileTargetSpellUntilSourceLeavesEffect} (Spell Queller).
 *
 * <p>The target spell is moved from the stack to exile. This is not countering, so "can't be
 * countered" does not protect the spell. The exiled card records the source permanent so the
 * source's leaves-the-battlefield trigger can find it again.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetSpellUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final StateTriggerService stateTriggerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetSpellUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry target = gameData.stack.stream()
                .filter(se -> se.getCard().getId().equals(targetCardId))
                .findFirst()
                .orElse(null);
        if (target == null) {
            log.info("Game {} - {}'s exile target is no longer on the stack", gameData.id, entry.getCard().getName());
            return;
        }

        gameData.stack.remove(target);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        if (target.isCopy()) {
            // A copy of a spell is not a card; it ceases to exist instead of being exiled.
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " (a copy) ceases to exist."));
            return;
        }

        UUID sourcePermanentId = findSourcePermanentId(gameData, entry);
    exileService.exileCard(gameData, target.getControllerId(), target.getPhysicalCard(), sourcePermanentId);

        gameLogService.append(gameData,
                GameLog.cardTextCard(target.getCard(), " is exiled by ", entry.getCard(), "."));
        log.info("Game {} - {} exiles the spell {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    /**
     * The permanent the ability came from, matched by card identity on its controller's
     * battlefield. Null when the source already left — the spell is still exiled, it simply has no
     * source to be given back on.
     */
    private UUID findSourcePermanentId(GameData gameData, StackEntry entry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) return null;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard() == entry.getCard()) {
                return permanent.getId();
            }
        }
        return null;
    }
}
