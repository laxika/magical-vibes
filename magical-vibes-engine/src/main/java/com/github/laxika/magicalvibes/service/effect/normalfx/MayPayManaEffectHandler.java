package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureCardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayPayManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MayPayManaEffect) effect;

        if (e.payer() == MayPayPayer.ANY_PLAYER || e.payer() == MayPayPayer.ANY_OTHER_PLAYER
                || e.payer() == MayPayPayer.ANY_OPPONENT) {
            List<UUID> order = apnapOrder(gameData);
            if (e.payer() == MayPayPayer.ANY_OPPONENT) {
                order.removeIf(entry.getControllerId()::equals);
            }
            if (e.payer() == MayPayPayer.ANY_OTHER_PLAYER && entry.getActivePlayerId() != null) {
                order.removeIf(entry.getActivePlayerId()::equals);
            }
            if (order.isEmpty()) {
                return;
            }
            UUID first = order.getFirst();
            gameData.anyPlayerMayPayManaRemainingPlayers.clear();
            gameData.anyPlayerMayPayManaRemainingPlayers.addAll(order.subList(1, order.size()));
            gameData.resolvingMayEffectFromStack = true;
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), first, List.of(e), entry.getCard().getName() + " - " + e.prompt(),
                    entry.getTargetId(), e.manaCost(), entry.getSourcePermanentId(), null, 0, e.lifeCost(),
                    entry.getAttackedTargetId(), entry.getActivePlayerId(), null,
                    entry.getSourcePermanentSnapshot(), entry.getControllerId(), null, 0));
            return;
        }

        // CR 603.5 — "you may pay" choice happens at resolution time.
        // For "that player may pay" triggers (Paralyze) the payer is the enchanted permanent's
        // controller, carried on the stack entry's targetId, not the Aura's controller. For
        // "defending player may pay" attack triggers (Mtenda Lion) it is the attacked player.
        UUID payer = switch (e.payer()) {
            case CONTROLLER -> entry.getControllerId();
            case ENCHANTED_CONTROLLER -> entry.getTargetId();
            case DEFENDING_PLAYER -> defendingPlayer(gameData, entry);
            case TARGET_PERMANENT_CONTROLLER -> targetPermanentController(gameData, entry);
            case TARGET_PLAYER_OR_PERMANENT_CONTROLLER -> targetPlayerOrPermanentController(gameData,
                    entry.getTargetId());
            case TRIGGERING_PLAYER -> entry.getTargetId();
            case TRIGGERING_SPELL_CONTROLLER -> entry.getTargetId();
            case ACTIVE_PLAYER -> entry.getActivePlayerId() != null
                    ? entry.getActivePlayerId() : gameData.activePlayerId;
            case ANY_PLAYER, ANY_OTHER_PLAYER, ANY_OPPONENT -> null;
        };
        if (payer == null) {
            return;
        }
        if (e.wrapped() instanceof DiscardAndDrawCardEffect
                && gameData.playerHands.getOrDefault(payer, List.of()).isEmpty()) {
            return;
        }
        if (e.wrapped() instanceof DiscardCardThenEffect discard && discard.filter() == null
                && !discard.thenWithoutDiscard() && discard.recipient() == DiscardRecipient.CONTROLLER
                && gameData.playerHands.getOrDefault(payer, List.of()).isEmpty()) {
            // "you may pay {M} and discard a card": discarding is part of the cost, so with no card
            // to discard the payment cannot be made.
            return;
        }

        if (e.wrapped() instanceof ExileDyingCreatureCardAndCreateTokenEffect exile && exile.dyingCardId() != null
                && gameQueryService.findCardInGraveyardById(gameData, exile.dyingCardId()) == null) {
            // "you may pay {1} and exile it": exiling the card is part of the payment, so a card that
            // already left the graveyard makes the payment impossible.
            return;
        }

        if (e.wrapped() instanceof ConditionalEffect conditional && !conditional.triggerTimeOnly()
                && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forStackEntry(entry), entry.getEventValue())) {
            return;
        }

        String manaCost = e.manaCost();
        if (e.dynamicManaCost() != null) {
            Permanent source = entry.getSourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            int amount = amountEvaluationService.evaluate(gameData, e.dynamicManaCost(),
                    AmountContext.forStackEntry(entry, source).withControllerId(payer));
            String dynamicGenericCost = "{" + amount + "}";
            manaCost = e.manaCost() == null
                    ? dynamicGenericCost
                    : e.manaCost().replace("{X}", dynamicGenericCost);
        }

        entry.setLastManaPaymentPlayerId(payer);
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                payer,
                e.wrapped() == null ? List.of() : List.of(e.wrapped()),
                entry.getCard().getName() + " - " + e.prompt(),
                entry.getTargetId(),
                manaCost,
                entry.getSourcePermanentId(),
                null,
                0,
                e.lifeCost(),
                entry.getAttackedTargetId(),
                entry.getActivePlayerId(),
                null,
                entry.getSourcePermanentSnapshot(),
                entry.getControllerId(),
                entry.getTriggeringCardId(),
                entry.getEventValue(),
                entry.getTriggeringPermanentId(),
                null,
                null,
                null,
                entry.getTriggeringCardGraveyardEntryVersion()
        ));

    }

    private UUID targetPermanentController(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return null;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
        if (controllerId == null) {
            // An earlier step of this spell moved the target away (String of Disappearances): its
            // controller is the one it had as it left the battlefield (CR 608.2h).
            controllerId = entry.getRemovedPermanentControllers().get(targetId);
        }
        if (controllerId == null && targetId.equals(entry.getTriggeringPermanentId())) {
            controllerId = entry.getTriggeringPermanentControllerId();
        }
        return controllerId;
    }

    private UUID targetPlayerOrPermanentController(GameData gameData, UUID targetId) {
        if (targetId == null) {
            return null;
        }
        return gameData.playerIds.contains(targetId)
                ? targetId
                : gameQueryService.findPermanentController(gameData, targetId);
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }

    /** The attacked player, or the controller of the attacked planeswalker, of an ON_ATTACK trigger. */
    private UUID defendingPlayer(GameData gameData, StackEntry entry) {
        if (entry.getDefendingPlayerId() != null) {
            return entry.getDefendingPlayerId();
        }
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return null;
        }
        if (gameData.playerIds.contains(attackedTargetId)) {
            return attackedTargetId;
        }
        // CR 310.9d: for a battle being attacked, "defending player" refers to its protector.
        Permanent attacked = gameQueryService.findPermanentById(gameData, attackedTargetId);
        if (attacked != null && gameQueryService.isBattle(gameData, attacked)) {
            return attacked.getProtectorPlayerId();
        }
        return gameQueryService.findPermanentController(gameData, attackedTargetId);
    }
}
