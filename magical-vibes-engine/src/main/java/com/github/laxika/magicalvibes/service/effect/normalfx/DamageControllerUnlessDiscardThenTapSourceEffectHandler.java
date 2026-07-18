package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageControllerUnlessDiscardThenTapSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "This permanent deals N damage to you unless you discard a card. If it deals damage to you this
 * way, tap it." (Mishra's War Machine). When the controller has no card the penalty is applied
 * immediately; otherwise the discard-or-take-it choice is offered via the may-ability system (the
 * accept/decline branch lives in {@code MayPenaltyChoiceHandlerService}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DamageControllerUnlessDiscardThenTapSourceEffectHandler implements NormalEffectHandlerBean {

    private final DealDamageToPlayersEffectHandler dealDamageToPlayersEffectHandler;
    private final GameQueryService gameQueryService;
    private final TapUntapSupport tapUntapSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageControllerUnlessDiscardThenTapSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DamageControllerUnlessDiscardThenTapSourceEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean hasCards = hand != null && !hand.isEmpty();

        if (!hasCards) {
            // No card to discard — apply the damage-then-tap penalty now.
            applyDamageThenTapIfDealt(gameData, entry, e.damage());
            return;
        }

        // Has a card — offer the discard-or-take-it choice via the may-ability system.
        String prompt = "Discard a card? If you don't, " + entry.getCard().getName()
                + " deals " + e.damage() + " damage to you and taps.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(e), prompt, null, null, entry.getSourcePermanentId()));
    }

    /**
     * Deals {@code damage} to the entry's controller through the normal damage path, then taps the
     * source permanent iff the damage actually landed (the controller's life dropped). Redirected or
     * prevented damage leaves the source untapped — "if it deals damage to you this way, tap it".
     */
    public void applyDamageThenTapIfDealt(GameData gameData, StackEntry entry, int damage) {
        UUID controllerId = entry.getControllerId();
        int lifeBefore = gameData.getLife(controllerId);

        dealDamageToPlayersEffectHandler.resolve(gameData, entry,
                new DealDamageToPlayersEffect(damage, DamageRecipient.CONTROLLER));

        if (gameData.getLife(controllerId) < lifeBefore) {
            Permanent source = entry.getSourcePermanentId() != null
                    ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                    : null;
            if (source != null && tapUntapSupport.tapPermanent(gameData, source)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source.getCard(), " taps itself."));
                log.info("Game {} - {} taps itself after dealing damage to its controller", gameData.id, source.getCard().getName());
            }
        }
    }
}
