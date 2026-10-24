package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEquippedCreatureThenDrawPowerEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a triggered ability that may sacrifice an Equipment's equipped creature for a draw. */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeEquippedCreatureThenDrawPowerEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeEquippedCreatureThenDrawPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent equipmentSnapshot = entry.getSourcePermanentSnapshot();
        UUID equippedId = equipmentSnapshot != null && equipmentSnapshot.getAttachedTo() != null
                ? equipmentSnapshot.getAttachedTo()
                : equipment == null ? null : equipment.getAttachedTo();
        if (equippedId == null) {
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, equippedId);
        if (creature == null) {
            return;
        }

        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (!entry.getControllerId().equals(creatureControllerId)
                || gameQueryService.cantBeSacrificed(gameData, creature)) {
            return;
        }

        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, creature.getId(), this);
    }

    /** "If you do, draw cards equal to its power": its last-known power (CR 608.2h). */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        if (!entry.getResolutionResults().anySacrificed()) {
            return;
        }
        int power = entry.getResolutionResults().lastKnownPower();
        for (int i = 0; i < power; i++) {
            drawService.resolveDrawCard(gameData, entry.getControllerId());
        }
        log.info("Game {} - {} draws {} cards for the sacrificed creature's power",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), power);
    }
}
