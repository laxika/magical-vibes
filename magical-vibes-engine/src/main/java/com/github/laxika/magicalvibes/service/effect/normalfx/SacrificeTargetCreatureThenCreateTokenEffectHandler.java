package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetCreatureThenCreateTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link SacrificeTargetCreatureThenCreateTokenEffect} (Sarkhan the Mad): the target creature's controller
 * sacrifices it as the resolving step, then that player creates the token (CR 608.2c), whether or not the creature
 * could be sacrificed.
 */
@Component
@RequiredArgsConstructor
public class SacrificeTargetCreatureThenCreateTokenEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final GameQueryService gameQueryService;
    private final PermanentControlSupport permanentControlSupport;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetCreatureThenCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        UUID controllerId = gameData.findControllerOf(target);
        if (gameQueryService.canEffectCauseSacrifice(gameData, controllerId, entry.getControllerId())) {
            stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, target.getId(), this);
            return;
        }
        createToken(gameData, entry, effect, controllerId);
    }

    /** "Then that player creates …": the sacrificed creature's last controller. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        List<DepartureResult> departures = entry.getResolutionResults().departures();
        if (!departures.isEmpty()) {
            createToken(gameData, entry, effect, departures.getFirst().lastKnownControllerId());
        }
    }

    private void createToken(GameData gameData, StackEntry entry, CardEffect effect, UUID playerId) {
        if (playerId == null) {
            return;
        }
        permanentControlSupport.applyCreateToken(gameData, playerId,
                ((SacrificeTargetCreatureThenCreateTokenEffect) effect).tokenTemplate(), entry.getCard().getSetCode());
    }
}
