package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SourceFightsTargetCreatureEffect}. A fight goes through {@link FightSupport#fight} (CR 701.14a):
 * both creatures must be on the battlefield (CR 701.14b), each deals damage as itself, and both are recorded as having
 * fought. The not-a-fight form ("it deals damage equal to its power to target creature. That creature deals damage
 * equal to its power to it") lets a source that left deal its damage as it last existed and takes none.
 */
@Component
@RequiredArgsConstructor
public class SourceFightsTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final FightSupport fightSupport;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SourceFightsTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SourceFightsTargetCreatureEffect fight = (SourceFightsTargetCreatureEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            targetId = entry.targetsForEffect(effect).stream().findFirst().orElse(null);
        }
        if (targetId == null) return;

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) return;

        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId != null
                ? gameQueryService.findPermanentById(gameData, sourcePermanentId) : null;
        if (fight.requiresSourceOnBattlefield()) {
            if (source == null || !gameQueryService.isCreature(gameData, source)
                    || !gameQueryService.isCreature(gameData, target)) {
                return;
            }
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " fights ", target.getCard(), "."));
            fightSupport.fight(gameData, entry, source, target);
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }
        Permanent sourceForPower = source != null ? source : entry.getSourcePermanentSnapshot();
        if (sourceForPower == null
                || (source != null && !gameQueryService.isCreature(gameData, source))
                || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        String cardName = FaceDownVisibility.sourceName(gameData, entry);

        int sourcePower = gameQueryService.getPowerBasedDamage(gameData, sourceForPower);
        int targetPower = gameQueryService.getPowerBasedDamage(gameData, target);

        damageSupport.dealCreatureDamage(gameData, entry, target, sourcePower);

        if (source == null) {
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        damageSupport.dealCreatureDamage(gameData, entry, source, targetPower, target);

        gameOutcomeService.checkWinCondition(gameData);
    }
}
