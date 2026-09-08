package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetArtifactThenCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Red Sun's Twilight's artifact destruction and its X=5 token-copy rider. */
@Component
@RequiredArgsConstructor
public class DestroyEachTargetArtifactThenCreateTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private static final int TOKEN_COPY_THRESHOLD = 5;

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEachTargetArtifactThenCreateTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetIds();
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        List<Permanent> targets = new ArrayList<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                targets.add(target);
            }
        }

        List<Permanent> destroyed = destructionSupport.destroyBatchCollecting(
                gameData, targets, entry.getCard().getName(), false);
        entry.setEventValue(destroyed.size());
        if (entry.getXValue() < TOKEN_COPY_THRESHOLD || destroyed.isEmpty()) {
            return;
        }

        List<Card> sourceCards = destroyed.stream().map(Permanent::getCard).toList();
        tokenCopySupport.createTokenCopies(
                gameData,
                entry,
                sourceCards,
                null,
                new CreateTokenCopyOfTargetPermanentEffect(true, true));
    }
}
