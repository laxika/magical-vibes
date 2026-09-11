package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CastInvokeCalamityChosenSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Applies Invoke Calamity's capped selection and queues its selected spells in order. */
@Component
@RequiredArgsConstructor
public class InvokeCalamityCastChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.InvokeCalamityCastChoice> {

    private static final int MAX_TOTAL_MANA_VALUE = 6;

    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.InvokeCalamityCastChoice> handledType() {
        return PendingInteraction.InvokeCalamityCastChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.InvokeCalamityCastChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> selectedIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (selectedIds.size() > 2 || selectedIds.stream().distinct().count() != selectedIds.size()) {
            throw new IllegalStateException("Choose up to two spells");
        }
        if (!interaction.validCardIds().containsAll(selectedIds)) {
            throw new IllegalStateException("Chosen card was not eligible for Invoke Calamity");
        }

        List<Card> cards = selectedIds.stream()
                .map(id -> findEligibleCard(gameData, interaction.playerId(), id))
                .toList();
        if (cards.stream().anyMatch(card -> card == null)
                || cards.stream().mapToInt(Card::getManaValue).sum() > MAX_TOTAL_MANA_VALUE) {
            throw new IllegalStateException("Chosen spells exceed Invoke Calamity's mana value limit");
        }

        gameData.interaction.clearAwaitingInput();
        for (int i = cards.size() - 1; i >= 0; i--) {
            Card card = cards.get(i);
            boolean fromGraveyard = gameData.playerGraveyards
                    .getOrDefault(interaction.playerId(), List.of()).stream()
                    .anyMatch(candidate -> candidate.getId().equals(card.getId()));
            List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects = fromGraveyard
                    ? List.of(new CastInvokeCalamityChosenSpellEffect(true))
                    : List.of(new CastInvokeCalamityChosenSpellEffect(false),
                    new MayCastFromHandWithoutPayingManaCostEffect(false, true));
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    interaction.playerId(),
                    effects,
                    "Cast " + card.getName() + " without paying its mana cost?"));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private static Card findEligibleCard(GameData gameData, UUID playerId, UUID cardId) {
        Card card = findCard(gameData.playerHands.get(playerId), cardId);
        if (card != null) {
            return isEligible(card) && !card.isCastOnlyFromGraveyard() ? card : null;
        }
        card = findCard(gameData.playerGraveyards.get(playerId), cardId);
        return card != null && isEligible(card) ? card : null;
    }

    private static Card findCard(List<Card> cards, UUID cardId) {
        if (cards == null) {
            return null;
        }
        return cards.stream().filter(card -> card.getId().equals(cardId)).findFirst().orElse(null);
    }

    private static boolean isEligible(Card card) {
        return (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                && card.getManaValue() <= MAX_TOTAL_MANA_VALUE;
    }
}
