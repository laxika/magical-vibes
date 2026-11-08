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
        UUID enteringPermanentId = entry.getTriggeringPermanentId();
        if (enteringPermanentId == null) {
            return;
        }
        var entering = gameQueryService.findPermanentById(gameData, enteringPermanentId);
        if (entering == null && entry.getAttachedPermanentSnapshot() != null
                && enteringPermanentId.equals(entry.getAttachedPermanentSnapshot().getId())) {
            entering = entry.getAttachedPermanentSnapshot();
        }
        if (entering == null && entry.lastKnownPermanentCard(enteringPermanentId) != null) {
            entering = new com.github.laxika.magicalvibes.model.Permanent(
                    entry.lastKnownPermanentCard(enteringPermanentId).createCardCopy());
        }
        if (entering == null) {
            // The permanent left the battlefield: copy it as it last existed there (CR 608.2h).
            entering = departedSnapshot(gameData, entry, enteringPermanentId);
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
                false, copyEffect.trackWithSource(), false, null, java.util.Set.of(), copyEffect.removeLegendary());
        tokenCopySupport.createTokenCopies(gameData, entry,
                java.util.List.of(permanentCopierService.copiableCard(entering)),
                entering, controllerId, options);
    }

    private static com.github.laxika.magicalvibes.model.Permanent departedSnapshot(GameData gameData, StackEntry entry,
                                                                                 UUID permanentId) {
        for (var snapshot : gameData.departedPermanentSnapshots.values()) {
            if (snapshot.getId().equals(permanentId)) {
                return snapshot;
            }
        }
        var triggering = entry.getTriggeringPermanentSnapshot();
        return triggering != null && triggering.getId().equals(permanentId) ? triggering : null;
    }
}
