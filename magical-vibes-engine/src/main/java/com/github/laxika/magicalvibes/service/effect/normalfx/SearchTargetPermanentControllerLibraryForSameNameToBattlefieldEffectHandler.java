package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPermanentControllerLibraryForSameNameToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchTargetPermanentControllerLibraryForSameNameToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SearchTargetLibraryEffectHandler searchTargetLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchTargetPermanentControllerLibraryForSameNameToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null || targetControllerId.equals(entry.getControllerId())) {
            return;
        }

        searchTargetLibraryEffectHandler.resolveForTargetPlayer(
                gameData,
                entry,
                new SearchTargetLibraryEffect(
                        1,
                        new CardNamedPredicate(target.getCard().getName()),
                        LibrarySearchDestination.BATTLEFIELD_UNDER_SEARCHER,
                        true),
                targetControllerId);
    }
}
