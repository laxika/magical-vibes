package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetNonlandCardFromGraveyardAndSearchSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTargetNonlandCardFromGraveyardAndSearchSameNameEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final ExileService exileService;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetNonlandCardFromGraveyardAndSearchSameNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null || targetCard.hasType(CardType.LAND)) {
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (!entry.getControllerId().equals(graveyardOwnerId)) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCard.getId());
        exileService.exileCard(gameData, graveyardOwnerId, targetCard);

        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        int matchingCount = library == null ? 0 : (int) library.stream()
                .filter(card -> targetCard.getName().equals(card.getName()))
                .count();
        searchLibraryEffectHandler.resolve(gameData, entry, new SearchLibraryEffect(
                new Fixed(matchingCount),
                new CardNamedPredicate(targetCard.getName()),
                LibrarySearchDestination.HAND));
    }
}
