package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPutMilledCreatureOrLandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutMilledCreatureOrLandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Mills the controller's library and offers only creature or land cards that were milled by this
 * resolution.
 */
@Component
@RequiredArgsConstructor
public class MillControllerAndMayPutMilledCreatureOrLandOnTopOfLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndMayPutMilledCreatureOrLandOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MillControllerAndMayPutMilledCreatureOrLandOnTopOfLibraryEffect millEffect =
                (MillControllerAndMayPutMilledCreatureOrLandOnTopOfLibraryEffect) effect;
        List<Card> milled = graveyardService.resolveMillPlayer(
                gameData, entry.getControllerId(), millEffect.count());
        CardAnyOfPredicate creatureOrLand = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.LAND)));

        List<Card> eligibleCards = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, creatureOrLand, null, gameData, entry.getControllerId()))
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();
        if (eligibleCards.isEmpty()) {
            return;
        }

        UUID groupId = UUID.randomUUID();
        for (int i = eligibleCards.size() - 1; i >= 0; i--) {
            Card card = eligibleCards.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    List.of(new PutMilledCreatureOrLandOnTopOfLibraryEffect(groupId)),
                    "Put " + card.getName() + " on top of your library?"));
        }
    }
}
