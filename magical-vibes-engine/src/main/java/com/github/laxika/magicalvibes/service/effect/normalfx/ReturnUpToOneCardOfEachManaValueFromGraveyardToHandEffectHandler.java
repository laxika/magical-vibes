package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneCardOfEachManaValueFromGraveyardToHandEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnUpToOneCardOfEachManaValueFromGraveyardToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnUpToOneCardOfEachManaValueFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }

        int distinctManaValues = (int) graveyard.stream()
                .map(Card::getManaValue)
                .distinct()
                .count();
        gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                controllerId, distinctManaValues, null, GraveyardChoiceDestination.HAND,
                true, false, false, true, Set.of()));
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }
}
