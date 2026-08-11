package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardWithSameNameAsAnotherCreatureYouControlEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves Pattern Matcher's non-targeted, resolution-time creature choice before delegating the
 * actual library search to the shared search effect handler.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLibraryForCardWithSameNameAsAnotherCreatureYouControlEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardWithSameNameAsAnotherCreatureYouControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<Permanent> otherCreatures = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!permanent.getId().equals(sourcePermanentId)
                        && gameQueryService.isCreature(gameData, permanent)) {
                    otherCreatures.add(permanent);
                }
            }
        }

        if (otherCreatures.isEmpty()) {
            log.info("Game {} - {} controls no other creatures for Pattern Matcher", gameData.id,
                    entry.getCard().getName());
            return;
        }

        if (otherCreatures.size() == 1) {
            search(gameData, entry, otherCreatures.getFirst().getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.PatternMatcherCreatureChoice(controllerId, sourcePermanentId));
        playerInputService.beginPermanentChoice(gameData, controllerId,
                otherCreatures.stream().map(Permanent::getId).toList(),
                "Choose another creature you control.");
    }

    public void search(GameData gameData, StackEntry entry, String cardName) {
        searchLibraryEffectHandler.resolve(gameData, entry,
                new SearchLibraryEffect(new CardNamedPredicate(cardName), LibrarySearchDestination.HAND));
    }
}
