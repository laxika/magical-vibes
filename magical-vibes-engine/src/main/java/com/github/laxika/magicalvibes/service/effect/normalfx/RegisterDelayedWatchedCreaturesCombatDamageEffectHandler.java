package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreaturesCombatDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Registers Tamiyo, Field Researcher's "until your next turn, whenever either of those creatures
 * deals combat damage, you draw a card" watch against the chosen creatures.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedWatchedCreaturesCombatDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedWatchedCreaturesCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedWatchedCreaturesCombatDamageEffect) effect;

        List<UUID> targetIds;
        if (e.watchesChosenCreature()) {
            // "That creature" is the one the resolving ability chose (bolster's), not a target.
            targetIds = entry.getChosenPermanentId() == null ? List.of() : List.of(entry.getChosenPermanentId());
        } else {
            targetIds = entry.targetsForEffect(effect);
            if (targetIds.isEmpty() && entry.getTargetIds() != null) {
                targetIds = entry.getTargetIds();
            }
            if (targetIds.isEmpty() && entry.getTargetId() != null) {
                targetIds = List.of(entry.getTargetId());
            }
        }

        // "Up to two target creatures": a target that already left the battlefield contributes
        // nothing, and if every chosen creature is gone there is no watch to register at all.
        Set<UUID> watched = new LinkedHashSet<>();
        for (UUID targetId : targetIds) {
            if (gameQueryService.findPermanentById(gameData, targetId) != null) {
                watched.add(targetId);
            }
        }
        if (watched.isEmpty()) {
            return;
        }

        // One delayed ability about each watched creature (CR 603.7c): the one that creature's damage triggers. The
        // ability that created it is the "granting" permanent of what it does ("convert Optimus Prime").
        TriggeredAbility ability = new TriggeredAbility(
                e.combatDamageToPlayerOnly() ? Triggers.dealsCombatDamageToPlayer() : Triggers.dealsCombatDamage(),
                e.effects().toArray(CardEffect[]::new)).withGrantingPermanentId(entry.getSourcePermanentId());
        for (UUID watchedId : watched) {
            gameData.temporaryGlobalTriggeredAbilities.add(e.untilEndOfTurn()
                    ? TemporaryGlobalTriggeredAbility.about(entry.getControllerId(), entry.getCard(), ability,
                    watchedId, entry.getSourcePermanentId(), true)
                    : new TemporaryGlobalTriggeredAbility(entry.getControllerId(), entry.getCard(), ability, null,
                    false, true, gameData.turnNumber, null, watchedId, entry.getSourcePermanentId(), null, null,
                    false, false));
        }
        log.info("Game {} - {} watches {} creature(s) for combat damage until its controller's next turn",
                gameData.id, entry.getCard().getName(), watched.size());
    }
}
