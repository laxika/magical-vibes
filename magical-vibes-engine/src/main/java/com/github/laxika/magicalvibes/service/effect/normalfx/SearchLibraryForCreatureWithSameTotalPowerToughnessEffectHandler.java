package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithSameTotalPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerToughnessTotalAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Wild Pair's trigger-bound creature search through the shared library-search handler. */
@Component
@RequiredArgsConstructor
public class SearchLibraryForCreatureWithSameTotalPowerToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCreatureWithSameTotalPowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForCreatureWithSameTotalPowerToughnessEffect wildPairEffect =
                (SearchLibraryForCreatureWithSameTotalPowerToughnessEffect) effect;
        Permanent enteringPermanent = entry.getTriggeringPermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());

        int totalPowerAndToughness;
        if (enteringPermanent != null) {
            totalPowerAndToughness = gameQueryService.getEffectivePower(gameData, enteringPermanent)
                    + gameQueryService.getEffectiveToughness(gameData, enteringPermanent);
        } else if (wildPairEffect.powerAtTrigger() != null && wildPairEffect.toughnessAtTrigger() != null) {
            totalPowerAndToughness = wildPairEffect.powerAtTrigger() + wildPairEffect.toughnessAtTrigger();
        } else {
            return;
        }

        SearchLibraryEffect search = new SearchLibraryEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardPowerToughnessTotalAtMostPredicate(totalPowerAndToughness),
                        new CardNotPredicate(new CardPowerToughnessTotalAtMostPredicate(
                                totalPowerAndToughness - 1)))),
                LibrarySearchDestination.BATTLEFIELD);
        searchLibraryEffectHandler.resolve(gameData, entry, search);
    }
}
