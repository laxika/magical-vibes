package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/** Shared base for the families of effects that modify damage to a permanent. */
abstract class PermanentDamageCandidateSource extends DamageCandidateSource<PermanentDamageContext> {

    protected final GameQueryService gameQueryService;
    protected final GameLogService gameLogService;

    protected PermanentDamageCandidateSource(GameQueryService gameQueryService, GameLogService gameLogService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
    }

    protected static Rule<PermanentDamageContext> pure(String family, ReplacementOption.Kind kind,
                                                       Predicate<PermanentDamageContext> applies,
                                                       Function<PermanentDamageContext, String> label,
                                                       Function<PermanentDamageContext, DamageEvent> transform,
                                                       BiFunction<PermanentDamageContext, DamageEvent, Void> log) {
        return pureRule(family, kind, applies, label, transform, log);
    }

    protected static Rule<PermanentDamageContext> effect(String family, ReplacementOption.Kind kind,
                                                         Predicate<PermanentDamageContext> applies,
                                                         Function<PermanentDamageContext, String> label,
                                                         Function<PermanentDamageContext, DamageEvent> apply) {
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
        return event.toPermanent();
    }

    @Override
    protected PermanentDamageContext context(GameData gameData, DamageEvent event) {
        Permanent target = gameQueryService.findPermanentById(gameData, event.recipientPermanentId());
        if (target == null) {
            return null;
        }
        StackEntry entry = event.entry();
        Permanent explicitSource = event.explicitSource();
        Permanent damageSource = explicitSource != null ? explicitSource
                : entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (explicitSource == null && damageSource != null && entry.getDamageSourceCard() != null
                && entry.getDamageSourceCard() != damageSource.getCard()) {
            // The ability belongs to this permanent but another object deals the damage (an Equipment
            // that granted the ability), so restrictions on the permanent's own damage don't apply.
            damageSource = null;
        }
        UUID sourceControllerId = gameQueryService.findDamageSourceController(gameData, entry, damageSource);
        Card sourceCard = damageSource == null ? entry.getEffectiveDamageSourceCard() : damageSource.getCard();
        UUID sourcePermanentId = explicitSource != null ? explicitSource.getId() : entry.getSourcePermanentId();
        UUID damageSourceKey = sourcePermanentId != null ? sourcePermanentId
                : entry.getEffectiveDamageSourceCard().getId();
        Set<CardColor> sourceColors = explicitSource != null
                ? gameQueryService.getEffectiveColors(gameData, explicitSource)
                : cardColors(entry.getEffectiveDamageSourceCard());
        return new PermanentDamageContext(gameData, event, target,
                gameQueryService.findPermanentController(gameData, target.getId()), damageSource,
                sourceControllerId, sourceCard, sourcePermanentId, damageSourceKey,
                gameQueryService.getDamageSourceColors(gameData, sourceColors));
    }

    private static Set<CardColor> cardColors(Card card) {
        if (card == null) return Set.of();
        Set<CardColor> colors = new HashSet<>(card.getColors());
        if (card.getColor() != null) colors.add(card.getColor());
        return colors;
    }

    /** "Damage to Permanent is prevented." */
    protected Void logPrevented(PermanentDamageContext ctx, DamageEvent ignored) {
        gameLogService.append(ctx.gameData(), GameLog.textCardText("Damage to ", ctx.target().getCard(), " is prevented."));
        return null;
    }

    protected static Void noLog(PermanentDamageContext ctx, DamageEvent ignored) {
        return null;
    }
}
