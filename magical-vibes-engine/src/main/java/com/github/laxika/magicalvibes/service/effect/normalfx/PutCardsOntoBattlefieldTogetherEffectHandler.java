package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldTogetherEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the steps of a {@link PutCardsOntoBattlefieldTogetherEffect} while hand-to-battlefield puts
 * are held, then enters every held card through one battlefield-entry batch.
 */
@Component
@RequiredArgsConstructor
public class PutCardsOntoBattlefieldTogetherEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardsOntoBattlefieldTogetherEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var together = (PutCardsOntoBattlefieldTogetherEffect) effect;
        if (!gameData.holdingHandBattlefieldPuts) {
            gameData.holdingHandBattlefieldPuts = true;
            gameData.heldHandBattlefieldPutsLeaveHand = together.pickedCardsLeaveHand();
            gameData.heldHandBattlefieldPuts.clear();
            List<CardEffect> stepsThenFinish = new ArrayList<>(together.steps());
            stepsThenFinish.add(together);
            entry.insertEffectsToResolve(currentEffectIndex(entry, together) + 1, stepsThenFinish);
            return;
        }

        gameData.holdingHandBattlefieldPuts = false;
        List<BattlefieldEntryCard> held = List.copyOf(gameData.heldHandBattlefieldPuts);
        gameData.heldHandBattlefieldPuts.clear();
        if (gameData.heldHandBattlefieldPutsLeaveHand) {
            gameData.heldHandBattlefieldPutsLeaveHand = false;
            held.forEach(heldCard -> gameData.playerHands.get(heldCard.zoneOwnerId()).add(heldCard.card()));
        }
        if (!held.isEmpty()) {
            battlefieldEntryBatchSupport.begin(gameData, held);
        }
    }

    private int currentEffectIndex(StackEntry entry, CardEffect effect) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = effects.size() - 1; i >= 0; i--) {
            if (effects.get(i) == effect) {
                return i;
            }
        }
        throw new IllegalStateException("Resolving effect is not present in its stack entry");
    }
}
