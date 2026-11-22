package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBecomeOnlyCardTypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Replaces each exiled card by its changed copy, the same object in the same stay. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBecomeOnlyCardTypeEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBecomeOnlyCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var become = (PerpetuallyBecomeOnlyCardTypeEffect) effect;
        for (var located : subjectResolutionService.locatedCards(gameData, entry, become, become.cards())) {
            if (located.zone() != Zone.EXILE) {
                continue;
            }
            Card changed = located.card().createRuntimeCopy();
            changed.setType(become.cardType());
            changed.setAdditionalTypes(Set.of());
            changed.setSubtypes(List.of());
            changed.freeze();
            gameData.exiledCards.replaceAll(exiled -> exiled.card().getId().equals(changed.getId())
                    ? new ExiledCardEntry(changed, exiled.ownerId(), exiled.sourcePermanentId(), exiled.faceDown(),
                    exiled.exilerId(), exiled.exiledTurnNumber(), exiled.controllerTurnsTakenAtExile(),
                    exiled.abilityLink())
                    : exiled);
            gameLogService.append(gameData, GameLog.cardThen(changed, " perpetually becomes only "
                    + become.cardType().name().toLowerCase(Locale.ROOT) + "."));
        }
    }
}
