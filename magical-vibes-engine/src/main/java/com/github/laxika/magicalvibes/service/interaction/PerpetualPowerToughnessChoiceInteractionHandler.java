package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PerpetualPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PerpetualPowerToughnessChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PerpetualPowerToughnessChoice> {

    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.PerpetualPowerToughnessChoice> handledType() {
        return PendingInteraction.PerpetualPowerToughnessChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PerpetualPowerToughnessChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        int cardIndex = ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        List<Card> hand = gameData.playerHands.getOrDefault(interaction.playerId(), List.of());
        if (!interaction.validIndices().contains(cardIndex)
                || cardIndex < 0 || cardIndex >= hand.size()) {
            throw new IllegalArgumentException("Choose a valid card from your hand");
        }

        Card chosenCard = hand.get(cardIndex);
        gameData.interaction.clearAwaitingInput();
        if (interaction.power() != 0 || interaction.toughness() != 0) {
            gameData.perpetualPowerToughnessModifiers.merge(
                    chosenCard.getId(),
                    new PerpetualPowerToughnessModifier(interaction.power(), interaction.toughness()),
                    PerpetualPowerToughnessModifier::add);
        }
        if (!interaction.grantedKeywords().isEmpty()) {
            gameData.perpetualKeywords.merge(chosenCard.getId(), interaction.grantedKeywords(),
                    (existing, added) -> {
                        Set<Keyword> merged = EnumSet.noneOf(Keyword.class);
                        merged.addAll(existing);
                        merged.addAll(added);
                        return Set.copyOf(merged);
                    });
        }
        if (interaction.genericCostReduction() != 0) {
            gameData.perpetualGenericCastCostIncreases.merge(
                    chosenCard.getId(), -interaction.genericCostReduction(), Integer::sum);
        }
        if (interaction.noncombatDamageBonus() != 0) {
            gameData.perpetualNoncombatDamageBonuses.merge(
                    chosenCard.getId(), interaction.noncombatDamageBonus(), Integer::sum);
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
