package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndSearchesLibraryToHandEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetPlayerLosesLifeAndSearchesLibraryToHandEffect} (Maralen of the Mornsong's
 * draw-step trigger): the draw-step player (the stack entry's target) loses the effect's life, then
 * performs a mandatory unrestricted search of their own library for a card to put into their hand,
 * then shuffles. Both parts act on the draw-step player, so this cannot reuse the controller-relative
 * {@code LoseLifeEffect}/{@code SearchLibraryEffect} handlers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerLosesLifeAndSearchesLibraryToHandEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerLosesLifeAndSearchesLibraryToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerLosesLifeAndSearchesLibraryToHandEffect) effect;
        UUID playerId = entry.getTargetId();
        String sourceName = entry.getCard().getName();

        lifeSupport.applyLifeLoss(gameData, playerId, e.lifeLoss(), sourceName);

        // Mandatory unrestricted tutor for the draw-step player: any card, no reveal, can't fail to find.
        librarySearchSupport.performLibrarySearch(
                gameData,
                playerId,
                card -> true,
                "cards",
                "Search your library for a card to put into your hand.",
                false,
                false,
                LibrarySearchDestination.HAND);
    }
}
