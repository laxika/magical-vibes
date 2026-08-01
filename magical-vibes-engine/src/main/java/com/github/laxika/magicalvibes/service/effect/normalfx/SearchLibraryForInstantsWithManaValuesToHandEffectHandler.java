package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForInstantsWithManaValuesToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SearchLibraryForInstantsWithManaValuesToHandEffect} (Firemind's Foresight):
 * searches the controller's library for instant cards with mana values 3, then 2, then 1 to hand,
 * driven through {@link LibrarySearchSupport#startNextInstantManaValueToHandPick} so each value is
 * a separate revealed pick and the library is shuffled once after the last value.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForInstantsWithManaValuesToHandEffectHandler implements NormalEffectHandlerBean {

    private static final List<Integer> MANA_VALUES = List.of(3, 2, 1);

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForInstantsWithManaValuesToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        librarySearchSupport.startNextInstantManaValueToHandPick(gameData, controllerId,
                LibrarySearchFollowUp.instantManaValueToHandPicks(MANA_VALUES));
    }
}
