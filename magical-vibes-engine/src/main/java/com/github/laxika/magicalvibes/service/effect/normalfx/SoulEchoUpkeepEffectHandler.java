package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SoulEchoUpkeepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SoulEchoUpkeepEffect} (Soul Echo): "sacrifice this enchantment if there are no echo
 * counters on it. Otherwise, target opponent may choose that for each 1 damage that would be dealt to
 * you until your next upkeep, you remove an echo counter from this enchantment instead."
 *
 * <p>Any redirection granted at the previous upkeep expires first — that is the "until your next
 * upkeep" duration. Then, with no echo counters left the source is sacrificed; otherwise the targeted
 * opponent is prompted (the accept/decline branch lives in {@code SoulEchoDamageRedirectionHandler}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SoulEchoUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SoulEchoUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        // "until your next upkeep" — whatever the opponent chose last upkeep ends here.
        self.setEchoDamageRedirectionActive(false);

        if (self.getCounterCount(CounterType.ECHO) <= 0) {
            if (permanentRemovalService.removePermanentToGraveyard(gameData, self)) {
                triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, entry.getControllerId(), self.getCard());
                gameLogService.append(gameData, GameLog.cardThen(self.getCard(), " is sacrificed."));
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
            return;
        }

        UUID opponentId = entry.getTargetId();
        if (opponentId == null || !gameData.playerIds.contains(opponentId)) {
            return;
        }

        String prompt = "Choose that for each 1 damage that would be dealt to "
                + gameData.playerIdToName.get(entry.getControllerId())
                + " until their next upkeep, they remove an echo counter from "
                + self.getCard().getName() + " instead?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                self.getCard(), opponentId, List.of(effect), prompt, null, null, self.getId(), null));
        log.info("Game {} - {} offers the echo-counter choice to {}", gameData.id,
                self.getCard().getName(), gameData.playerIdToName.get(opponentId));
    }
}
