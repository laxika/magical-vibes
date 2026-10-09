package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a token copy of the permanent that caused an enter-the-battlefield trigger. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEnteringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final com.github.laxika.magicalvibes.service.battlefield.GameQueryService gameQueryService;
    private final com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService permanentCopierService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEnteringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfEnteringPermanentEffect) effect;
        UUID enteringPermanentId = entry.getTriggeringPermanentId() != null
                ? entry.getTriggeringPermanentId()
                : entry.getTargetId();
        if (enteringPermanentId == null) {
            return;
        }
        var entering = gameQueryService.findPermanentById(gameData, enteringPermanentId);
        if (entering == null && entry.getAttachedPermanentSnapshot() != null
                && enteringPermanentId.equals(entry.getAttachedPermanentSnapshot().getId())) {
            entering = entry.getAttachedPermanentSnapshot();
        }
        if (entering == null) return;
        UUID controllerId = entry.getControllerId();
        if (copyEffect.createForEnteringController()) {
            UUID enteringControllerId = gameQueryService.findPermanentController(gameData, enteringPermanentId);
            if (enteringControllerId == null) {
                enteringControllerId = entry.getTriggeringPermanentControllerId();
            }
            if (enteringControllerId != null) {
                controllerId = enteringControllerId;
            }
        }
        var options = new CreateTokenCopyOfTargetPermanentEffect(
                java.util.List.of(), java.util.Set.of(), null, null, java.util.Map.of(),
                copyEffect.grantHaste(), copyEffect.exileAtEndStep(), copyEffect.sacrificeAtEndStep(),
                false, copyEffect.trackWithSource(), false, null, java.util.Set.of());
        tokenCopySupport.createTokenCopies(gameData, entry,
                java.util.List.of(permanentCopierService.copiableCard(entering)),
                entering, controllerId, options);
    }
}
