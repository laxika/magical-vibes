package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceAtNextStepEffect;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link ReturnAllCardsExiledWithSourceAtNextStepEffect}: queues one delayed return for all
 * cards exiled with the source, each coming back under its owner's control.
 */
@Component
public class ReturnAllCardsExiledWithSourceAtNextStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnAllCardsExiledWithSourceAtNextStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnAllCardsExiledWithSourceAtNextStepEffect) effect;
        UUID sourceId = entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId()
                : entry.getCard() == null ? null : entry.getCard().getId();
        if (sourceId == null) {
            return;
        }

        List<ExiledCardEntry> tracked = gameData.exiledCards.stream()
                .filter(exiled -> sourceId.equals(exiled.sourcePermanentId()))
                .toList();
        if (tracked.isEmpty()) {
            return;
        }

        List<Card> cards = tracked.stream().map(ExiledCardEntry::card).toList();
        Map<UUID, UUID> returnControllerByCardId = new LinkedHashMap<>();
        tracked.forEach(exiled -> returnControllerByCardId.put(exiled.card().getId(), exiled.ownerId()));

        gameData.queueDelayedAction(new PendingExileReturn(
                cards.getFirst(), tracked.getFirst().ownerId(), false, false, e.returnStep(), 0,
                cards.subList(1, cards.size()), false, false, false, false, null, null, false, false, 0,
                Set.of(), null, 0, Map.of(), false)
                .withTriggerSource(entry.getCard(), entry.getControllerId())
                .withReturnControllers(returnControllerByCardId));
    }
}
