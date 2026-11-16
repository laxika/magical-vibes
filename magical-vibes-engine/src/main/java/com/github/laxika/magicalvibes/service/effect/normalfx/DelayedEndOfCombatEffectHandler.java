package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedEndOfCombatEffect;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Schedules a triggered ability for the beginning of the end-of-combat step (CR 603.7a, 511.2). The resolving ability's
 * non-targeting permanent, "that creature" (the creature on the other side of a block), is the delayed ability's
 * "that creature" too, with what was attached to it just before it left the battlefield if it has left (CR 608.2h).
 */
@Component
@RequiredArgsConstructor
public class DelayedEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DelayedEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DelayedEndOfCombatEffect delayedEffect = (DelayedEndOfCombatEffect) effect;
        UUID thatCreatureId = entry.isNonTargeting() && entry.getTargetId() != null
                && !gameData.playerIds.contains(entry.getTargetId()) ? entry.getTargetId() : null;
        DelayedReferents referents = thatCreatureId == null ? DelayedReferents.NONE
                : DelayedReferents.permanents(List.of(thatCreatureId))
                .withPermanentAttachments(entry.lastKnownPermanentAttachments(thatCreatureId));
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.endOfCombat(), delayedEffect.effect()),
                entry.getSourcePermanentId(), thatCreatureId, null, referents));
    }
}
