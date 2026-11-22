package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Puts the named cards onto the battlefield as one batch, which asks its entry choices at the end of the step. */
@Component
@RequiredArgsConstructor
public class PutCardsOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardsOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var put = (PutCardsOntoBattlefieldEffect) effect;
        List<BattlefieldEntryCard> entering = new ArrayList<>();
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, put, put.cards())) {
            UUID controllerId = battlefieldEntryService.resolveEnteringController(
                    gameData, located.ownerId(), new Permanent(located.card()));
            entering.add(new BattlefieldEntryCard(
                    controllerId, located.ownerId(), located.card(), located.zone(), null).withTapped(put.tapped()));
        }
        if (!entering.isEmpty()) {
            battlefieldEntryBatchSupport.begin(gameData, entering);
        }
    }
}
