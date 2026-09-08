package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPreviouslyDamagedBySourceEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToPreviouslyDamagedBySourceEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler playerDamageHandler;
    private final DealDamageToTargetPlayerOrPlaneswalkerEffectHandler playerOrPlaneswalkerDamageHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToPreviouslyDamagedBySourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToPreviouslyDamagedBySourceEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        for (UUID targetId : source.getDamageTargetsThisGame()) {
            if (gameData.playerIds.contains(targetId)) {
                if (targetId.equals(entry.getControllerId())) {
                    continue;
                }
                StackEntry playerDamageEntry = damageEntry(entry, targetId,
                        new DealDamageToPlayersEffect(e.damage(), DamageRecipient.TARGET_PLAYER));
                playerDamageHandler.resolve(gameData, playerDamageEntry,
                        playerDamageEntry.getEffectsToResolve().getFirst());
                continue;
            }

            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isPlaneswalker(gameData, target)) {
                continue;
            }

            StackEntry planeswalkerDamageEntry = damageEntry(entry, targetId,
                    new DealDamageToTargetPlayerOrPlaneswalkerEffect(e.damage()));
            playerOrPlaneswalkerDamageHandler.resolve(gameData, planeswalkerDamageEntry,
                    planeswalkerDamageEntry.getEffectsToResolve().getFirst());
        }
    }

    private static StackEntry damageEntry(StackEntry sourceEntry, UUID targetId, CardEffect damageEffect) {
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceEntry.getCard(),
                sourceEntry.getControllerId(),
                sourceEntry.getDescription(),
                List.of(damageEffect),
                targetId,
                sourceEntry.getSourcePermanentId());
        damageEntry.setSourcePermanentSnapshot(sourceEntry.getSourcePermanentSnapshot());
        return damageEntry;
    }
}
