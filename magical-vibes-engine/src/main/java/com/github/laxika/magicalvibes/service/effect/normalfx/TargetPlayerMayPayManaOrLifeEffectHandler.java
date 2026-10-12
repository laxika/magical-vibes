package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerMayPayManaOrLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "That player may pay {M} or N life. If the player does, [wrapped]." The payer is the stack
 * entry's {@code targetId} (the active player for an {@code EACH_UPKEEP_TRIGGERED} trigger), not
 * the source's controller. A player who can pay neither resource is never asked.
 *
 * <p>When the player can afford both halves they pick one (or decline) from a list; otherwise the
 * choice is queued through the resolution-time may-ability path, so the wrapped effect
 * resolves on the same stack entry once the player accepts (see
 * {@code EffectResolutionService}'s CR 603.5 re-entry branch).
 */
@Component
@RequiredArgsConstructor
public class TargetPlayerMayPayManaOrLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerMayPayManaOrLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerMayPayManaOrLifeEffect) effect;

        UUID payerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        if (!gameData.playerIds.contains(payerId)) {
            return;
        }

        boolean canPayMana = new ManaCost(e.manaCost()).canPay(gameData.playerManaPools.get(payerId));
        boolean canPayLife = gameQueryService.canPlayerLifeChange(gameData, payerId)
                && gameData.getLife(payerId) >= e.lifeCost();
        if (!canPayMana && !canPayLife) {
            return;
        }

        if (canPayMana && canPayLife) {
            var context = new ChoiceContext.TargetPlayerManaOrLifePaymentChoice(
                    payerId, entry.getCard().getName(), e.manaCost(), e.lifeCost());
            gameData.rerunCurrentEffectAfterInteraction = true;
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    payerId, null, null, context,
                    List.of(context.payManaOption(), context.payLifeOption(),
                            ChoiceContext.TargetPlayerManaOrLifePaymentChoice.DECLINE),
                    entry.getCard().getName() + " - " + e.prompt()));
            return;
        }

        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), payerId, List.of(e.wrapped()),
                entry.getCard().getName() + " - " + e.prompt(),
                entry.getTargetId(), e.manaCost(), entry.getSourcePermanentId(), null, e.lifeCost(), 0));
    }
}
