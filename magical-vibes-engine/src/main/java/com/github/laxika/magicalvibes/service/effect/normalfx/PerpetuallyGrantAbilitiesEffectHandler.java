package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves {@link PerpetuallyGrantAbilitiesEffect}. The log names no card in a hand or library (CR 400.2). */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantAbilitiesEffect) effect;
        for (var located : subjectResolutionService.locatedCards(gameData, entry, effect, grant.cards())) {
            grant.abilities().forEach(ability -> gameData.grantPerpetualAbility(located.card().getId(), ability));
            String change = " perpetually gains an ability.";
            boolean hidden = located.zone() == Zone.HAND || located.zone() == Zone.LIBRARY;
            gameLogService.append(gameData, hidden
                    ? GameLog.text("A card in " + gameData.playerIdToName.get(located.ownerId()) + "'s "
                            + (located.zone() == Zone.HAND ? "hand" : "library") + change)
                    : GameLog.cardThenUnlessHidden(gameData, located.card(), change));
        }
    }
}
