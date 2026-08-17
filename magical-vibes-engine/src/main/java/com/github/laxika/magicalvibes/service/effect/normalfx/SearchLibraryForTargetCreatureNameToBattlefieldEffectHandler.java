package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForTargetCreatureNameToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchLibraryForTargetCreatureNameToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForTargetCreatureNameToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = findLegalTarget(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        SearchLibraryForTargetCreatureNameToBattlefieldEffect searchEffect =
                (SearchLibraryForTargetCreatureNameToBattlefieldEffect) effect;
        String targetName = target.getCard().getName();
        UUID controllerId = entry.getControllerId();
        librarySearchSupport.performLibrarySearch(
                gameData,
                controllerId,
                card -> targetName.equals(card.getName())
                        && (!searchEffect.permanentCardOnly() || card.getType().isPermanentType()),
                searchEffect.permanentCardOnly() ? "permanent cards named " + targetName : "cards named " + targetName,
                searchEffect.permanentCardOnly()
                        ? "Search your library for a permanent card with the same name as target creature and put it onto the battlefield."
                        : "Search your library for a card with the same name as target creature and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD);
    }

    private Permanent findLegalTarget(GameData gameData, UUID targetId) {
        if (targetId == null) {
            return null;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        return target != null && gameQueryService.isCreature(gameData, target) && !target.getCard().isToken()
                ? target
                : null;
    }
}
