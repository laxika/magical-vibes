package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithOneMoreManaValueThanSacrificedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchLibraryForCreatureWithOneMoreManaValueThanSacrificedPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCreatureWithOneMoreManaValueThanSacrificedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Permanent sacrificed = entry.getSacrificedPermanentSnapshot();
        if (controllerId == null || sacrificed == null) {
            return;
        }

        int requiredManaValue = sacrificed.getCard().getManaValue() + 1;
        librarySearchSupport.performLibrarySearch(
                gameData,
                controllerId,
                card -> card.hasType(CardType.CREATURE)
                        && card.getManaValue() == requiredManaValue
                        && !gameQueryService.isCardBlockedFromEnteringFromZone(
                                gameData, card, Zone.LIBRARY),
                "a creature card with mana value " + requiredManaValue,
                "Search your library for a creature card with mana value " + requiredManaValue
                        + " to put onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD
        );
    }
}
