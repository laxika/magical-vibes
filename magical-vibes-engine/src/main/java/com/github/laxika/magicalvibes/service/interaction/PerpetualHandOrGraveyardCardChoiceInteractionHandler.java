package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Applies a perpetual incorporation chosen from the controller's hand or graveyard. */
@Component
@RequiredArgsConstructor
public class PerpetualHandOrGraveyardCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PerpetualHandOrGraveyardCardChoice> {

    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.PerpetualHandOrGraveyardCardChoice> handledType() {
        return PendingInteraction.PerpetualHandOrGraveyardCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PerpetualHandOrGraveyardCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 1 || !interaction.validCardIds().contains(cardIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one card from your hand or graveyard");
        }

        Card chosenCard = findCard(gameData, interaction.playerId(), cardIds.getFirst());
        if (chosenCard == null) {
            throw new IllegalStateException("Chosen card is no longer in your hand or graveyard");
        }

        gameData.interaction.clearAwaitingInput();
        gameData.perpetualManaCostIncreases.merge(
                chosenCard.getId(), new ManaCost(interaction.perpetualManaCostIncrease()), ManaCost::increasedBy);
        gameData.perpetualTriggeredAbilityGrants.compute(chosenCard.getId(), (ignored, existing) -> {
            java.util.Map<EffectSlot, List<CardEffect>> updated = new java.util.EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
            }
            List<CardEffect> selfCastEffects = updated.computeIfAbsent(
                    EffectSlot.ON_SELF_CAST, ignoredSlot -> new ArrayList<>());
            if (!selfCastEffects.contains(interaction.selfCastAbility())) {
                selfCastEffects.add(interaction.selfCastAbility());
            }
            updated.replaceAll((slot, effects) -> List.copyOf(effects));
            return java.util.Map.copyOf(updated);
        });
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private Card findCard(GameData gameData, UUID playerId, UUID cardId) {
        return findCard(gameData.playerHands.getOrDefault(playerId, List.of()), cardId,
                gameData.playerGraveyards.getOrDefault(playerId, List.of()));
    }

    private Card findCard(List<Card> firstZone, UUID cardId, List<Card> secondZone) {
        for (Card card : firstZone) {
            if (card.getId().equals(cardId)) {
                return card;
            }
        }
        for (Card card : secondZone) {
            if (card.getId().equals(cardId)) {
                return card;
            }
        }
        return null;
    }
}
