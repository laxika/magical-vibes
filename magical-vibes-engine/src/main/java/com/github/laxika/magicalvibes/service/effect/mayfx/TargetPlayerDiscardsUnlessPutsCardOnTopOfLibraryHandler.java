package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsUnlessPutsCardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Accept branch puts a chosen card from the answering player's hand on top of their library;
 * decline branch discards a card and records that discard as the resolving entry's event value,
 * which Tainted Specter's following mass damage reads back as "1 damage".
 */
@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsUnlessPutsCardOnTopOfLibraryHandler implements MayEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsUnlessPutsCardOnTopOfLibraryEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID playerId = ability.controllerId();
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        if (accepted) {
            List<Card> handSnapshot = List.copyOf(hand);
            interactionHandlerRegistry.begin(gameData, PendingInteraction.PutCardsFromHandOnLibraryCardChoice
                    .putOnLibrary(playerId, handSnapshot.stream().map(Card::getId).toList(), handSnapshot, 1,
                            HandToLibraryPlacement.TOP));
            return;
        }

        // The parked entry is the ability that queued this prompt; its event value carries the
        // discarded-card count to the mass damage that follows.
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry != null) {
            entry.setEventValue(1);
            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
        }
        playerInputService.beginDiscardChoice(gameData, playerId, 1);
    }
}
