package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersToCreateTetraviteTokensEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveCountersToCreateTetraviteTokensEffect} (Tetravus): prompts the controller
 * for how many of the source's +1/+1 counters (0..the count present) to remove. The counter removal
 * and the follow-up token creation (recorded as "created with" the source) are completed in
 * {@code ChoiceHandlerService} once the number is answered. Does nothing if the source has no
 * +1/+1 counters.
 */
@Component
@RequiredArgsConstructor
public class RemoveCountersToCreateTetraviteTokensEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCountersToCreateTetraviteTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCountersToCreateTetraviteTokensEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        int counters = source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        if (counters <= 0) {
            return;
        }
        playerInputService.beginTetravusCounterRemovalChoice(gameData, entry.getControllerId(),
                source.getId(), counters, e.tokenTemplate());
    }
}
