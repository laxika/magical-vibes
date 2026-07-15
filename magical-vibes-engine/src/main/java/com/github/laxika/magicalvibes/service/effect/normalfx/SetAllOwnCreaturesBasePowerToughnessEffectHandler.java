package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetAllOwnCreaturesBasePowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetAllOwnCreaturesBasePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetAllOwnCreaturesBasePowerToughnessEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        AmountContext ctx = AmountContext.forStackEntry(entry, source);
        int power = amountEvaluationService.evaluate(gameData, e.power(), ctx);
        int toughness = amountEvaluationService.evaluate(gameData, e.toughness(), ctx);

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            // CR 613 layer engine: one floating layer-7b effect per affected creature locks the
            // set at resolution time (CR 611.2c) and carries the resolution timestamp; the
            // legacy fields stay written for direct Permanent readers.
            permanent.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
            permanent.setBasePowerOverride(power);
            permanent.setBaseToughnessOverride(toughness);
            gameData.addFloatingEffect(new FloatingContinuousEffect(java.util.UUID.randomUUID(),
                    entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(),
                    new SetBasePowerToughnessEffect(power, toughness), permanent.getId(), null, null,
                    EffectDuration.UNTIL_END_OF_TURN, 0));
            count++;
        }

        String logEntry = entry.getCard().getName() + " sets base power and toughness of " + count
                + " creature(s) you control to " + power + "/" + toughness + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} sets base P/T of {} own creature(s) to {}/{}",
                gameData.id, entry.getCard().getName(), count, power, toughness);
    }
}
