package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForTargetCreatureNameToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchLibraryForTargetCreatureNameToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForTargetCreatureNameToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForTargetCreatureNameToBattlefieldEffect searchEffect =
                (SearchLibraryForTargetCreatureNameToBattlefieldEffect) effect;
        Permanent target = findLegalTarget(gameData, entry, searchEffect);
        if (target == null) {
            return;
        }

        String targetName = target.getCard().getName();
        UUID controllerId = entry.getControllerId();
        String cardType = searchEffect.creatureCardOnly()
                ? "creature card"
                : searchEffect.permanentCardOnly() ? "permanent card" : "card";
        String pluralCardType = searchEffect.creatureCardOnly()
                ? "creature cards"
                : searchEffect.permanentCardOnly() ? "permanent cards" : "cards";
        String destination = searchEffect.destination() == LibrarySearchDestination.BATTLEFIELD_TAPPED
                ? "onto the battlefield tapped"
                : "onto the battlefield";
        librarySearchSupport.performLibrarySearch(
                gameData,
                controllerId,
                card -> targetName.equals(card.getName())
                        && (!searchEffect.permanentCardOnly() || card.getType().isPermanentType())
                        && (!searchEffect.creatureCardOnly()
                        || card.hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE)),
                pluralCardType + " named " + targetName,
                "Search your library for a " + cardType + " with the same name as target creature and put it "
                        + destination + ".",
                false,
                true,
                searchEffect.destination());
    }

    private Permanent findLegalTarget(GameData gameData, StackEntry entry,
                                      SearchLibraryForTargetCreatureNameToBattlefieldEffect searchEffect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null && entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            targetId = entry.getTargetIds().getFirst();
        }
        if (targetId == null) {
            return null;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target) || target.getCard().isToken()) {
            return null;
        }
        return searchEffect.targetRestriction() == null
                || predicateEvaluationService.matchesPermanentPredicate(
                target,
                searchEffect.targetRestriction(),
                FilterContext.of(gameData).withXValue(entry.getXValue()))
                ? target
                : null;
    }
}
