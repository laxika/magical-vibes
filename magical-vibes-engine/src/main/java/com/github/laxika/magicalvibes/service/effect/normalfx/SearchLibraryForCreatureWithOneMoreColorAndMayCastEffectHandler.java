package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithOneMoreColorAndMayCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyNColorsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchLibraryForCreatureWithOneMoreColorAndMayCastEffectHandler
        implements NormalEffectHandlerBean {

    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCreatureWithOneMoreColorAndMayCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent sacrificed = entry.getSacrificedPermanentSnapshot();
        if (entry.getControllerId() == null || sacrificed == null) {
            return;
        }

        CardAllOfPredicate filter = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardHasExactlyNColorsPredicate(sacrificed.getEffectiveColors().size() + 1)));
        searchLibraryEffectHandler.resolveWithFollowUp(
                gameData,
                entry,
                new SearchLibraryEffect(filter, LibrarySearchDestination.EXILE_FOR_MAY_CAST_WITH_NORMAL_COST),
                LibrarySearchFollowUp.NONE);
    }
}
