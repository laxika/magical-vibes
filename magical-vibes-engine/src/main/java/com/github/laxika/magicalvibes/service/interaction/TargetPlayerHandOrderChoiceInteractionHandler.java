package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ZyymRevealHandEffect;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Applies the target player's hand order and starts Zyym's reveal prompts. */
@Component
@RequiredArgsConstructor
public class TargetPlayerHandOrderChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.TargetPlayerHandOrderChoice> {

    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.TargetPlayerHandOrderChoice> handledType() {
        return PendingInteraction.TargetPlayerHandOrderChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardOrder.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.TargetPlayerHandOrderChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to order your hand");
        }

        List<Integer> cardOrder = ((InteractionAnswer.CardOrder) answer).cardOrder();
        if (cardOrder == null || cardOrder.size() != interaction.cards().size()) {
            throw new IllegalStateException("Must specify order for all cards in hand");
        }

        Set<Integer> seen = new HashSet<>();
        for (int index : cardOrder) {
            if (index < 0 || index >= interaction.cards().size()) {
                throw new IllegalStateException("Invalid card index: " + index);
            }
            if (!seen.add(index)) {
                throw new IllegalStateException("Duplicate card index: " + index);
            }
        }

        List<Card> cards = interaction.cards();
        List<java.util.UUID> orderedCardIds = cardOrder.stream()
                .map(cards::get)
                .map(Card::getId)
                .toList();
        ZyymRevealHandEffect effect = new ZyymRevealHandEffect(
                interaction.playerId(), orderedCardIds, List.of(), 0);

        gameData.interaction.clearAwaitingInput();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                interaction.sourceCard(), interaction.controllerId(), List.of(effect),
                "Reveal another card?", interaction.playerId(), null, interaction.sourcePermanentId()));
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
