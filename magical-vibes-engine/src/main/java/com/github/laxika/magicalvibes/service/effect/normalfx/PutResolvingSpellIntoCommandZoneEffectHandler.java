package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutResolvingSpellIntoCommandZoneEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves emblem cards by putting the physical spell card into its owner's command zone. */
@Component
@RequiredArgsConstructor
public class PutResolvingSpellIntoCommandZoneEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutResolvingSpellIntoCommandZoneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.isCopy() || entry.getPhysicalCard() == null) {
            return;
        }

        Card card = entry.getPhysicalCard();
        List<Card> commandZone = gameData.playerCommandZones.computeIfAbsent(
                entry.getOwnerId(), ignored -> new ArrayList<>());
        commandZone.removeIf(commandZoneCard -> commandZoneCard.getId().equals(card.getId()));
        commandZone.add(card);
        triggerCollectionService.checkYourCommanderPutIntoCommandZoneTriggers(
                gameData, card, entry.getOwnerId());
        entry.setSpellDispositionHandled(true);
        gameLogService.append(gameData, GameLog.cardThen(card,
                " is put into its owner's command zone."));
    }
}
