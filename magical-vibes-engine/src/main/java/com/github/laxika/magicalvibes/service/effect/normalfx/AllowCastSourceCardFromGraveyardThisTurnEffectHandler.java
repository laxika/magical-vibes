package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastSourceCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AllowCastSourceCardFromGraveyardThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastSourceCardFromGraveyardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (sourceCard == null) {
            return;
        }
        boolean inGraveyard = graveyard != null
                && graveyard.stream().anyMatch(card -> card.getId().equals(sourceCard.getId()));
        boolean onBattlefield = entry.getSourcePermanentId() != null
                && gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) != null;
        if (!inGraveyard && !onBattlefield) {
            return;
        }

        gameData.graveyardPlayPermissions.put(sourceCard.getId(), controllerId);
        gameData.graveyardPlayPermissionsExpireEndOfTurn.add(sourceCard.getId());
    }
}
