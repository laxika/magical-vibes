package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenEachOpponentSacrificesCreaturePerCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DiscardOwnHandThenEachOpponentSacrificesCreaturePerCardEffect} (Malfegor): the
 * controller discards their entire hand, then each opponent sacrifices a creature of their choice
 * for each card discarded this way.
 *
 * <p>The number discarded equals the controller's hand size at resolution (every hand card is
 * discarded), snapshotted before delegating the discard to {@link DiscardHandEffectHandler}. That
 * count then drives an {@link SacrificePermanentsEffect} routed through
 * {@link SacrificePermanentsEffectHandler}; a {@link PermanentAllOfPredicate} wrapper (rather than a
 * bare {@link PermanentIsCreaturePredicate}) selects the multi-permanent choice mechanic so each
 * opponent chooses that many creatures instead of exactly one.
 */
@Component
@RequiredArgsConstructor
public class DiscardOwnHandThenEachOpponentSacrificesCreaturePerCardEffectHandler
        implements NormalEffectHandlerBean {

    private final DiscardHandEffectHandler discardHandEffectHandler;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardOwnHandThenEachOpponentSacrificesCreaturePerCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        int discardCount = hand == null ? 0 : hand.size();

        discardHandEffectHandler.resolve(gameData, entry, new DiscardHandEffect(DiscardRecipient.CONTROLLER));

        if (discardCount <= 0) {
            return;
        }

        // Wrapping the creature predicate routes through the multi-permanent choice, honouring the
        // discardCount (a bare PermanentIsCreaturePredicate would force exactly one per opponent).
        sacrificePermanentsEffectHandler.resolve(gameData, entry, new SacrificePermanentsEffect(discardCount,
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                SacrificeRecipient.EACH_OPPONENT));
    }
}
