package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenCombatOpponentMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link BoostSelfWhenCombatOpponentMatchesEffect}: at resolution the source creature's combat
 * opponents (creatures it is blocking + creatures blocking it) are gathered and, if at least one matches
 * the effect's filter, the source gets +X/+Y until end of turn. Blocking relationships are still intact
 * during the declare-blockers step, when these triggers resolve.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoostSelfWhenCombatOpponentMatchesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfWhenCombatOpponentMatchesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostSelfWhenCombatOpponentMatchesEffect) effect;
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        boolean anyMatch = collectCombatOpponents(gameData, self).stream()
                .anyMatch(opponent -> predicateEvaluationService.matchesPermanentPredicate(gameData, opponent, boost.opponentFilter()));
        if (!anyMatch) {
            return;
        }

        self.setPowerModifier(self.getPowerModifier() + boost.powerBoost());
        self.setToughnessModifier(self.getToughnessModifier() + boost.toughnessBoost());

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .card(self.getCard())
                .text(String.format(" gets %+d/%+d until end of turn.", boost.powerBoost(), boost.toughnessBoost()))
                .build());
        log.info("Game {} - {} gets {}/{} from combat-opponent trigger", gameData.id,
                self.getCard().getName(), boost.powerBoost(), boost.toughnessBoost());
    }

    /**
     * Every creature currently in combat opposite {@code self}: the creatures {@code self} blocks (when it
     * is a blocker) plus the creatures blocking {@code self} (when it is the blocked attacker).
     */
    private List<Permanent> collectCombatOpponents(GameData gameData, Permanent self) {
        List<Permanent> opponents = new ArrayList<>();
        for (UUID blockedId : self.getBlockingTargetIds()) {
            Permanent blocked = gameQueryService.findPermanentById(gameData, blockedId);
            if (blocked != null) {
                opponents.add(blocked);
            }
        }
        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.isBlocking() && perm.getBlockingTargetIds().contains(self.getId())) {
                opponents.add(perm);
            }
        });
        return opponents;
    }
}
