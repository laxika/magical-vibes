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
import java.util.UUID;

/** Resolves Wild Pair's creature search, by the stack entry's triggering permanent, through the shared library-search handler. */
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
        Integer totalPowerAndToughness = triggeringPermanentTotalPowerAndToughness(gameData, entry);
        if (totalPowerAndToughness == null) {
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

    /**
     * The triggering permanent's power plus toughness as the ability resolves, or as it last existed on the
     * battlefield (CR 608.2h): the departure's last-known values, else the ones the collector recorded as the ability
     * triggered; {@code null} when the entry names no permanent.
     */
    private Integer triggeringPermanentTotalPowerAndToughness(GameData gameData, StackEntry entry) {
        UUID triggeringId = entry.getTriggeringPermanentId();
        if (triggeringId == null) {
            return null;
        }
        Permanent triggering = gameQueryService.findPermanentById(gameData, triggeringId);
        if (triggering != null) {
            return gameQueryService.getEffectivePower(gameData, triggering)
                    + gameQueryService.getEffectiveToughness(gameData, triggering);
        }
        for (Permanent departed : gameData.departedPermanentSnapshots.values()) {
            if (departed.getId().equals(triggeringId) && departed.getLastKnownPower() != null
                    && departed.getLastKnownToughness() != null) {
                return departed.getLastKnownPower() + departed.getLastKnownToughness();
            }
        }
        if (entry.getLastKnownPermanentPowers().containsKey(triggeringId)
                && entry.getLastKnownPermanentToughnesses().containsKey(triggeringId)) {
            return entry.getLastKnownPermanentPowers().get(triggeringId)
                    + entry.getLastKnownPermanentToughnesses().get(triggeringId);
        }
        if (entry.getTriggeringPermanentPowerAtTrigger() != null
                && entry.getTriggeringPermanentToughnessAtTrigger() != null) {
            return entry.getTriggeringPermanentPowerAtTrigger() + entry.getTriggeringPermanentToughnessAtTrigger();
        }
        return null;
    }
}
