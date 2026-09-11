package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastCreatureFromHandFaceDownEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Illusionary Mask's creature-card offer. */
@Component
public class CastCreatureFromHandFaceDownEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastCreatureFromHandFaceDownEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null || hand.isEmpty()) {
            return;
        }

        Map<ManaColor, Integer> manaSpent = entry.getActivationManaSpent();
        ManaPool spentPool = new ManaPool();
        manaSpent.forEach(spentPool::add);
        List<Card> eligible = hand.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .filter(card -> card.getManaCost() != null)
                .filter(card -> new ManaCost(card.getManaCost()).canPay(spentPool))
                .toList();
        if (eligible.isEmpty()) {
            return;
        }

        UUID offerGroupId = UUID.randomUUID();
        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card card = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    List.of(new CastCreatureFromHandFaceDownEffect(manaSpent, offerGroupId)),
                    "Cast " + card.getName() + " face down without paying its mana cost?"
            ));
        }
    }
}
