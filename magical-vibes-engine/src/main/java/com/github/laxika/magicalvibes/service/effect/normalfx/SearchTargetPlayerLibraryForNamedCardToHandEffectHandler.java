package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a target player's optional search of their own library for a named card. */
@Component
@RequiredArgsConstructor
public class SearchTargetPlayerLibraryForNamedCardToHandEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchTargetPlayerLibraryForNamedCardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        SearchTargetPlayerLibraryForNamedCardToHandEffect search =
                (SearchTargetPlayerLibraryForNamedCardToHandEffect) effect;
        librarySearchSupport.startNextToHandPick(gameData, targetPlayerId,
                LibrarySearchFollowUp.namedToHandPicks(List.of(search.cardName())));
    }
}
