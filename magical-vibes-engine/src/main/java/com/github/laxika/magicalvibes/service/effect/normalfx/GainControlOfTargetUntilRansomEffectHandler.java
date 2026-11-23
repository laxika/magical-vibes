package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetUntilRansomEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayRansomEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the playtest ransom control pattern used by Squidnapper: control of the target until the source leaves the
 * battlefield (not as it phases out, CR 702.26d), and a ransom ability on the creature that only the player it was
 * taken from may activate ("or that player pays the ransom").
 */
@Component
@RequiredArgsConstructor
public class GainControlOfTargetUntilRansomEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GainControlOfTargetEffectHandler gainControlOfTargetEffectHandler;
    private final GrantActivatedAbilityEffectHandler grantActivatedAbilityEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfTargetUntilRansomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var ransom = (GainControlOfTargetUntilRansomEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null
                || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
            return;
        }

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            if (gameQueryService.findPermanentById(gameData, targetId) == null) {
                continue;
            }
            UUID takenFromPlayerId = gameQueryService.findPermanentController(gameData, targetId);

            gainControlOfTargetEffectHandler.resolve(gameData, entry,
                    new GainControlOfTargetEffect(ransom.controlDuration()));

            ActivatedAbility ransomAbility = new ActivatedAbility(
                    false,
                    "{" + ransom.manaCost() + "}",
                    List.of(new PayLifeCost(ransom.lifeCost()),
                            new PayRansomEffect(sourcePermanentId)),
                    "{" + ransom.manaCost() + "} and " + ransom.lifeCost()
                            + " life: Pay this creature's ransom.")
                    .withActivatableOnlyByGrantingPlayer()
                    .withGrantingPlayer(takenFromPlayerId);
            grantActivatedAbilityEffectHandler.resolve(gameData, entry,
                    new GrantActivatedAbilityEffect(
                            ransomAbility,
                            Subjects.target(),
                            null,
                            ransom.controlDuration()));
        }
    }
}
