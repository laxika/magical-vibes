package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Canoptek Wraith's non-targeting land choice and same-name basic-land search. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> lands = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isLand(gameData, permanent)) {
                    lands.add(permanent);
                }
            }
        }

        if (lands.isEmpty()) {
            // No land can be chosen, but the library is still searched (finding nothing) and shuffled
            log.info("Game {} - {} controls no land to choose for same-name basic-land search",
                    gameData.id, entry.getCard().getName());
            search(gameData, entry, "");
            return;
        }

        if (lands.size() == 1) {
            search(gameData, entry, lands.getFirst().getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SearchLibraryForUpToTwoBasicLandsWithSameNameAsChosenLand(
                        controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId,
                lands.stream().map(Permanent::getId).toList(),
                "Choose a land you control.");
    }

    public void search(GameData gameData, StackEntry entry, String landName) {
        searchLibraryEffectHandler.resolve(gameData, entry,
                new SearchLibraryEffect(
                        new Fixed(2),
                        new CardAllOfPredicate(List.of(
                                CardPredicateUtils.basicLand(), new CardNamedPredicate(landName))),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
