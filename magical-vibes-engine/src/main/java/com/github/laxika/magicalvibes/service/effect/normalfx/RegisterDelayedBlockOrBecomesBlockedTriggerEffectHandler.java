package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedBlockOrBecomesBlockedTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Registers "whenever this permanent blocks or becomes blocked by a creature this combat" as two delayed abilities
 * about the permanent, one for each, controlled by the player who controlled the registering ability (CR 603.7e).
 */
@Slf4j
@Component
public class RegisterDelayedBlockOrBecomesBlockedTriggerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedBlockOrBecomesBlockedTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID permanentId = entry.getSourcePermanentId();
        if (permanentId == null) {
            return;
        }
        var e = (RegisterDelayedBlockOrBecomesBlockedTriggerEffect) effect;
        DelayedReferents thisCombat = DelayedReferents.NONE.duringCombatPhase(gameData.combatPhasesThisTurn);
        for (TriggeredAbility ability : new TriggeredAbility[] {
                new TriggeredAbility(Triggers.blocks(), e.effect()),
                new TriggeredAbility(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, e.effect())}) {
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                    entry.getControllerId(), entry.getCard(), ability, permanentId, permanentId, true)
                    .withReferents(thisCombat));
        }
        log.info("Game {} - {} registers a block-or-blocked delayed trigger for this combat",
                gameData.id, entry.getCard().getName());
    }
}
