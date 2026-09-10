package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CastingPlayerMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.PendingMayAbility;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Queues Minamo's optional draw for the spell's caster rather than the plane's controller. */
@Component
public class CastingPlayerMayDrawCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastingPlayerMayDrawCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID castingPlayerId = entry.getTargetId();
        if (castingPlayerId == null || !gameData.playerIds.contains(castingPlayerId)) {
            return;
        }

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                entry.getCard(),
                castingPlayerId,
                List.of(effect),
                entry.getCard().getName() + " — You may draw a card."));
    }
}
