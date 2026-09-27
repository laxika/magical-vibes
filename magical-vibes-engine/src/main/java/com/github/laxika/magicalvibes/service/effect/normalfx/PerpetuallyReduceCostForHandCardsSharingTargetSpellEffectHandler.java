package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCostForHandCardsSharingTargetSpellEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetuallyReduceCostForHandCardsSharingTargetSpellEffectHandler
        implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyReduceCostForHandCardsSharingTargetSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        StackEntry targetEntry = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(targetId))
                .findFirst()
                .orElse(null);
        if (targetEntry == null || targetEntry.getCard() == null) {
            return;
        }

        Set<CardType> targetCardTypes = EnumSet.noneOf(CardType.class);
        Card targetCard = targetEntry.getCard();
        if (targetCard.getType() != null) {
            targetCardTypes.add(targetCard.getType());
        }
        targetCardTypes.addAll(targetCard.getAdditionalTypes());

        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        for (Card card : hand) {
            if (counterSupport.sharesCardType(card, targetCardTypes)) {
                gameData.perpetualGenericCastCostIncreases.merge(card.getId(), -1, Integer::sum);
            }
        }
    }
}
