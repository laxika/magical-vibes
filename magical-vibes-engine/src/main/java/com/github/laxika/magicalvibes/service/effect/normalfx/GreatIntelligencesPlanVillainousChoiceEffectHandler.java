package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GreatIntelligencesPlanVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Great Intelligence's Plan's targeted villainous choice. */
@Component
@RequiredArgsConstructor
public class GreatIntelligencesPlanVillainousChoiceEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final VillainousChoiceSupport villainousChoiceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GreatIntelligencesPlanVillainousChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (!state.active) {
            state.reset();
            state.active = true;
            state.currentTargetId = entry.getTargetId();
        }

        if (state.waitingForDiscard) {
            state.waitingForDiscard = false;
            finish(gameData);
            return;
        }
        if (state.waitingForControllerMay) {
            if (!gameData.pendingMayAbilities.isEmpty() || gameData.interaction.isAwaitingInput()) {
                return;
            }
            state.waitingForControllerMay = false;
            finish(gameData);
            return;
        }
        if (state.chosenMode != null) {
            String chosen = state.chosenMode;
            state.chosenMode = null;
            if (GreatIntelligencesPlanVillainousChoiceEffect.DISCARD_OPTION.equals(chosen)) {
                discard(gameData, entry);
            } else {
                offerFreeCast(gameData, entry);
            }
            return;
        }

        UUID targetPlayerId = state.currentTargetId;
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || targetPlayerId.equals(entry.getControllerId())) {
            finish(gameData);
            return;
        }

        boolean canDiscard = hasCardToDiscard(gameData, targetPlayerId);
        boolean canCast = hasSpellToCast(gameData, entry.getControllerId());
        if (!canDiscard && !canCast) {
            finish(gameData);
            return;
        }
        if (!canDiscard) {
            offerFreeCast(gameData, entry);
            return;
        }
        if (!canCast) {
            discard(gameData, entry);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        villainousChoiceSupport.beginChoice(gameData, targetPlayerId, entry.getCard().getName(),
                GreatIntelligencesPlanVillainousChoiceEffect.CAST_OPTION,
                List.of(GreatIntelligencesPlanVillainousChoiceEffect.DISCARD_OPTION,
                        GreatIntelligencesPlanVillainousChoiceEffect.CAST_OPTION),
                entry.getCard().getName() + " — Choose a villainous choice.");
    }

    private void discard(GameData gameData, StackEntry entry) {
        VillainousChoiceState state = gameData.villainousChoice;
        state.waitingForDiscard = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        gameData.discardCausedByOpponent = true;
        playerInteractionSupport.resolveDiscardCards(gameData, state.currentTargetId, 3,
                DiscardFollowUp.NONE);
        if (!gameData.interaction.isAwaitingInput() && gameData.pendingMayAbilities.isEmpty()) {
            state.waitingForDiscard = false;
            finish(gameData);
        }
    }

    private void offerFreeCast(GameData gameData, StackEntry entry) {
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null) {
            finish(gameData);
            return;
        }

        List<Card> eligible = hand.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        if (eligible.isEmpty()) {
            finish(gameData);
            return;
        }

        VillainousChoiceState state = gameData.villainousChoice;
        state.waitingForControllerMay = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card card = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect()),
                    "Cast " + card.getName() + " without paying its mana cost?"));
        }
    }

    private boolean hasCardToDiscard(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        return hand != null && !hand.isEmpty();
    }

    private boolean hasSpellToCast(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        return hand != null && hand.stream().anyMatch(card -> !card.hasType(CardType.LAND));
    }

    private void finish(GameData gameData) {
        if (villainousChoiceSupport.repeatIfNeeded(gameData)) {
            return;
        }
        gameData.villainousChoice.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }
}
