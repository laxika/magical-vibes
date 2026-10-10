package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnBatch;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureCardFromTheirGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Dredge the Mire's sequential opponent choices. */
@Component
@RequiredArgsConstructor
public class EachOpponentChoosesCreatureCardFromTheirGraveyardToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentChoosesCreatureCardFromTheirGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.pendingGraveyardReturnBatch = new PendingGraveyardReturnBatch(
                controllerId, List.of(), java.util.Map.of());

        List<UUID> opponents = AnyOpponentMayTakeDamageSacrificeSourceEffectHandler.apnapOpponents(
                gameData, controllerId);
        CardTypePredicate creatureFilter = new CardTypePredicate(CardType.CREATURE);
        for (UUID opponentId : opponents) {
            gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                    opponentId, 1, creatureFilter, GraveyardChoiceDestination.BATTLEFIELD,
                    false, true, false, false, false, Set.of(), Set.of(), opponentId));
        }
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }
}
