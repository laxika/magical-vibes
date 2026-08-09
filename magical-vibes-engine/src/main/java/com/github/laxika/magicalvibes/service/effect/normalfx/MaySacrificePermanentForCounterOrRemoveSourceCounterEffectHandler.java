package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MaySacrificePermanentForCounterOrRemoveSourceCounterEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a sacrifice-or-counter upkeep choice. If no matching permanent remains, the decline
 * branch is applied without asking an impossible question.
 */
@Component
@RequiredArgsConstructor
public class MaySacrificePermanentForCounterOrRemoveSourceCounterEffectHandler
        implements NormalEffectHandlerBean {

    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MaySacrificePermanentForCounterOrRemoveSourceCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MaySacrificePermanentForCounterOrRemoveSourceCounterEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> matchingIds = maySacrificeForCounterSupport.matchingPermanentIds(
                gameData, controllerId, e.filter());
        if (matchingIds.isEmpty()) {
            maySacrificeForCounterSupport.removeCounterFromSource(
                    gameData, entry.getSourcePermanentId(), e.counterType());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(e),
                entry.getCard().getName() + " - Sacrifice " + e.description() + "?",
                null, null, entry.getSourcePermanentId()));
    }
}
