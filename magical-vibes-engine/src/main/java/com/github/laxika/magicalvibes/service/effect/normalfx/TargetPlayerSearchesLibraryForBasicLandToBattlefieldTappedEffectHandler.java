package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSearchesLibraryForBasicLandToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerSearchesLibraryForBasicLandToBattlefieldTappedEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerSearchesLibraryForBasicLandToBattlefieldTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        librarySearchSupport.performLibrarySearch(
                gameData,
                targetPlayerId,
                card -> card.hasType(CardType.LAND)
                        && gameQueryService.cardHasSupertype(
                        card, CardSupertype.BASIC, gameData, targetPlayerId),
                "basic land cards",
                "Search your library for a basic land card and put it onto the battlefield tapped.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }
}
