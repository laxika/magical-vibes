package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
