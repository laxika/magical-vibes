package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealPlanarCardsUntilPlaneAndTriggerChaosEffect;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RevealPlanarCardsUntilPlaneAndTriggerChaosEffectHandler implements NormalEffectHandlerBean {

    private final PlanechaseService planechaseService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealPlanarCardsUntilPlaneAndTriggerChaosEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.planechase == null) {
            return;
        }

        List<Card> revealed = new ArrayList<>();
        Card plane = null;
        while (!gameData.planechase.deck.isEmpty()) {
            Card card = gameData.planechase.deck.removeFirst();
            revealed.add(card);
            if (card.hasType(CardType.PLANE)) {
                plane = card;
                break;
            }
        }
        if (revealed.isEmpty()) {
            return;
        }

        gameLogService.append(gameData, GameLog.text("Planar cards are revealed until a plane is found: "
                + revealed.stream().map(Card::getName).collect(Collectors.joining(", ")) + "."));

        UUID controllerId = entry.getControllerId();
        if (plane != null && !plane.getEffects(EffectSlot.CHAOS_TRIGGERED).isEmpty()) {
            planechaseService.trigger(gameData, new PlanarObject(plane, gameData.nextTimestamp()),
                    EffectSlot.CHAOS_TRIGGERED, controllerId);
        }

        if (revealed.size() == 1) {
            gameData.planechase.deck.add(revealed.getFirst());
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                controllerId, revealed, true, null,
                "Put the revealed cards on the bottom of the planar deck in any order.", 0, false, true));
    }
}
