package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetBasePowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetBasePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetBasePowerToughnessEffect) effect;
        // SELF scope ("this creature has base P/T X/Y until end of turn", e.g. Marsh Flitter)
        // resolves against the source; TARGET scope resolves against the chosen target.
        UUID id = e.scope() == GrantScope.SELF ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, id);
        if (target == null) {
            return;
        }

        // CR 613 layer engine: a one-shot base-P/T setter is a floating layer-7b effect with
        // its own timestamp — of all applicable 7b setters (auras, animations, other one-shots)
        // the latest timestamp wins in the layered pass. The legacy fields are still written
        // for direct Permanent readers (views, last-known-information); the floating instance
        // is what drives precedence.
        // The legacy UEOT fields are an all-or-nothing pair, so a partial setter
        // ("has base toughness 1") skips them entirely and rides on the floating 7b entry alone,
        // which carries per-component nulls.
        if (e.power() != null && e.toughness() != null) {
            target.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
            target.setBasePowerOverride(e.power());
            target.setBaseToughnessOverride(e.toughness());
        }
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(),
                e, target.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        String description = e.power() == null
                ? " has base toughness " + e.toughness() + " until end of turn."
                : e.toughness() == null
                ? " has base power " + e.power() + " until end of turn."
                : " has base power and toughness " + e.power() + "/" + e.toughness() + " until end of turn.";
        gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(description).build());

        log.info("Game {} - {}{}", gameData.id, target.getCard().getName(), description);
    }
}
