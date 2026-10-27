package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GameOfChaosEffect;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.ChoosesCoinFlipResultsFirst;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GameOfChaosEffect}: performs the first coin flip (stakes 1) against the targeted
 * opponent, then queues the repeating "flip again?" prompt (doubled stakes) for the flip's winner.
 * Subsequent flips are driven by {@code GameOfChaosFlipAgainHandler}.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: its only change before the life swap is the coin flip, whose result
 * {@link GameOfChaosSupport#flipRound} keeps until the swap, so the loser's Lich's Duel Mastery sacrifice can be
 * asked about (CR 616.1) and the effect resolved again without flipping again. Marked
 * {@link ChoosesCoinFlipResultsFirst} too: the flip is the first thing it does, so a Krark's Thumb flip whose results
 * disagree stops it until the controller chooses the kept result.
 */
@Component
@RequiredArgsConstructor
public class GameOfChaosEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst,
        ChoosesCoinFlipResultsFirst {

    private final GameOfChaosSupport gameOfChaosSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GameOfChaosEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        if (!gameData.playerIds.contains(opponentId)) {
            return;
        }

        String sourceName = entry.getCard().getName();
        UUID decidingPlayer = gameOfChaosSupport.flipRound(gameData, sourceName, controllerId, opponentId, 1);
        if (decidingPlayer == null) {
            return;
        }
        gameOfChaosSupport.queueFlipAgain(gameData, entry.getCard(), controllerId, opponentId, decidingPlayer, 2);
    }
}
