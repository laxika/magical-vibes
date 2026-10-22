package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/** Shared base for the families of effects that modify damage to a player. */
abstract class PlayerDamageCandidateSource extends DamageCandidateSource<PlayerDamageContext> {

    protected final GameQueryService gameQueryService;
    protected final GameLogService gameLogService;

    protected PlayerDamageCandidateSource(GameQueryService gameQueryService, GameLogService gameLogService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
    }

    protected static Rule<PlayerDamageContext> pure(String family, ReplacementOption.Kind kind,
                                                    Predicate<PlayerDamageContext> applies,
                                                    Function<PlayerDamageContext, String> label,
                                                    Function<PlayerDamageContext, DamageEvent> transform,
                                                    BiFunction<PlayerDamageContext, DamageEvent, Void> log) {
        return pureRule(family, kind, applies, label, transform, log);
    }

    protected static Rule<PlayerDamageContext> effect(String family, ReplacementOption.Kind kind,
                                                      Predicate<PlayerDamageContext> applies,
                                                      Function<PlayerDamageContext, String> label,
                                                      Function<PlayerDamageContext, DamageEvent> apply) {
        return effectRule(family, kind, applies, label, apply);
    }

    @Override
    protected Permanent findPermanent(GameData gameData, UUID permanentId) {
        return gameQueryService.findPermanentById(gameData, permanentId);
    }

    @Override
    protected UUID controllerOf(GameData gameData, UUID permanentId) {
        return gameQueryService.findPermanentController(gameData, permanentId);
    }

    @Override
    protected boolean handles(DamageEvent event) {
        return !event.toPermanent();
    }

    @Override
    protected PlayerDamageContext context(GameData gameData, DamageEvent event) {
        StackEntry entry = event.entry();
        Permanent sourcePermanent = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID sourceControllerId = gameQueryService.findDamageSourceController(gameData, entry, sourcePermanent);
        Set<CardColor> sourceColors = sourcePermanent == null
                ? gameQueryService.getEffectiveCardColors(gameData, entry.getEffectiveDamageSourceCard())
                : gameQueryService.getEffectiveColors(gameData, sourcePermanent);
        UUID damageSourceId = sourcePermanent != null ? sourcePermanent.getId()
                : entry.getSourcePermanentId() != null ? entry.getSourcePermanentId()
                : entry.getCard().getId();
        return new PlayerDamageContext(gameData, event, entry.getEffectiveDamageSourceCard(), sourcePermanent,
                sourceControllerId, gameQueryService.getDamageSourceColors(gameData, sourceColors), damageSourceId);
    }

    /** "Source's damage to Player is prevented." */
    protected Void logPrevented(PlayerDamageContext ctx, DamageEvent ignored) {
        gameLogService.append(ctx.gameData(), GameLog.cardThen(ctx.source(),
                "'s damage to " + ctx.playerName() + " is prevented."));
        return null;
    }

    protected static Void noLog(PlayerDamageContext ctx, DamageEvent ignored) {
        return null;
    }
}
