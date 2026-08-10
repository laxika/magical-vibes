package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.KrarksThumbEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
        int thumbCount = gameQueryService.countPlayerControlledStaticEffects(
                gameData, playerId, KrarksThumbEffect.class);
        int physicalFlips = 1 << thumbCount;
        boolean heads = false;
        for (int i = 0; i < physicalFlips; i++) {
            heads |= ThreadLocalRandom.current().nextBoolean();
        }
        return new CoinFlipResult(heads, physicalFlips);
    }

    public String replacementDetails(CoinFlipResult result) {
        if (result.physicalFlips() == 1) {
            return "";
        }
        return " (flipped " + result.physicalFlips() + " coins and ignored "
                + (result.physicalFlips() - 1) + ")";
    }

    public record CoinFlipResult(boolean heads, int physicalFlips) {
    }
}
