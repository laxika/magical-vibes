package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Removes the permanent linked to the trigger source (Dance of Many's mutual bond). The trigger
 * collector bakes the linked permanent's id into the stack entry's {@code targetId}; here we clear the
 * partner's back-reference first so removing it does not re-trigger the reciprocal removal, then exile
 * or sacrifice it per {@link RemoveLinkedPermanentEffect.Mode}. A sacrifice is the resolution step's
 * departure ({@link StepDepartureSupport#sacrifice}), so it asks its CR 616.1 questions before the
 * permanent leaves.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveLinkedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveLinkedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveLinkedPermanentEffect) effect;

        // The leaves-battlefield collector bakes the id in before the source is gone; the
        // becomes-untapped trigger leaves it null and the still-present source carries the link.
        UUID linkedId = e.linkedPermanentId();
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (linkedId == null) {
            linkedId = source == null ? null : source.getChosenPermanentId();
        }
        if (source != null && linkedId != null && linkedId.equals(source.getChosenPermanentId())) {
            source.setChosenPermanentId(null);
        }

        Permanent linked = linkedId == null ? null : gameQueryService.findPermanentById(gameData, linkedId);
        if (linked == null) {
            return;
        }

        // CR 701.21a: only a permanent its controller controls can be sacrificed, so a chosen creature
        // that another player has since gained control of is left alone.
        if (e.mode() == RemoveLinkedPermanentEffect.Mode.SACRIFICE
                && !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, linked.getId()))) {
            return;
        }

        // Break the mutual bond before removal so the linked permanent's own leaves-battlefield trigger
        // becomes a no-op (avoids the reciprocal exile <-> sacrifice bouncing back). A sacrificed partner's
        // link points at the permanent that has already left, so breaking it while its departure waits for
        // an answer can't lose a trigger. A one-way link (Tyrannical Pitlord) has no reciprocal trigger, and
        // the linked permanent may itself be a Pitlord whose own chosen creature must still be sacrificed
        // when it leaves.
        if (e.mode() != RemoveLinkedPermanentEffect.Mode.SACRIFICE) {
            linked.setChosenPermanentId(null);
        }

        String name = linked.getCard().getName();
        log.info("Game {} - {} removed ({}) via linked leaves-battlefield trigger",
                gameData.id, name, e.mode());
        if (e.mode() == RemoveLinkedPermanentEffect.Mode.EXILE) {
            permanentRemovalService.removePermanentToExile(gameData, linked);
            gameLogService.append(gameData, GameLog.text(name + " is exiled."));
            permanentRemovalService.removeOrphanedAuras(gameData);
            return;
        }
        stepDepartureSupport.sacrifice(gameData, entry, e, linked.getId());
    }
}
