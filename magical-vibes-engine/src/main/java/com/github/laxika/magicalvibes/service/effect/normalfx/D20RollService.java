package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PokeyTheScallywaggEffect;
import com.github.laxika.magicalvibes.model.effect.RollWithAdvantageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Produces a random twenty-sided die result. */
@Component
public class D20RollService {

    @Autowired
    private GameQueryService gameQueryService;

    @Autowired
    private CoinFlipService coinFlipService;

    public int roll() {
        return ThreadLocalRandom.current().nextInt(1, 21);
    }

    /** Rolls a d20 while applying each advantage replacement effect controlled by the player. */
    public int roll(GameData gameData, UUID rollingPlayerId) {
        return rollResult(gameData, rollingPlayerId).result();
    }

    /** Rolls the requested number of d20s while applying the player's extra-die effects. */
    public int roll(GameData gameData, UUID rollingPlayerId, int baseDiceCount) {
        return rollResult(gameData, rollingPlayerId, baseDiceCount).result();
    }

    /** Rolls a d20, retaining whether Pokey replaced it with a coin flip for trigger handling. */
    public D20RollResult rollResult(GameData gameData, UUID rollingPlayerId) {
        return rollResult(gameData, rollingPlayerId, 1);
    }

    public D20RollResult rollResult(GameData gameData, UUID rollingPlayerId, int baseDiceCount) {
        if (baseDiceCount < 1) {
            throw new IllegalArgumentException("At least one d20 must be rolled");
        }
        if (gameData != null && rollingPlayerId != null
                && gameQueryService != null && coinFlipService != null
                && gameQueryService.countPlayerControlledStaticEffects(
                        gameData, rollingPlayerId, PokeyTheScallywaggEffect.class) > 0) {
            CoinFlipService.CoinFlipResult result = coinFlipService.flipWithoutPokey(gameData, rollingPlayerId);
            return new D20RollResult(result.heads() ? 20 : 1, false);
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
        return new D20RollResult(result, true);
    }

    public record D20RollResult(int result, boolean wasD20Roll) {
    }
}
