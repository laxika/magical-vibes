package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerToughnessFromCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link SetSelfBasePowerToughnessFromCombatOpponentEffect} by locking the combat
 * opponent's current power and toughness into an until-end-of-turn base P/T setting on the source.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SetSelfBasePowerToughnessFromCombatOpponentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetSelfBasePowerToughnessFromCombatOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent combatOpponent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        // CR 608.2h: an object that left the battlefield is used as it last existed there, so its
        // departure snapshot (modifiers applied after the trigger was put on the stack) wins over
        // the values captured when the trigger was put on the stack.
        Permanent departed = combatOpponent != null || entry.getTargetId() == null ? null
                : gameData.departedPermanentSnapshots.values().stream()
                .filter(snapshot -> entry.getTargetId().equals(snapshot.getId()))
                .findFirst().orElse(null);
        if (source == null || (combatOpponent == null && departed == null
                && (entry.getCombatOpponentPowerAtTrigger() == null
                || entry.getCombatOpponentToughnessAtTrigger() == null))) {
            return;
        }

        Permanent lastKnown = combatOpponent != null ? combatOpponent : departed;
        int power = lastKnown == null ? entry.getCombatOpponentPowerAtTrigger()
                : gameQueryService.getEffectivePower(gameData, lastKnown);
        int toughness = lastKnown == null ? entry.getCombatOpponentToughnessAtTrigger()
                : gameQueryService.getEffectiveToughness(gameData, lastKnown);

        source.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
        source.setBasePowerOverride(power);
        source.setBaseToughnessOverride(toughness);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(),
                new SetBasePowerToughnessEffect(power, toughness, GrantScope.SELF),
                source.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        String description = " has base power and toughness " + power + "/" + toughness
                + " until end of turn.";
        gameLogService.append(gameData, GameLog.builder().card(source.getCard()).text(description).build());
        log.info("Game {} - {}{}", gameData.id, source.getCard().getName(), description);
    }
}
