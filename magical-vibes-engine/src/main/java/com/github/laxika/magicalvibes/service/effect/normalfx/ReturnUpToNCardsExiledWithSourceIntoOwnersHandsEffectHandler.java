package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToNCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Starts the choice to return up to the rolled number of source-tracked exiled cards. */
@Component
@RequiredArgsConstructor
public class ReturnUpToNCardsExiledWithSourceIntoOwnersHandsEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnUpToNCardsExiledWithSourceIntoOwnersHandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnUpToNCardsExiledWithSourceIntoOwnersHandsEffect returnEffect =
                (ReturnUpToNCardsExiledWithSourceIntoOwnersHandsEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        if (sourcePermanentId == null) {
            return;
        }

        int maxCount = Math.max(0, amountEvaluationService.evaluate(gameData, returnEffect.maxCount(),
                AmountContext.forStackEntry(entry, null)));
        if (maxCount == 0) {
            return;
        }

        UUID sourceId = sourcePermanentId;
        List<UUID> validCardIds = gameData.exiledCards.stream()
                .filter(exiled -> sourceId.equals(exiled.sourcePermanentId()))
                .map(ExiledCardEntry::card)
                .map(card -> card.getId())
                .toList();
        if (validCardIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ReturnExiledCardsToHandChoice(
                entry.getControllerId(), sourcePermanentId, validCardIds,
                Math.min(maxCount, validCardIds.size()), entry.getCard().getName()));
    }
}
