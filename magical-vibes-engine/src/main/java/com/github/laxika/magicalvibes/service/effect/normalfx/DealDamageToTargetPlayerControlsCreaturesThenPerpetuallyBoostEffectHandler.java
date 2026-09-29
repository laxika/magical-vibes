package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerControlsCreaturesThenPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves targeted creature damage followed by a perpetual power/toughness modification. */
@Component
@RequiredArgsConstructor
public class DealDamageToTargetPlayerControlsCreaturesThenPerpetuallyBoostEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DealDamageToPermanentsTargetControlsEffectHandler damageHandler;
    private final PerpetuallyBoostTargetCreatureEffectHandler boostHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetPlayerControlsCreaturesThenPerpetuallyBoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DealDamageToTargetPlayerControlsCreaturesThenPerpetuallyBoostEffect boostEffect =
                (DealDamageToTargetPlayerControlsCreaturesThenPerpetuallyBoostEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> creatures = new ArrayList<>(gameData.playerBattlefields
                .getOrDefault(targetPlayerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .toList());

        damageHandler.resolve(gameData, entry,
                new DealDamageToPermanentsTargetControlsEffect(boostEffect.damage()));

        for (Permanent creature : creatures) {
            StackEntry boostEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    entry.getCard(),
                    entry.getControllerId(),
                    entry.getCard().getName() + "'s ability",
                    List.of(),
                    creature.getId(),
                    entry.getSourcePermanentId());
            boostHandler.resolve(gameData, boostEntry,
                    new PerpetuallyBoostTargetCreatureEffect(
                            boostEffect.powerBoost(), boostEffect.toughnessBoost()));
        }
    }
}
