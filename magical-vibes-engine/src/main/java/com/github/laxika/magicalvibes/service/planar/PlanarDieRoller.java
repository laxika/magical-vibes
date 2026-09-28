package com.github.laxika.magicalvibes.service.planar;

import com.github.laxika.magicalvibes.model.planar.PlanarDieResult;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RollPlanarDieWithAdvantageEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PlanarDieRoller {

    public PlanarDieResult roll() {
        return switch (ThreadLocalRandom.current().nextInt(6)) {
            case 4 -> PlanarDieResult.CHAOS;
            case 5 -> PlanarDieResult.PLANESWALKER;
            default -> PlanarDieResult.BLANK;
        };
    }

    /** Returns the number of planar dice to roll for this player after static replacements. */
    public int numberOfRolls(GameData gameData, UUID rollingPlayerId) {
        int count = 1;
        List<Permanent> battlefield = gameData == null || rollingPlayerId == null
                ? List.of() : gameData.playerBattlefields.getOrDefault(rollingPlayerId, List.of());
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof RollPlanarDieWithAdvantageEffect) {
                    count++;
                }
            }
        }
        return count;
    }
}
