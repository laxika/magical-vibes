package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MayEffect) effect;

        // CR 702.99a — cipher is "If this spell is represented by a card, you may exile this card
        // encoded on a creature you control". A copy cast off the encoded card is not represented by
        // a card, so the copy's cipher ability does nothing and must not prompt.
        if (entry.isCopy() && e.wrapped() instanceof CipherEncodeEffect) {
            return;
        }

        // On a multi-target card each "you may" is bound to its own target group, so the pending
        // ability must carry that group's target rather than the entry's lone one. A bound group
        // with no legal target left does nothing (CR 608.2b) — don't even prompt.
        UUID targetId = entry.getTargetId();
        List<UUID> groupTargets = entry.targetsForBoundEffectGroup(e);
        if (groupTargets != null && !entry.getTargetIds().isEmpty()) {
            if (groupTargets.isEmpty()) {
                return;
            }
            targetId = groupTargets.getFirst();
        }

        // CR 603.5 — "you may" choice happens at resolution time.
        // Set flag so the resolution loop re-runs this effect after the player responds.
        gameData.resolvingMayEffectFromStack = true;
        UUID choicePlayerId = switch (e.choicePlayer()) {
            case CONTROLLER -> entry.getControllerId();
            case ACTIVE_PLAYER -> entry.getActivePlayerId();
            case TARGET_PLAYER -> targetId != null && gameData.playerIds.contains(targetId) ? targetId : null;
            case TARGET_PERMANENT_CONTROLLER -> targetId == null
                    ? null
                    : gameQueryService.findPermanentController(gameData, targetId);
            case TARGET_SPELL_CONTROLLER -> findTargetSpellControllerId(gameData, targetId);
            case TRIGGERING_PERMANENT_CONTROLLER -> entry.getTriggeringPermanentControllerId() != null
                    ? entry.getTriggeringPermanentControllerId() : targetId;
        };
        if (choicePlayerId == null) {
            gameData.resolvingMayEffectFromStack = false;
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                choicePlayerId,
                List.of(e.wrapped()),
                entry.getCard().getName() + " - " + e.prompt(),
                targetId,
                null,
                entry.getSourcePermanentId(),
                null,
                0,
                0,
                entry.getAttackedTargetId(),
                e.choicePlayer() == MayChoicePlayer.ACTIVE_PLAYER ? entry.getActivePlayerId() : null,
                null,
                entry.getSourcePermanentSnapshot(),
                null,
                entry.getTriggeringCardId(),
                entry.getEventValue(),
                entry.getTriggeringPermanentId(),
                null,
                null
        ));
    }

    private UUID findTargetSpellControllerId(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) {
            return null;
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.getCard().getId().equals(targetCardId)) {
                return stackEntry.getControllerId();
            }
        }
        return null;
    }
}
