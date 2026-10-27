package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PokeyTheScallywaggEffect;
import com.github.laxika.magicalvibes.model.effect.RollWithAdvantageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.ArrayList;
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
        if (pokeyReplacesRoll(gameData, rollingPlayerId)) {
            CoinFlipService.CoinFlipResult result = coinFlipService.flipWithoutPokey(gameData, rollingPlayerId);
            return new D20RollResult(result.heads() ? 20 : 1, false);
        }
        if (gameData != null && coinFlipService != null) {
            coinFlipService.closeChoiceWindow(gameData);
        }

        int diceCount = baseDiceCount;
        List<Permanent> battlefield = gameData == null || rollingPlayerId == null
                ? null : gameData.playerBattlefields.get(rollingPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService != null && gameQueryService.hasLostAllAbilities(gameData, permanent)) continue;
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

    /**
     * Has each of {@code playerIds} roll one d20, as one roll per player ("each player rolls a d20"). The rolls Pokey,
     * the Scallywagg replaces with coin flips are flipped first, before any d20 is rolled, so a Krark's Thumb choice
     * on one of those flips can still stop the resolving effect without a d20 already rolled being rolled again.
     *
     * @return the results in the order of {@code playerIds}
     */
    public List<D20RollResult> rollEach(GameData gameData, List<UUID> playerIds) {
        List<UUID> pokeyPlayers = playerIds.stream()
                .filter(playerId -> pokeyReplacesRoll(gameData, playerId))
                .toList();
        List<CoinFlipService.CoinFlipResult> flips = coinFlipService == null || gameData == null
                ? List.of() : coinFlipService.flipEachWithoutPokey(gameData, pokeyPlayers);
        List<D20RollResult> results = new ArrayList<>(playerIds.size());
        for (UUID playerId : playerIds) {
            int flipIndex = pokeyPlayers.indexOf(playerId);
            results.add(flipIndex >= 0
                    ? new D20RollResult(flips.get(flipIndex).heads() ? 20 : 1, false)
                    : new D20RollResult(roll(gameData, playerId), true));
        }
        return results;
    }

    private boolean pokeyReplacesRoll(GameData gameData, UUID rollingPlayerId) {
        return gameData != null && rollingPlayerId != null
                && gameQueryService != null && coinFlipService != null
                && gameQueryService.countPlayerControlledStaticEffects(
                        gameData, rollingPlayerId, PokeyTheScallywaggEffect.class) > 0;
    }

    public record D20RollResult(int result, boolean wasD20Roll) {
    }
}
