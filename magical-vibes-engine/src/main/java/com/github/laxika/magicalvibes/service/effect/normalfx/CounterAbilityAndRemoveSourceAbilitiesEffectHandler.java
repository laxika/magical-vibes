package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterAbilityAndRemoveSourceAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link CounterAbilityAndRemoveSourceAbilitiesEffect}. The source permanent's current
 * card types are checked after the ability is countered, and the ability-removing effect is tied
 * to the resolving permanent so it expires when that permanent leaves the battlefield.
 */
@Component
@RequiredArgsConstructor
public class CounterAbilityAndRemoveSourceAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterAbilityAndRemoveSourceAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetId, entry);
        if (targetEntry == null) {
            return;
        }

        UUID targetSourcePermanentId = targetEntry.getSourcePermanentId();
        if (!counterSupport.counterSpell(gameData, entry, targetEntry)
                || targetSourcePermanentId == null) {
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null
                || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
            return;
        }

        Permanent targetSource = gameQueryService.findPermanentById(gameData, targetSourcePermanentId);
        if (targetSource == null || (!gameQueryService.isArtifact(gameData, targetSource)
                && !gameQueryService.isCreature(gameData, targetSource)
                && !gameQueryService.isPlaneswalker(gameData, targetSource))) {
            return;
        }

        LosesAllAbilitiesEffect loseAbilities = new LosesAllAbilitiesEffect(
                GrantScope.TARGET, EffectDuration.WHILE_SOURCE_REMAINS);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), sourcePermanentId,
                entry.getControllerId(), loseAbilities, targetSourcePermanentId, null, null,
                loseAbilities.duration(), 0));
        gameLogService.append(gameData, GameLog.cardThen(targetSource.getCard(),
                " loses all abilities for as long as " + entry.getCard().getName()
                        + " remains on the battlefield."));
    }
}
