package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AllowCastSourceCardFromGraveyardThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastSourceCardFromGraveyardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (sourceCard == null || graveyard == null
                || graveyard.stream().noneMatch(card -> card.getId().equals(sourceCard.getId()))) {
            return;
        }

        gameData.graveyardPlayPermissions.put(sourceCard.getId(), controllerId);
        gameData.graveyardPlayPermissionsExpireEndOfTurn.add(sourceCard.getId());
    }
}
