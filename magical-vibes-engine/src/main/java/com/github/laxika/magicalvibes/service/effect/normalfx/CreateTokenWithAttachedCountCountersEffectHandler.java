package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithAttachedCountCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Animal Friend's token creation and attachment-count counter rider. */
@Component
@RequiredArgsConstructor
public class CreateTokenWithAttachedCountCountersEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenWithAttachedCountCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenWithAttachedCountCountersEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        boolean sourceHasLeft = source == null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null) {
            return;
        }

        int counterAmount = sourceHasLeft ? entry.getEventValue()
                : countAttachedAurasAndEquipment(gameData, source.getId(), e.excludedAttachedPermanentId());
        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.tokenTemplate(), entry.getCard().getSetCode());
        entry.getCreatedPermanentIds().addAll(createdIds);
        if (counterAmount <= 0 || createdIds.isEmpty()) {
            return;
        }

        for (UUID createdId : createdIds) {
            Permanent token = gameQueryService.findPermanentById(gameData, createdId);
            if (token != null && !gameQueryService.cantHaveCounters(gameData, token)) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, token, e.counterType(), counterAmount);
            }
        }
    }

    private int countAttachedAurasAndEquipment(GameData gameData, UUID sourceId, UUID excludedId) {
        int[] count = {0};
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (sourceId.equals(permanent.getAttachedTo())
                    && !permanent.getId().equals(excludedId)
                    && (permanent.getCard().getSubtypes().contains(CardSubtype.AURA)
                    || permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT))) {
                count[0]++;
            }
        });
        return count[0];
    }
}
