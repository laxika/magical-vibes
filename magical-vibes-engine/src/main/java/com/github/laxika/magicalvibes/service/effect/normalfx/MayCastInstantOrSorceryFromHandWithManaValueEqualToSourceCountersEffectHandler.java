package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastInstantOrSorceryFromHandWithManaValueEqualToSourceCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Offers matching instant and sorcery cards one at a time through the shared free-cast flow.
 */
@Component
@RequiredArgsConstructor
public class MayCastInstantOrSorceryFromHandWithManaValueEqualToSourceCountersEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastInstantOrSorceryFromHandWithManaValueEqualToSourceCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MayCastInstantOrSorceryFromHandWithManaValueEqualToSourceCountersEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null) {
            return;
        }

        int requiredManaValue = source.getCounterCount(e.counterType());
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null) {
            return;
        }

        List<Card> eligible = hand.stream()
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .filter(card -> card.getManaValue() == requiredManaValue)
                .toList();

        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card card = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect()),
                    "Cast " + card.getName() + " without paying its mana cost?"
            ));
        }
    }
}
