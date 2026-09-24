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
        if (cardIds == null || cardIds.size() != 1
                || !interaction.validCardIds().contains(cardIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one card from the spellbook");
        }

        Card selected = interaction.cards().stream()
                .filter(card -> card.getId().equals(cardIds.getFirst()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen spellbook card is no longer available"));

        if (interaction.draftMode()
                == com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.DraftMode.CONJURE_TO_HAND) {
            gameData.addCardToHand(player.getId(), selected);
            gameData.interaction.clearAwaitingInput();
            gameLogService.append(gameData, GameLog.text(player.getUsername() + " conjures "
                    + selected.getName() + " into their hand."));
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

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
