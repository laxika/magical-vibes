package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayersLibraryForCardToTopEffect;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchTargetPlayersLibraryForCardToTopEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchTargetPlayersLibraryForCardToTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var targetPlayers = entry.targetsForEffect(effect);
        if (targetPlayers.isEmpty()) {
            return;
        }

        librarySearchSupport.startNextTargetPlayerTopSearch(
                gameData,
                LibrarySearchFollowUp.targetPlayersLibraryToTop(new ArrayList<>(targetPlayers)));
    }
}
