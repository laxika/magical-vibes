package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.service.battlefield.ExileAndReturnTransformedService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileSelfAndReturnTransformedEffect}: exiles the source and returns it transformed, and records the
 * RETURN step (the permanent it became, its owner) so "If you do, …" after it reads whether it came back. It
 * never prompts.
 */
@Component
@RequiredArgsConstructor
public class ExileSelfAndReturnTransformedEffectHandler implements NormalEffectHandlerBean {

    private final ExileAndReturnTransformedService exileAndReturnTransformedService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfAndReturnTransformedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.recordStep(StepKind.RETURN, StepOutcome.NONE);
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID cardId = source == null ? null : source.getOriginalCard().getId();
        boolean returned = exileAndReturnTransformedService.exileAndReturnTransformed(
                gameData, entry.getSourcePermanentId(),
                ((ExileSelfAndReturnTransformedEffect) effect).underControllerControl(), entry.getControllerId());
        Permanent back = returned ? returnedPermanent(gameData, cardId) : null;
        if (back == null) {
            return;
        }
        UUID ownerId = back.getOriginalCard().getOwnerId();
        entry.recordStep(StepKind.RETURN, StepOutcome.ofObjects(List.of(back.getId()),
                ownerId == null ? List.of() : List.of(ownerId)));
    }

    /** The permanent the card {@code cardId} became as it returned, showing either face. */
    private static Permanent returnedPermanent(GameData gameData, UUID cardId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getOriginalCard().getId().equals(cardId)) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
