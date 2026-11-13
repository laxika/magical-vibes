package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeGrantingEquipmentThenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a granted attack ability that sacrifices its granting Equipment before its payload. */
@Component
@RequiredArgsConstructor
public class SacrificeGrantingEquipmentThenEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeGrantingEquipmentThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID equipmentId = entry.requireGrantingPermanentId();

        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
        if (equipment == null
                || !entry.getControllerId().equals(
                        gameQueryService.findPermanentController(gameData, equipmentId))) {
            return;
        }

        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, equipmentId, this);
    }

    /** "If you do, …": the payload is a reflexive ability (CR 603.12) that triggers only on the sacrifice. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getResolutionResults().anySacrificed()) {
            beginReflexiveTrigger(gameData, entry, ((SacrificeGrantingEquipmentThenEffect) effect).thenEffect());
        }
    }

    private void beginReflexiveTrigger(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        // "When you do" is an ability of its own (CR 603.12): its target is chosen as it goes on the stack, and it
        // records what the resolving entry was about.
        triggeredAbilityStackService.putMade(gameData, ReflexiveAbilitySupport.record(gameData, entry,
                List.of(thenEffect), entry.getCard().getName() + "'s ability"));
    }
}
