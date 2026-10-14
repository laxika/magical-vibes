package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
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

import static com.github.laxika.magicalvibes.service.effect.normalfx.CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffectHandler.MAX_TOTAL_MANA_VALUE;
import static com.github.laxika.magicalvibes.service.effect.normalfx.CastUpToTwoInstantOrSorceriesFromHandOrGraveyardWithoutPayingManaCostEffectHandler.isEligible;

/** Applies Invoke Calamity's capped selection and queues its selected spells in order. */
@Component
@RequiredArgsConstructor
public class InvokeCalamityCastChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.InvokeCalamityCastChoice> {

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
                || cards.stream().mapToInt(Card::getSmallestSpellManaValue).sum() > MAX_TOTAL_MANA_VALUE) {
            throw new IllegalStateException("Chosen spells exceed Invoke Calamity's mana value limit");
        }

        // Each spell counts with its own mana value, so a split card counts only the half being cast
        // (CR 709.3a). Its half is chosen as it is cast, so each cast leaves room for the cheapest
        // halves of the spells after it and counts what the spells before it actually cast.
        gameData.interaction.clearAwaitingInput();
        for (int i = cards.size() - 1; i >= 0; i--) {
            Card card = cards.get(i);
            int reservedManaValue = cards.subList(i + 1, cards.size()).stream()
                    .mapToInt(Card::getSmallestSpellManaValue)
                    .sum();
            List<UUID> earlierSelectedIds = cards.subList(0, i).stream().map(Card::getId).toList();
            boolean fromGraveyard = gameData.playerGraveyards
                    .getOrDefault(interaction.playerId(), List.of()).stream()
                    .anyMatch(candidate -> candidate.getId().equals(card.getId()));
            List<com.github.laxika.magicalvibes.model.effect.CardEffect> effects = fromGraveyard
                    ? List.of(new CastInvokeCalamityChosenSpellEffect(true, reservedManaValue, earlierSelectedIds))
                    : List.of(new CastInvokeCalamityChosenSpellEffect(false, reservedManaValue, earlierSelectedIds),
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
}
