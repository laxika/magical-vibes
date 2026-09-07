package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceAsAdventureFromGraveyardUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AllowCastSourceAsAdventureFromGraveyardUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastSourceAsAdventureFromGraveyardUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        Card sourceCard = entry.getCard();
        if (graveyard == null || sourceCard == null
                || graveyard.stream().noneMatch(card -> card.getId().equals(sourceCard.getId()))) {
            return;
        }

        int expireTurn = gameData.turnNumber
                + (controllerId.equals(gameData.activePlayerId) ? 2 : 1);
        gameData.graveyardAdventureCastPermissions.put(sourceCard.getId(),
                new GameData.GraveyardAdventureCastPermission(controllerId, expireTurn));
    }
}
