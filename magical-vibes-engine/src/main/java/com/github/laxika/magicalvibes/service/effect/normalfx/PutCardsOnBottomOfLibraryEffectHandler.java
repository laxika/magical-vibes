package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Moves the named cards at once, then each owner orders the ones put on the bottom of their library. */
@Component
@RequiredArgsConstructor
public class PutCardsOnBottomOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardsOnBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var put = (PutCardsOnBottomOfLibraryEffect) effect;
        Map<UUID, List<Card>> byOwner = new LinkedHashMap<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (SubjectResolutionService.LocatedCard located
                    : subjectResolutionService.locatedCards(gameData, entry, put, put.cards())) {
                Card card = located.card();
                if (located.zone() == Zone.GRAVEYARD) {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                } else if (located.zone() != Zone.EXILE || !gameData.removeFromExile(card.getId())) {
                    continue;
                }
                byOwner.computeIfAbsent(located.ownerId(), ignored -> new ArrayList<>()).add(card);
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        boolean asked = false;
        for (Map.Entry<UUID, List<Card>> group : byOwner.entrySet()) {
            UUID ownerId = group.getKey();
            List<Card> cards = group.getValue();
            if (cards.size() == 1) {
                gameData.playerDecks.get(ownerId).addLast(cards.getFirst());
            } else if (!asked) {
                asked = true;
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                        ownerId, new ArrayList<>(cards), true, ownerId,
                        "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
            } else {
                gameData.pendingLibraryBottomReorders.addLast(
                        new LibraryBottomReorderRequest(ownerId, new ArrayList<>(cards)));
            }
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(ownerId) + " puts "
                    + cards.size() + " card(s) on the bottom of their library."));
        }
    }
}
