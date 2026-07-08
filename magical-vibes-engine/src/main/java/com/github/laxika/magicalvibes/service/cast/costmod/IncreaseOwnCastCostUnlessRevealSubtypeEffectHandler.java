package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOwnCastCostUnlessRevealSubtypeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Spell-self handler for {@link IncreaseOwnCastCostUnlessRevealSubtypeEffect}: the spell costs
 * {@code amount} more to cast unless the caster can reveal a card of the required subtype from
 * hand. The card being cast is excluded (during the playable-card preview it is still in hand, and
 * it cannot reveal itself). Returns a positive generic-mana delta when no matching card is
 * available, otherwise zero (the reveal is free).
 */
@Component
@RequiredArgsConstructor
public class IncreaseOwnCastCostUnlessRevealSubtypeEffectHandler implements CostModificationHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IncreaseOwnCastCostUnlessRevealSubtypeEffect.class;
    }

    @Override
    public boolean onSpellItself() {
        return true;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reveal = (IncreaseOwnCastCostUnlessRevealSubtypeEffect) effect;
        UUID playerId = context.castingPlayerId();
        List<Card> hand = context.gameData().playerHands.get(playerId);
        UUID spellId = context.spell().getId();
        boolean canReveal = hand != null && hand.stream()
                .anyMatch(c -> !c.getId().equals(spellId)
                        && gameQueryService.cardHasSubtype(c, reveal.subtype(), context.gameData(), playerId));
        return canReveal ? 0 : reveal.amount();
    }
}
