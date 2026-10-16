package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOfLatNamEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Library of Lat-Nam on resolution: carries out the mode the opponent chose while the spell was cast
 * (CR 700.2e, see {@code MayPenaltyChoiceHandlerService#handleLibraryOfLatNamChoice}). Draw three
 * schedules a delayed "draw three cards at the beginning of the next turn's upkeep"; search runs an
 * unrestricted library search to hand as the next step of this resolution.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LibraryOfLatNamEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LibraryOfLatNamEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Boolean drawThree = ((LibraryOfLatNamEffect) effect).drawThree();
        if (drawThree == null) {
            return;
        }
        if (drawThree) {
            gameData.queueDelayedAction(new DrawCardsAtNextUpkeep(entry.getControllerId(), 3, entry.getCard()));
            log.info("Game {} - Library of Lat-Nam schedules three draws", gameData.id);
            return;
        }
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == effect) {
                entry.insertEffectsToResolve(i + 1, List.of(new SearchLibraryEffect()));
                return;
            }
        }
    }
}
