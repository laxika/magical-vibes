package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromTargetPlayerHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "You may cast a [filter] spell from among the cards in target player's hand without paying its
 * mana cost." (Mindclaw Shaman)
 *
 * <p>Reuses the Counterlash may-cast-from-hand routing: one {@link PendingMayAbility} is queued per
 * eligible card, offered to the effect's controller, and accepting one casts it for free while
 * clearing the remaining offers. The cards live in the targeted player's hand, so the may handler
 * removes the chosen card from whichever hand holds it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastFromTargetPlayerHandWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastFromTargetPlayerHandWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayCastFromTargetPlayerHandWithoutPayingManaCostEffect e =
                (MayCastFromTargetPlayerHandWithoutPayingManaCostEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetId);
        if (hand == null || hand.isEmpty()) return;

        List<Card> eligible = hand.stream()
                .filter(c -> !c.hasType(CardType.LAND))
                .filter(c -> predicateEvaluationService.matchesCardPredicate(c, e.spellFilter(), null))
                .toList();

        for (int i = eligible.size() - 1; i >= 0; i--) {
            Card c = eligible.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    c, controllerId,
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect()),
                    "Cast " + c.getName() + " without paying its mana cost?"
            ));
        }
    }
}
