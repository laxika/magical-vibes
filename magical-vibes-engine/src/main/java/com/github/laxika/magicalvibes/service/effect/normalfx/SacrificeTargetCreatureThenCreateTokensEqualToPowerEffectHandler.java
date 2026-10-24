package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect}: the target creature's
 * controller sacrifices it as the resolving step, then that same player creates one token per point of the
 * sacrificed creature's power (Mercy Killing). A creature that can't be sacrificed stays on the battlefield, but
 * its controller still creates the tokens.
 *
 * <p>The power is the creature's last-known power (CR 608.2h), read from the departure once it has left, or its
 * current power while it is still on the battlefield.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeTargetCreatureThenCreateTokensEqualToPowerEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final GameQueryService gameQueryService;
    private final PermanentControlSupport permanentControlSupport;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            log.info("Game {} - {} fizzles (no legal creature target)", gameData.id, entry.getCard().getName());
            return;
        }

        if (gameQueryService.cantBeSacrificed(gameData, target)) {
            log.info("Game {} - {} can't be sacrificed, so it stays on the battlefield", gameData.id,
                    target.getCard().getName());
            createTokens(gameData, entry, effect, gameData.findControllerOf(target),
                    gameQueryService.getEffectivePower(gameData, target));
            return;
        }
        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, target.getId(), this);
    }

    /** "Then creates X tokens, where X is that creature's power": its last controller and last-known power. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        List<DepartureResult> departures = entry.getResolutionResults().departures();
        if (departures.isEmpty()) {
            return;
        }
        DepartureResult result = departures.getFirst();
        Integer power = result.lastKnownPower();
        if (power == null) {
            Permanent stayed = gameQueryService.findPermanentById(gameData, result.permanentId());
            power = stayed == null ? 0 : gameQueryService.getEffectivePower(gameData, stayed);
        }
        createTokens(gameData, entry, effect, result.lastKnownControllerId(), power);
    }

    private void createTokens(GameData gameData, StackEntry entry, CardEffect effect, UUID playerId, int power) {
        if (playerId == null || power <= 0) {
            return;
        }
        permanentControlSupport.applyCreateToken(gameData, playerId,
                ((SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect) effect).tokenTemplate(), power,
                entry.getCard().getSetCode());
    }
}
