package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveHitCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the card choice in Mari's combat-damage ability. */
@Component
@RequiredArgsConstructor
public class RemoveHitCounterFromExiledCardEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveHitCounterFromExiledCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID ownerId = entry.getTargetId();
        if (ownerId == null) {
            return;
        }

        List<UUID> eligibleCardIds;
        synchronized (gameData.exiledCards) {
            eligibleCardIds = gameData.exiledCards.stream()
                    .filter(exiled -> ownerId.equals(exiled.ownerId()) && !exiled.faceDown())
                    .filter(exiled -> gameData.exiledCardHitCounters.getOrDefault(
                            exiled.card().getId(), 0) > 0)
                    .map(ExiledCardEntry::card)
                    .map(card -> card.getId())
                    .toList();
        }
        if (eligibleCardIds.isEmpty()) {
            return;
        }

        RemoveHitCounterFromExiledCardEffect removeEffect =
                (RemoveHitCounterFromExiledCardEffect) effect;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.HitCounterExiledCardChoice(
                entry.getControllerId(), ownerId, eligibleCardIds, removeEffect.followUpEffect()));
    }
}
