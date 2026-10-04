package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect}: each opponent, in
 * APNAP order (active player first among opponents), may search their library for a creature card,
 * put it onto the battlefield, then shuffle. The per-player searches are driven through the shared
 * {@link LibrarySearchSupport}/{@code LibraryChoiceHandlerService} interaction pipeline, with a
 * continuation after each player's optional search. Used by Boldwyr Heavyweights.
 */
@Component
@RequiredArgsConstructor
public class EachOpponentMaySearchLibraryForCreatureToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    @Autowired
    @Lazy
    private EffectResolutionService effectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        // Build APNAP-ordered queue of opponents only (skip the controller).
        var searchEffect = (EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect) effect;
        List<UUID> searchers = searchEffect.remainingSearchers() == null
                ? new ArrayList<>() : new ArrayList<>(searchEffect.remainingSearchers());
        if (searchEffect.remainingSearchers() == null) {
            int activeIndex = gameData.orderedPlayerIds.indexOf(gameData.activePlayerId);
            for (int offset = 0; offset < gameData.orderedPlayerIds.size(); offset++) {
                UUID playerId = gameData.orderedPlayerIds.get(
                        (Math.max(0, activeIndex) + offset) % gameData.orderedPlayerIds.size());
                if (!playerId.equals(controllerId)) {
                    searchers.add(playerId);
                }
            }
        }
        if (searchers.isEmpty()) {
            return;
        }
        UUID searcherId = searchers.removeFirst();
        StackEntry searchEntry = new StackEntry(entry.getEntryType(), entry.getCard(), searcherId,
                entry.getDescription(), List.of(
                        new MayEffect(new SearchLibraryEffect(new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD),
                                "Search your library for a creature card?"),
                        new EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect(searchers)),
                null, entry.getSourcePermanentId());
        searchEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        effectResolutionService.resolveEffects(gameData, searchEntry);
    }
}
