package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.EdgarKingOfFigaroEffect;
import com.github.laxika.magicalvibes.model.effect.KrarksThumbEffect;
import com.github.laxika.magicalvibes.model.effect.PokeyTheScallywaggEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Performs one logical coin flip, including coin-flip replacement effects controlled by its player. */
@Component
@RequiredArgsConstructor
public class CoinFlipService {

    private final GameQueryService gameQueryService;

    public CoinFlipResult flip(GameData gameData, UUID playerId) {
        return flipCoins(gameData, playerId, 1).getFirst();
    }

    /** Performs a single event that flips {@code count} coins at once. */
    public List<CoinFlipResult> flipCoins(GameData gameData, UUID playerId, int count) {
        return flipCoins(gameData, playerId, count, true);
    }

    /** Performs coin-flip replacement without allowing Pokey to replace the event again. */
    public CoinFlipResult flipWithoutPokey(GameData gameData, UUID playerId) {
        return flipCoins(gameData, playerId, 1, false).getFirst();
    }

    private List<CoinFlipResult> flipCoins(GameData gameData, UUID playerId, int count,
                                           boolean applyPokey) {
        boolean pokeyApplies = applyPokey && gameQueryService.countPlayerControlledStaticEffects(
                gameData, playerId, PokeyTheScallywaggEffect.class) > 0;
        if (pokeyApplies) {
            List<CoinFlipResult> results = new ArrayList<>(count);
            for (int coin = 0; coin < count; coin++) {
                int roll = ThreadLocalRandom.current().nextInt(1, 21);
                results.add(new CoinFlipResult(roll >= 11, 1, false, roll));
            }
            return results;
        }

        int thumbCount = gameQueryService.countPlayerControlledStaticEffects(
                gameData, playerId, KrarksThumbEffect.class);
        int physicalFlips = 1 << thumbCount;
        boolean edgarApplies = gameData.playersWhoFlippedCoinsThisTurn.add(playerId)
                && gameQueryService.countPlayerControlledStaticEffects(
                        gameData, playerId, EdgarKingOfFigaroEffect.class) > 0;

        List<CoinFlipResult> results = new ArrayList<>(count);
        for (int coin = 0; coin < count; coin++) {
            boolean heads = edgarApplies;
            if (!heads) {
                for (int i = 0; i < physicalFlips; i++) {
                    heads |= ThreadLocalRandom.current().nextBoolean();
                }
            }
            results.add(new CoinFlipResult(heads, physicalFlips, edgarApplies, null));
        }
        return results;
    }

    public String replacementDetails(CoinFlipResult result) {
        if (result.pokeyD20Result() != null) {
            return " (Pokey, the Scallywagg rolled a d20: " + result.pokeyD20Result() + ")";
        }
        if (result.edgarApplied()) {
            return result.physicalFlips() == 1
                    ? " (Edgar, King of Figaro made it come up heads)"
                    : " (Edgar, King of Figaro made all physical flips come up heads; flipped "
                            + result.physicalFlips() + ")";
        }
        if (result.physicalFlips() == 1) {
            return "";
        }
        return " (flipped " + result.physicalFlips() + " coins and ignored "
                + (result.physicalFlips() - 1) + ")";
    }

    public String replacementDetailsForCoins(List<CoinFlipResult> results) {
        if (results.stream().anyMatch(result -> result.pokeyD20Result() != null)) {
            String rolls = results.stream()
                    .map(CoinFlipResult::pokeyD20Result)
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(", "));
            return " (Pokey, the Scallywagg rolled d20s: " + rolls + ")";
        }
        int physicalFlips = results.stream().mapToInt(CoinFlipResult::physicalFlips).sum();
        boolean edgarApplied = results.stream().anyMatch(CoinFlipResult::edgarApplied);
        if (edgarApplied) {
            return physicalFlips == results.size()
                    ? " (Edgar, King of Figaro made all of these flips come up heads)"
                    : " (Edgar, King of Figaro made all physical flips come up heads; flipped "
                            + physicalFlips + ")";
        }
        if (physicalFlips == results.size()) {
            return "";
        }
        return " (" + physicalFlips + " physical coin flips; one result kept per coin)";
    }

    public record CoinFlipResult(boolean heads, int physicalFlips, boolean edgarApplied,
                                 Integer pokeyD20Result) {
        public CoinFlipResult(boolean heads, int physicalFlips, boolean edgarApplied) {
            this(heads, physicalFlips, edgarApplied, null);
        }

        public CoinFlipResult(boolean heads, int physicalFlips) {
            this(heads, physicalFlips, false, null);
        }

        public boolean isActualCoinFlip() {
            return pokeyD20Result == null;
        }
    }
}
