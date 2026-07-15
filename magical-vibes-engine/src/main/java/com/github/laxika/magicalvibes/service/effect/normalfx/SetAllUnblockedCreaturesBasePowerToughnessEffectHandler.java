package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.SetAllUnblockedCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetAllUnblockedCreaturesBasePowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetAllUnblockedCreaturesBasePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetAllUnblockedCreaturesBasePowerToughnessEffect) effect;
        int power = e.power();
        int toughness = e.toughness();

        int count = 0;
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (!permanent.isAttacking() || isBlocked(gameData, permanent)) {
                    continue;
                }
                // CR 613 layer engine: one floating layer-7b effect per affected creature locks the
                // set at resolution time (CR 611.2c) and carries the resolution timestamp; the
                // legacy fields stay written for direct Permanent readers.
                permanent.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
                permanent.setBasePowerOverride(power);
                permanent.setBaseToughnessOverride(toughness);
                gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                        entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(),
                        new SetBasePowerToughnessEffect(power, toughness), permanent.getId(), null, null,
                        EffectDuration.UNTIL_END_OF_TURN, 0));
                count++;
            }
        }

        String logEntry = entry.getCard().getName() + " sets base power and toughness of " + count
                + " unblocked creature(s) to " + power + "/" + toughness + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} sets base P/T of {} unblocked creature(s) to {}/{}",
                gameData.id, entry.getCard().getName(), count, power, toughness);
    }

    /** An attacking creature is blocked if any permanent references its id as a blocking target. */
    private boolean isBlocked(GameData gameData, Permanent attacker) {
        for (List<Permanent> bf : gameData.playerBattlefields.values()) {
            for (Permanent blocker : bf) {
                if (blocker.isBlocking() && blocker.getBlockingTargetIds().contains(attacker.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
}
