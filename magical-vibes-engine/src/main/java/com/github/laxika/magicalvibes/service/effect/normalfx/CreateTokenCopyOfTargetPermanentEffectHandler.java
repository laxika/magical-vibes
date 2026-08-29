package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    static Card buildTokenCopyCard(Card sourceCard, CreateTokenCopyOfTargetPermanentEffect effect) {
        return TokenCopySupport.buildTokenCopyCard(sourceCard, effect);
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfTargetPermanentEffect) effect;

        List<UUID> targetIds = entry.targetsForEffect(copyEffect);
        if (targetIds.isEmpty() && !entry.getTargetCardIds().isEmpty()) {
            targetIds = entry.getTargetCardIds();
        } else if (targetIds.isEmpty() && entry.getDeclaredTargetIds().isEmpty()
                && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            resolveForTarget(gameData, entry, copyEffect, targetId);
        }
    }

    void resolveForTarget(GameData gameData, StackEntry entry,
                          CreateTokenCopyOfTargetPermanentEffect effect, UUID targetId) {
        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
        if (targetPermanent == null) {
            return;
        }
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID tokenControllerId = entry.getControllerId();
        if (effect.createForTargetController()) {
            UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (targetControllerId != null) {
                tokenControllerId = targetControllerId;
            }
        }
        tokenCopySupport.createTokenCopies(gameData, entry, List.of(targetPermanent.getCard()),
                sourcePermanent, tokenControllerId, effect);
    }
}
