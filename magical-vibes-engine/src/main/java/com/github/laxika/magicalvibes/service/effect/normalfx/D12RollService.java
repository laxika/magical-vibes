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

/** Produces a random twelve-sided die result. */
@Component
public class D12RollService {

    @Autowired
    private GameQueryService gameQueryService;

    public int roll() {
        return ThreadLocalRandom.current().nextInt(1, 13);
    }

    /** Rolls a d12 while applying each advantage replacement effect controlled by the player. */
    public int roll(GameData gameData, UUID rollingPlayerId) {
        return rollDice(gameData, rollingPlayerId, 1).getFirst();
    }

    /**
     * Makes one roll of {@code count} d12s. Each advantage replacement effect the player controls
     * adds one die to the roll, and that many lowest results are ignored (rule 706.6), so the
     * returned list keeps {@code count} results in the order they were rolled.
     */
    public List<Integer> rollDice(GameData gameData, UUID rollingPlayerId, int count) {
        int extraDice = countAdvantageEffects(gameData, rollingPlayerId);
        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < count + extraDice; i++) {
            results.add(roll());
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
