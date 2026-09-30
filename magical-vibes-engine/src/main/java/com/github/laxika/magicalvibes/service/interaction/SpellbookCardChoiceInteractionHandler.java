package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

/** Resolves a mandatory one-card draft from a spellbook. */
@Component
@RequiredArgsConstructor
public class SpellbookCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SpellbookCardChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.SpellbookCardChoice> handledType() {
        return PendingInteraction.SpellbookCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.SpellbookCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null
                || cardIds.size() < interaction.minCount()
                || cardIds.size() > interaction.maxCount()
                || cardIds.stream().distinct().count() != cardIds.size()
                || !interaction.validCardIds().containsAll(cardIds)) {
            throw new IllegalStateException("Choose between " + interaction.minCount() + " and "
                    + interaction.maxCount() + " cards from the spellbook");
        }

        List<Card> selectedCards = cardIds.stream()
                .map(cardId -> interaction.cards().stream()
                        .filter(card -> card.getId().equals(cardId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "Chosen spellbook card is no longer available")))
                .toList();

        if (interaction.draftMode()
                == com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.DraftMode.CONJURE_TO_HAND) {
            for (Card selected : selectedCards) {
                gameData.addCardToHand(player.getId(), selected);
            }
            gameData.interaction.clearAwaitingInput();
            gameLogService.append(gameData, GameLog.text(player.getUsername() + " conjures "
                    + String.join(", ", selectedCards.stream().map(Card::getName).toList())
                    + " into their hand."));
            if (interaction.chosenCardThenEffect() != null) {
                List<Card> hand = gameData.playerHands.get(player.getId());
                List<Integer> validIndices = IntStream.range(0, hand.size())
                        .filter(index -> cardIds.contains(hand.get(index).getId()))
                        .boxed()
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedHandChoice(
                        player.getId(), player.getId(), validIndices, 1,
                        false, false, List.of(), null,
                        "Choose one of those cards to put onto the battlefield.",
                        false, false, false, null, null, 0, null,
                        false, false, false, false, false, false, 0, false,
                        null, null, interaction.chosenCardThenEffect(), 0).withKeepInHand());
                return;
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card selected = selectedCards.getFirst();
        Card drafted = selected.createRuntimeCopy();
        drafted.setOwnerId(player.getId());
        if (interaction.draftMode()
                == com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.DraftMode.PERPETUALLY_BECOMES_ENCHANTMENT) {
            EnumSet<CardType> types = EnumSet.noneOf(CardType.class);
            types.addAll(drafted.getAdditionalTypes());
            types.add(CardType.ENCHANTMENT);
            drafted.setAdditionalTypes(Set.copyOf(types));
        }
        drafted.freeze();

        gameData.interaction.clearAwaitingInput();
        gameData.addCardToHand(player.getId(), drafted);
        if (interaction.draftMode()
                == com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.DraftMode.MAY_CAST_WITHOUT_PAYING_MANA_COST) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    drafted,
                    player.getId(),
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect(false)),
                    "Cast " + drafted.getName() + " without paying its mana cost?"));
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(player.getId()) + " drafts ", drafted,
                    " from a spellbook into their hand."));
        } else {
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(player.getId()) + " drafts ", drafted,
                    " from a spellbook into their hand. It perpetually becomes an enchantment."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }
}
