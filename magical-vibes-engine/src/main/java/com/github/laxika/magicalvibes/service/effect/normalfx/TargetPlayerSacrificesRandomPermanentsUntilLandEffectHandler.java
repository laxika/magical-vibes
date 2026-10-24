package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesRandomPermanentsUntilLandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetPlayerSacrificesRandomPermanentsUntilLandEffect}: the targeted player sacrifices a
 * uniformly-random permanent they control, and if a nonland permanent was sacrificed this way the process repeats
 * (Tyrant of Discord).
 *
 * <p>Each sacrifice is one resolution step ({@link StepDepartureSupport#sacrificeThenContinue}), so a CR 616.1
 * question about the sacrificed permanent ends the step before it leaves. Once it has left, the process repeats by
 * inserting this effect again as the next step, reading what was sacrificed from the resolution scratchpad. It ends
 * when a land was sacrificed, nothing was sacrificed, or the player controls no permanents.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TargetPlayerSacrificesRandomPermanentsUntilLandEffectHandler
        implements NormalEffectHandlerBean, ContinuesAfterDeparture {

    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSacrificesRandomPermanentsUntilLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            log.info("Game {} - {} fizzles (no valid target player)", gameData.id, entry.getCard().getName());
            return;
        }

        if (!gameQueryService.canEffectCauseSacrifice(gameData, targetPlayerId, entry.getControllerId())) {
            log.info("Game {} - {} can't be made to sacrifice by {}", gameData.id,
                    gameData.playerIdToName.get(targetPlayerId), entry.getCard().getName());
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }
        Permanent chosen = battlefield.get(ThreadLocalRandom.current().nextInt(battlefield.size()));
        log.info("Game {} - {} sacrifices {} at random to {}", gameData.id,
                gameData.playerIdToName.get(targetPlayerId), chosen.getCard().getName(), entry.getCard().getName());
        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, chosen.getId(), this);
    }

    /** Repeats the process as the next step if a nonland permanent was sacrificed this way. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        boolean nonlandSacrificed = entry.getResolutionResults().departures().stream()
                .anyMatch(result -> result.sacrificed() && isNonland(result));
        if (nonlandSacrificed) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(effect));
        }
    }

    private static boolean isNonland(DepartureResult result) {
        return !result.lastKnownCardTypes().contains(CardType.LAND);
    }
}
