package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MaySacrificePermanentForCounterSourceEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a resolution-time optional sacrifice that rewards the source with a +1/+1 counter. */
@Component
@RequiredArgsConstructor
public class MaySacrificePermanentForCounterSourceEffectHandler implements NormalEffectHandlerBean {

    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MaySacrificePermanentForCounterSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MaySacrificePermanentForCounterSourceEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> matchingIds = maySacrificeForCounterSupport.matchingPermanentIds(
                gameData, controllerId, entry.getSourcePermanentId(), e.filter());
        if (matchingIds.isEmpty()) {
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(e),
                entry.getCard().getName() + " - Sacrifice " + e.description() + "?",
                null, null, entry.getSourcePermanentId()));
    }
}
