package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RollWithAdvantageEffect;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

/** Produces a random twenty-sided die result. */
@Component
public class D20RollService {

    public int roll() {
        return ThreadLocalRandom.current().nextInt(1, 21);
    }

    /** Rolls a d20 while applying each advantage replacement effect controlled by the player. */
    public int roll(GameData gameData, UUID rollingPlayerId) {
        return roll(gameData, rollingPlayerId, 1);
    }

    /** Rolls the requested number of d20s while applying the player's extra-die effects. */
    public int roll(GameData gameData, UUID rollingPlayerId, int baseDiceCount) {
        if (baseDiceCount < 1) {
            throw new IllegalArgumentException("At least one d20 must be rolled");
        }

        int diceCount = baseDiceCount;
        List<Permanent> battlefield = gameData == null || rollingPlayerId == null
                ? null : gameData.playerBattlefields.get(rollingPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof RollWithAdvantageEffect) {
                        diceCount++;
                    }
                }
            }
        }

        int result = 0;
        for (int i = 0; i < diceCount; i++) {
            result = Math.max(result, roll());
        }
        return result;
    }
}
