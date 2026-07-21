package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastForMiracleCostEffect;
import com.github.laxika.magicalvibes.model.effect.MiracleMayCastEffect;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the miracle triggered ability: offer to cast the revealed card for its miracle cost
 * (CR 702.94a). No-ops if the card has left the hand.
 */
@Slf4j
@Component
public class MiracleMayCastEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MiracleMayCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card card = entry.getCard();
        UUID controllerId = entry.getControllerId();

        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean stillInHand = hand != null && hand.stream().anyMatch(c -> c.getId().equals(card.getId()));
        if (!stillInHand) {
            log.info("Game {} - miracle may-cast skipped; {} no longer in hand", gameData.id, card.getName());
            return;
        }

        String cost = card.getCastingOption(MiracleCast.class)
                .map(MiracleCast::manaCostString)
                .orElse(null);
        if (cost == null) {
            log.warn("Game {} - {} has no miracle cost", gameData.id, card.getName());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                card,
                controllerId,
                List.of(new MayCastForMiracleCostEffect()),
                "Cast " + card.getName() + " for its miracle cost (" + cost + ")?",
                null,
                cost
        ));
        log.info("Game {} - offering miracle cast of {} for {}", gameData.id, card.getName(), cost);
    }
}
