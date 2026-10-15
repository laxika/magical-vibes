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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private SplitCardCastSupport splitCardCastSupport;

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

        // "A spell with mana value equal to" considers only the half of a split card being cast
        // (CR 709.3a), so a split card is offered as the halves that match.
        Map<Card, List<Integer>> eligible = new LinkedHashMap<>();
        for (Card card : hand) {
            if (!card.hasType(CardType.INSTANT) && !card.hasType(CardType.SORCERY)) {
                continue;
            }
            if (SplitCardCastSupport.isSplitCard(card)) {
                List<Integer> modes = splitCardCastSupport.modesMatching(gameData, card, controllerId,
                        half -> half.getManaValue() == requiredManaValue);
                if (!modes.isEmpty()) {
                    eligible.put(card, modes);
                }
            } else if (card.getManaValue() == requiredManaValue) {
                eligible.put(card, null);
            }
        }

        List<Card> cards = new ArrayList<>(eligible.keySet());
        for (int i = cards.size() - 1; i >= 0; i--) {
            Card card = cards.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect(false)
                            .withCastableSplitModes(eligible.get(card))),
                    "Cast " + card.getName() + " without paying its mana cost?"
            ));
        }
    }
}
