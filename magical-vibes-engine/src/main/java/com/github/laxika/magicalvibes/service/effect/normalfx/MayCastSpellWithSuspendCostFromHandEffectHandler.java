package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastSpellWithSuspendCostFromHandEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Queues one cast-with-suspend-cost choice for every eligible spell in hand. */
@Component
public class MayCastSpellWithSuspendCostFromHandEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastSpellWithSuspendCostFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Card> eligible = hand.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .filter(card -> !card.isCastOnlyFromGraveyard())
                .filter(card -> card.getHandActivatedAbilities().stream()
                        .anyMatch(ability -> ability.isSuspendsSourceFromHand()
                                && ability.getManaCost() != null))
                .toList();

        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card card = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(new MayCastSpellWithSuspendCostFromHandEffect()),
                    "Cast " + card.getName() + " using its suspend cost?"
            ));
        }
    }
}
