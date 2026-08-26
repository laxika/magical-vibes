package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastPermanentSpellFromHandOrPutLandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Kellan, the Kid's permanent-spell-or-land choice. */
@Component
@RequiredArgsConstructor
public class MayCastPermanentSpellFromHandOrPutLandEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastPermanentSpellFromHandOrPutLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null) {
            return;
        }

        var permanentSpellFilter = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.BATTLE),
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardTypePredicate(CardType.PLANESWALKER)));
        int maxManaValue = Math.max(0, entry.getEventValue());
        List<Card> eligible = hand.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .filter(card -> card.getManaValue() <= maxManaValue)
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, permanentSpellFilter, null))
                .toList();
        CardEffect putLand = new PutCardToBattlefieldEffect(
                new CardTypePredicate(CardType.LAND), "land");

        if (!eligible.isEmpty()) {
            UUID choiceGroupId = UUID.randomUUID();
            for (int i = eligible.size() - 1; i >= 0; i--) {
                Card card = eligible.get(i);
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        card,
                        controllerId,
                        List.of(new MayCastFromHandWithoutPayingManaCostEffect(
                                false, choiceGroupId, putLand)),
                        "Cast " + card.getName() + " without paying its mana cost?"));
            }
        } else if (hand.stream().anyMatch(card -> card.hasType(CardType.LAND))) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    controllerId,
                    List.of(putLand),
                    "Put a land card from your hand onto the battlefield?"));
        }
    }
}
