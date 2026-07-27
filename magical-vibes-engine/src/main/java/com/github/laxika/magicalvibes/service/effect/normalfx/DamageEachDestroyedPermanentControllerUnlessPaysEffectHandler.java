package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageEachDestroyedPermanentControllerUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerTakesDamageUnlessPaysEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DamageEachDestroyedPermanentControllerUnlessPaysEffect}: one pay-or-take-damage
 * prompt per permanent recorded on {@code StackEntry.eventPlayerIds}, ordered APNAP so a player's
 * prompts arrive together. The prompts themselves are the existing
 * {@link EachPlayerTakesDamageUnlessPaysEffect} queue, which already sequences one decision at a
 * time and applies the damage on a decline.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DamageEachDestroyedPermanentControllerUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final EachPlayerTakesDamageUnlessPaysEffectHandler eachPlayerTakesDamageUnlessPaysEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageEachDestroyedPermanentControllerUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DamageEachDestroyedPermanentControllerUnlessPaysEffect) effect;
        List<UUID> payers = apnapGrouped(gameData, entry.getEventPlayerIds());
        if (payers.isEmpty()) {
            return;
        }
        eachPlayerTakesDamageUnlessPaysEffectHandler.offerToPlayers(gameData, entry,
                new EachPlayerTakesDamageUnlessPaysEffect(e.damage(), e.manaCost()), payers);
    }

    /** Keeps every occurrence but groups each player's prompts together, active player first. */
    private static List<UUID> apnapGrouped(GameData gameData, List<UUID> payers) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            ordered = rotated;
        }

        List<UUID> grouped = new ArrayList<>();
        for (UUID playerId : ordered) {
            for (UUID payer : payers) {
                if (payer.equals(playerId)) {
                    grouped.add(payer);
                }
            }
        }
        return grouped;
    }
}
