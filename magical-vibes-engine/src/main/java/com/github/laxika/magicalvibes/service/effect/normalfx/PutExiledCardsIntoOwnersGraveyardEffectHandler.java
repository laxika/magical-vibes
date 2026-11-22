package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Puts the exiled cards the subject names into their owners' graveyards. */
@Component
@RequiredArgsConstructor
public class PutExiledCardsIntoOwnersGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutExiledCardsIntoOwnersGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var put = (PutExiledCardsIntoOwnersGraveyardEffect) effect;
        var cards = subjectResolutionService.locatedCards(gameData, entry, put, put.cards());
        entry.recordStep(StepKind.PUT_INTO_GRAVEYARD, StepOutcome.NONE);
        for (SubjectResolutionService.LocatedCard located : cards) {
            if (located.zone() != Zone.EXILE || !gameData.removeFromExile(located.card().getId())) {
                continue;
            }
            if (graveyardService.addCardToGraveyard(gameData, located.ownerId(), located.card(), Zone.EXILE)) {
                entry.addToStep(StepKind.PUT_INTO_GRAVEYARD,
                        StepOutcome.ofObjects(List.of(located.card().getId()), List.of(located.ownerId())));
            }
            gameLogService.append(gameData, GameLog.textCardText(entry.getDescription() + " puts ", located.card(),
                    " into its owner's graveyard."));
        }
    }
}
