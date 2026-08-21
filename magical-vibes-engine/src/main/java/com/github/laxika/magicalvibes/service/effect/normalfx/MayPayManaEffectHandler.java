package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayPayManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MayPayManaEffect) effect;

        if (e.payer() == MayPayPayer.ANY_PLAYER || e.payer() == MayPayPayer.ANY_OTHER_PLAYER) {
            List<UUID> order = apnapOrder(gameData);
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
                    entry.getTargetId(), e.manaCost(), entry.getSourcePermanentId(), null, e.lifeCost(), 0,
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
            case TARGET_PERMANENT_CONTROLLER -> entry.getTargetId() == null
                    ? null
                    : gameQueryService.findPermanentController(gameData, entry.getTargetId());
            case TARGET_PLAYER_OR_PERMANENT_CONTROLLER -> targetPlayerOrPermanentController(gameData,
                    entry.getTargetId());
            case TRIGGERING_PLAYER -> entry.getTargetId();
            case TRIGGERING_SPELL_CONTROLLER -> entry.getTargetId();
            case ANY_PLAYER, ANY_OTHER_PLAYER -> null;
        };
        if (payer == null) {
            return;
        }

        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                payer,
                e.wrapped() == null ? List.of() : List.of(e.wrapped()),
                entry.getCard().getName() + " - " + e.prompt(),
                entry.getTargetId(),
                e.manaCost(),
                entry.getSourcePermanentId(),
                e.lifeCost()
        ));

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
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }
}
