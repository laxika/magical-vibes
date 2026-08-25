package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessExilesGraveyardEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Grip of Amnesia's choice to exile the targeted spell controller's graveyard. */
@Component
@RequiredArgsConstructor
public class CounterUnlessExilesGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessExilesGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                targetEntry.getControllerId(),
                List.of(effect),
                "Exile all cards from your graveyard to prevent "
                        + targetEntry.getCard().getName() + " from being countered?",
                targetCardId
        ));
    }
}
