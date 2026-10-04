package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSuspendedCardAndRemoveTimeCountersEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseSuspendedCardAndRemoveTimeCountersEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseSuspendedCardAndRemoveTimeCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ChooseSuspendedCardAndRemoveTimeCountersEffect choiceEffect =
                (ChooseSuspendedCardAndRemoveTimeCountersEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, choiceEffect.amount(),
                AmountContext.forStackEntry(entry, null));
        if (amount <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<UUID> validCardIds;
        synchronized (gameData.exiledCards) {
            validCardIds = gameData.exiledCards.stream()
                    .filter(exiled -> controllerId.equals(exiled.ownerId()) && !exiled.faceDown())
                    .filter(exiled -> RemoveTimeCounterFromExiledCardEffectHandler.isSuspended(gameData, exiled))
                    .map(ExiledCardEntry::card)
                    .map(Card::getId)
                    .toList();
        }
        if (validCardIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.SuspendedCardTimeCounterChoice(
                        controllerId, validCardIds, amount, entry.getCard().getName()));
    }
}
