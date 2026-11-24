package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RollWithAdvantageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Produces random results for ordinary dice. */
@Component
public class DiceRollService {

    @Autowired
    private GameQueryService gameQueryService;

    public int roll(int sides) {
        if (sides < 1) {
            throw new IllegalArgumentException("A die must have at least one side");
        }
        return ThreadLocalRandom.current().nextInt(sides) + 1;
    }

    /**
     * Makes one roll of {@code count} dice with {@code sides} sides each. Each advantage replacement effect
     * ({@link RollWithAdvantageEffect}: "roll that many dice plus one and ignore the lowest roll") the player controls
     * adds one die to the whole roll, and that many lowest results are ignored, as if never rolled (CR 706.6). The
     * returned list keeps {@code count} results in the order they were rolled.
     */
    public List<Integer> rollDice(GameData gameData, UUID rollingPlayerId, int sides, int count) {
        int extraDice = countAdvantageEffects(gameData, rollingPlayerId);
        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < count + extraDice; i++) {
            results.add(roll(sides));
        }
        for (int i = 0; i < extraDice; i++) {
            results.remove(results.indexOf(Collections.min(results)));
        }
        return results;
    }

    private int countAdvantageEffects(GameData gameData, UUID rollingPlayerId) {
        int advantageCount = 0;
        List<Permanent> battlefield = gameData == null || rollingPlayerId == null
                ? null : gameData.playerBattlefields.get(rollingPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService != null && gameQueryService.hasLostAllAbilities(gameData, permanent)) continue;
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                    if (effect instanceof RollWithAdvantageEffect) {
                        advantageCount++;
                    }
                }
            }
        }
        return advantageCount;
    }
}
