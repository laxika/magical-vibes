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
        if (source == null || combatOpponent == null) {
            return;
        }

        int power = gameQueryService.getEffectivePower(gameData, combatOpponent);
        int toughness = gameQueryService.getEffectiveToughness(gameData, combatOpponent);

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
