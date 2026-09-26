package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetUntilRansomEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayRansomEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the playtest ransom control pattern used by Squidnapper. */
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

            gainControlOfTargetEffectHandler.resolve(gameData, entry,
                    new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_REMAINS));

            ActivatedAbility ransomAbility = new ActivatedAbility(
                    false,
                    "{" + ransom.manaCost() + "}",
                    List.of(new PayLifeCost(ransom.lifeCost()),
                            new PayRansomEffect(sourcePermanentId)),
                    "{" + ransom.manaCost() + "} and " + ransom.lifeCost()
                            + " life: Pay this creature's ransom.")
                    .withActivatableByAnyPlayer()
                    .withActivatableOnlyByOwner();
            grantActivatedAbilityEffectHandler.resolve(gameData, entry,
                    new GrantActivatedAbilityEffect(
                            ransomAbility,
                            GrantScope.TARGET,
                            null,
                            EffectDuration.WHILE_SOURCE_REMAINS));
        }
    }
}
