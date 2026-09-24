package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EncoreEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EncoreEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenCopyOfSourceEffectHandler sourceCopyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EncoreEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (var opponentId : gameData.orderedPlayerIds) {
            if (!gameData.playerIds.contains(opponentId) || opponentId.equals(entry.getControllerId())) {
                continue;
            }

            StackEntry copyEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    entry.getCard(),
                    entry.getControllerId(),
                    entry.getDescription(),
                    List.of(new CreateTokenCopyOfSourceEffect(
                            false, new Fixed(1), null, null, false, null, null,
                            true, false, Map.of(), true, Set.of(), null, false, 0)),
                    0,
                    null,
                    null,
                    Map.of(),
                    null,
                    List.of(),
                    List.of());
            Permanent sourceSnapshot = new Permanent(entry.getCard());
            sourceSnapshot.setAttackTarget(opponentId);
            copyEntry.setSourcePermanentSnapshot(sourceSnapshot);

            sourceCopyHandler.resolve(gameData, copyEntry, copyEntry.getEffectsToResolve().getFirst());
            for (var permanentId : copyEntry.getCreatedPermanentIds()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        permanentId, DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
            }
        }
    }
}
