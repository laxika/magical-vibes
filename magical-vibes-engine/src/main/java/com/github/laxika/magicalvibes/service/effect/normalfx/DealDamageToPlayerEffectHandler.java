package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import org.springframework.stereotype.Component;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DealDamageToPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DealDamageToPlayerEffect remembered = (DealDamageToPlayerEffect) effect;
        if (!gameData.playerIds.contains(remembered.playerId())) {
            return;
        }

        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getDescription(),
                List.of(new DealDamageToPlayersEffect(remembered.damage(), DamageRecipient.TARGET_PLAYER)),
                remembered.playerId(),
                entry.getSourcePermanentId());
        damageEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        dealDamageToPlayersEffectHandler.resolve(gameData, damageEntry, damageEntry.getEffectsToResolve().getFirst());
    }
}
