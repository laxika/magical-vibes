package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTetraviteTokensToPutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileTetraviteTokensToPutCountersOnSelfEffect} (Tetravus): gathers the tokens the
 * source created (tracked in {@code GameData.tetravusCreatedTokens}) that are still on the
 * battlefield and prompts the controller to choose any number of them to exile. The exile and the
 * follow-up "put that many +1/+1 counters on this creature" are completed in
 * {@code MultiPermanentChoiceHandlerService} once the choice is answered. Does nothing if the source
 * has no surviving tokens.
 */
@Component
@RequiredArgsConstructor
public class ExileTetraviteTokensToPutCountersOnSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTetraviteTokensToPutCountersOnSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }
        Set<UUID> created = gameData.tetravusCreatedTokens.get(sourceId);
        if (created == null || created.isEmpty()) {
            return;
        }

        // "tokens created with this creature" — regardless of who currently controls them.
        List<UUID> eligible = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent perm : battlefield) {
                if (created.contains(perm.getId())) {
                    eligible.add(perm.getId());
                }
            }
        }
        if (eligible.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), eligible, eligible.size(),
                new MultiPermanentChoiceContext.ExileTetraviteTokensPutCountersOnSource(sourceId),
                "Exile any number of tokens created with " + source.getCard().getName()
                        + " to put that many +1/+1 counters on it.");
    }
}
