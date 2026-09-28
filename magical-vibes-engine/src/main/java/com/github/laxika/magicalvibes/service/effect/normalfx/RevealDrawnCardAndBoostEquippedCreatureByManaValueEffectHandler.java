package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardAndBoostEquippedCreatureByManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RevealDrawnCardAndBoostEquippedCreatureByManaValueEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealDrawnCardAndBoostEquippedCreatureByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> drawnCardIds = entry.getDrawnCardIdsThisResolution();
        if (drawnCardIds.isEmpty()) {
            return;
        }

        UUID drawnCardId = drawnCardIds.getLast();
        Card drawnCard = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(card -> drawnCardId.equals(card.getId()))
                .findFirst()
                .orElse(null);
        if (drawnCard == null) {
            return;
        }

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.textCardText(playerName + " reveals ", drawnCard, "."));

        int manaValue = drawnCard.getManaValue();
        Permanent equippedCreature = entry.getTriggeringPermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (equippedCreature != null && manaValue > 0) {
            equippedCreature.setPowerModifier(equippedCreature.getPowerModifier() + manaValue);
            equippedCreature.setToughnessModifier(equippedCreature.getToughnessModifier() + manaValue);
            gameLogService.append(gameData, GameLog.builder()
                    .card(equippedCreature.getCard())
                    .text(" gets +" + manaValue + "/+" + manaValue + " until end of turn.")
                    .build());
        }

        if (manaValue > 0) {
            lifeSupport.applyLifeLoss(gameData, entry.getControllerId(), manaValue, entry.getCard().getName());
        }
    }
}
