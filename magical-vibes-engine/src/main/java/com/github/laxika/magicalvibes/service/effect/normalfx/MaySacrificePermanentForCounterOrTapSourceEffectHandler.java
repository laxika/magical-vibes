package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MaySacrificePermanentForCounterOrTapSourceEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "You may sacrifice [a permanent]. If you do, put a +1/+1 counter on this creature. If you don't,
 * tap this creature." With nothing to sacrifice there is no choice to make, so the source is tapped
 * without a prompt.
 */
@Component
@RequiredArgsConstructor
public class MaySacrificePermanentForCounterOrTapSourceEffectHandler implements NormalEffectHandlerBean {

    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MaySacrificePermanentForCounterOrTapSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MaySacrificePermanentForCounterOrTapSourceEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<UUID> matchingIds = maySacrificeForCounterSupport.matchingPermanentIds(
                gameData, controllerId, entry.getSourcePermanentId(), e.filter());
        if (matchingIds.isEmpty()) {
            maySacrificeForCounterSupport.tapSource(gameData, entry.getSourcePermanentId());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(e),
                entry.getCard().getName() + " - Sacrifice " + e.description() + "?",
                null, null, entry.getSourcePermanentId()));
    }
}
