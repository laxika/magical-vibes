package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastPermanentFromHandWithManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayCastPermanentFromHandWithManaValueEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastPermanentFromHandWithManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        int requiredManaValue = greatestOtherPermanentManaValue(gameData, entry) + 1;

        List<Card> eligible = hand == null ? List.of() : hand.stream()
                .filter(this::isPermanentSpell)
                .filter(card -> card.getManaValue() == requiredManaValue)
                .toList();

        if (eligible.isEmpty()) {
            int effectIndex = entry.getEffectsToResolve().indexOf(effect);
            entry.insertEffectsToResolve(effectIndex + 1, List.of(new ScryEffect(1)));
            return;
        }

        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card card = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(new MayCastPermanentFromHandWithManaValueEffect(i == eligible.size() - 1)),
                    "Cast " + card.getName() + " without paying its mana cost?"
            ));
        }
    }

    private int greatestOtherPermanentManaValue(GameData gameData, StackEntry entry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) return 0;

        UUID sourcePermanentId = entry.getSourcePermanentId();
        return battlefield.stream()
                .filter(permanent -> sourcePermanentId == null || !permanent.getId().equals(sourcePermanentId))
                .mapToInt(permanent -> permanent.getCard().getManaValue())
                .max()
                .orElse(0);
    }

    private boolean isPermanentSpell(Card card) {
        CardType type = card.getType();
        return type != null && type.isPermanentType() && type != CardType.LAND;
    }
}
