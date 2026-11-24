package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves a mandatory one-card draft from a spellbook. */
@Component
@RequiredArgsConstructor
public class SpellbookCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SpellbookCardChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

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
            // Cards chosen from a spellbook into a hand are hidden (CR 400.2): the line counts them, as the hand's
            // size shows, and names none.
            gameLogService.append(gameData, GameLog.text(player.getUsername()
                    + (interaction.conjure() ? " conjures " : " drafts ") + cardsFromASpellbook(selectedCards.size())
                    + " into their hand."));
            if (interaction.conjure()) {
                ActionEvents.conjured(
                        gameData, player.getId(), selectedCards.size());
            }
            StackEntry choosing = gameData.pendingEffectResolutionEntry;
            if (choosing != null) {
                // The conjure step's result (StepKind.CONJURE): "then put one of them onto the battlefield" reads it.
                List<UUID> chosenIds = selectedCards.stream().map(Card::getId).toList();
                choosing.recordStep(StepKind.CONJURE, StepOutcome.ofObjects(chosenIds, List.of(player.getId()),
                        chosenIds.stream().map(id -> new MovedCard(id, id, Zone.HAND, player.getId())).toList()));
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
                == com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.DraftMode.MAY_CAST_WITHOUT_PAYING_MANA_COST
                || interaction.draftMode()
                == com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.DraftMode.MAY_CAST_WITHOUT_PAYING_MANA_COST_TO_CONTROLLER) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    drafted,
                    player.getId(),
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect(false)),
                    "Cast " + drafted.getName() + " without paying its mana cost?"));
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(player.getId())
                    + (interaction.conjure() ? " conjures " : " drafts ") + cardsFromASpellbook(1)
                    + " into their hand."));
            if (interaction.conjure()) {
                ActionEvents.conjured(gameData, player.getId(), 1);
            }
        } else {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(player.getId()) + " drafts "
                    + cardsFromASpellbook(1) + " into their hand. It perpetually becomes an enchantment."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private static String cardsFromASpellbook(int count) {
        return (count == 1 ? "a card" : count + " cards") + " from a spellbook";
    }
}
