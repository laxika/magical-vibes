package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantColorUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantColorUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantColorUntilEndOfTurnEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // CR 613 layer engine: "becomes [color] until end of turn" is a floating layer-5
        // color effect with its own timestamp — it beats earlier setters (an already
        // attached Nim Deathmantle) and loses to later ones, then wears off at cleanup. The
        // legacy fields are still written for direct Permanent.getEffectiveColor callers; the
        // layered pass seeds them and then replays this effect at its real timestamp.
        if (!e.additive()) {
            target.getTransientColors().clear();
            target.getTransientColors().add(e.color());
            target.setColorOverridden(true);
        }
        // For an additive grant ("in addition to its other colors") the legacy fields are left
        // untouched: seedLegacyColorAndAbilityState folds transientColors into the *seeded* colors
        // before snapshotting, so the query pass would strip them as intrinsic. The floating
        // effect below adds the color after the snapshot, so it registers as a granted color.
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), null, entry.getControllerId(), e,
                target.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        String colorName = e.color().name().charAt(0) + e.color().name().substring(1).toLowerCase();
        String suffix = e.additive() ? " in addition to its other colors until end of turn." : " until end of turn.";
        String logEntry = target.getCard().getName() + " becomes " + colorName + suffix;
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} becomes {} until end of turn", gameData.id, target.getCard().getName(), colorName);
    }
}
