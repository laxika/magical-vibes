package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.action.SacrificePermanentAtControllerEndStepUnlessPays;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves targeted token copies that use a mana-value-sized energy ransom at end step. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfTargetPermanentUnlessPayEnergyEqualToManaValueEffect) effect;
        List<UUID> targetIds = entry.targetsForBoundEffectGroup(copyEffect);
        if (targetIds == null) {
            targetIds = !entry.getTargetIds().isEmpty()
                    ? entry.getTargetIds()
                    : !entry.getTargetCardIds().isEmpty()
                    ? entry.getTargetCardIds()
                    : entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        } else if (targetIds.isEmpty() && entry.getDeclaredTargetIds().isEmpty()
                && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID controllerId = entry.getControllerId();
        for (UUID targetId : targetIds) {
            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) {
                continue;
            }
            // One ordered creation event (CR 616.1); the controller chooses what each token attacks, which need
            // not be what the source attacks (CR 508.4). "That token" covers every token the event creates, added
            // ones such as Queen Allenal's Soldier included.
            tokenCopySupport.createTokenCopiesChoosingAttackTargets(gameData, entry,
                    Collections.singletonList(targetPermanent.getCard()), sourcePermanent, controllerId,
                    copyEffect.copyEffect(), token -> gameData.queueDelayedAction(
                            new SacrificePermanentAtControllerEndStepUnlessPays(token.getId(), controllerId,
                                    entry.getCard(), new PayEnergyCost(token.getCard().getManaValue()))));
        }
    }
}
