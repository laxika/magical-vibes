package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Prevention effects (CR 615) for damage to a player, from blanket "prevent all damage" effects and
 * protection to the one-shot shields that are used up as they prevent: Circle of Protection-style
 * shields, Healing Grace, Sanctum Guardian, "prevent the next N damage", Channel Harm, Comeuppance,
 * Judgment of Alexander, Eye for an Eye and the Cleric-counting "you may prevent". A shield that is
 * consumed, or carries a rider, is not pure, so meeting another effect always lets the player
 * choose which applies first. Prevention of combat damage only uses the combat forms.
 */
@Component
class PlayerDamagePreventions extends PlayerDamageCandidateSource {

    private static final ReplacementOption.Kind PREVENTION = ReplacementOption.Kind.PREVENTION;

    private final DamagePreventionService prevention;
    private final ObjectProvider<DamageSupport> damageSupport;
    private final List<Rule<PlayerDamageContext>> rules;

    PlayerDamagePreventions(GameQueryService gameQueryService, GameLogService gameLogService,
                            DamagePreventionService prevention, ObjectProvider<DamageSupport> damageSupport) {
        super(gameQueryService, gameLogService);
        this.prevention = prevention;
        this.damageSupport = damageSupport;
        this.rules = List.of(
                preventAll("SourcePrevented", "Prevent the damage from this source",
                        ctx -> !ctx.combat() && gameQueryService.isDamageFromStackEntryPrevented(ctx.gameData(), ctx.entry())),
                preventAll("AllCombatDamagePrevented", "Prevent all combat damage",
                        ctx -> ctx.combat() && ctx.gameData().preventAllCombatDamage
                                && preventable(ctx)).whole(),
                pure("Flying", PREVENTION,
                        ctx -> gameQueryService.playerHasFlying(ctx.gameData(), ctx.playerId())
                                && gameQueryService.isDamageSourceCreature(ctx.gameData(), ctx.entry(), ctx.sourcePermanent())
                                && !gameQueryService.sourceHasKeyword(
                                ctx.gameData(), ctx.entry(), ctx.sourcePermanent(), Keyword.FLYING),
                        ctx -> "Flying prevents the damage",
                        PlayerDamageCandidateSource::prevented,
                        (ctx, result) -> {
                            gameLogService.append(ctx.gameData(), GameLog.cardThen(ctx.source(),
                                    "'s damage to " + ctx.playerName() + " is prevented by flying."));
                            return null;
                        }),
                preventAll("SpellToControllerAndPermanents", "Prevent the spell's damage",
                        ctx -> gameQueryService.isSpellDamageToControllerAndPermanentsPrevented(
                                ctx.gameData(), ctx.entry(), ctx.playerId())),
                preventAll("InstantOrSorceryPrevented", "Prevent damage from instants and sorceries",
                        ctx -> gameQueryService.isDamageFromInstantOrSorcerySpellPrevented(ctx.gameData(), ctx.entry())),
                effect("TargetSpellPrevented", PREVENTION,
                        ctx -> gameQueryService.isDamageFromTargetSpellPrevented(ctx.gameData(), ctx.entry()),
                        ctx -> "Prevent the targeted spell's damage",
                        ctx -> {
                            damageSupport.getObject().gainLifeForTargetSpellDamage(ctx.gameData(), ctx.entry(), ctx.amount());
                            logPrevented(ctx, null);
                            return prevented(ctx);
                        }),
                pure("SpellDamagePrevention", PREVENTION,
                        ctx -> gameQueryService.getSpellDamagePrevention(ctx.gameData(), ctx.entry()) > 0,
                        ctx -> "Prevent " + gameQueryService.getSpellDamagePrevention(ctx.gameData(), ctx.entry())
                                + " of the spell's damage",
                        ctx -> ctx.event().withAmount(
                                ctx.amount() - gameQueryService.getSpellDamagePrevention(ctx.gameData(), ctx.entry())),
                        (ctx, result) -> {
                            gameLogService.append(ctx.gameData(), GameLog.cardThen(ctx.source(),
                                    "'s " + (ctx.amount() - result.amount()) + " damage to " + ctx.playerName()
                                            + " is prevented."));
                            return null;
                        }),
                preventAll("ColorPreventedForTarget", "Prevent damage from sources of this color",
                        ctx -> prevention.isColorDamagePreventedForTarget(
                                ctx.gameData(), ctx.playerId(), ctx.damageSourceColors(), ctx.combat())),
                effect("ChosenSourcePrevented", PREVENTION,
                        ctx -> prevention.isSourceDamagePreventedForPlayer(
                                ctx.gameData(), ctx.playerId(), ctx.damageSourceId(), ctx.combat()),
                        ctx -> "Prevent damage from the chosen source " + ctx.source().getName(),
                        ctx -> {
                            prevention.applySourceDamagePreventionForPlayer(ctx.gameData(), ctx.playerId(),
                                    ctx.damageSourceId(), ctx.amount(), ctx.damageSourceColors(), ctx.combat());
                            return preventedWithCreatureLifeGain(ctx);
                        }),
                preventAll("AttackerDamagePrevented", "Prevent damage from attacking creatures",
                        ctx -> ctx.combat()
                                ? prevention.isCombatDamageFromAttackersPreventedForPlayer(ctx.gameData(), ctx.playerId())
                                && ctx.sourcePermanent() != null && ctx.sourcePermanent().isAttacking()
                                : prevention.isNoncombatDamageFromAttackerPreventedForPlayer(
                                ctx.gameData(), ctx.playerId(), ctx.damageSourceId())),
                preventAll("MatchingSourcePrevented", "Prevent damage from matching sources",
                        ctx -> ctx.combat()
                                ? ctx.sourcePermanent() != null && gameQueryService.isDamageFromMatchingSourcePreventedForPlayer(
                                ctx.gameData(), ctx.playerId(), ctx.sourcePermanent(), true)
                                : gameQueryService.isDamageFromMatchingSourcePreventedForPlayer(
                                ctx.gameData(), ctx.playerId(), ctx.entry())),
                effect("CreatureDamagePrevented", PREVENTION,
                        ctx -> damageSupport.getObject().isGlobalCreaturePreventionForEntry(ctx.gameData(), ctx.entry()),
                        ctx -> "Prevent damage dealt by creatures",
                        this::preventedWithCreatureLifeGain),
                preventAll("SourcePreventedFromDealingDamage", "Prevent all damage the source would deal",
                        ctx -> !ctx.combat() && damageSupport.getObject().isSourcePermanentPreventedFromDealingDamage(
                                ctx.gameData(), ctx.entry())),
                preventAll("ProtectionFromColor", "Protection from the source's color",
                        ctx -> preventable(ctx) && gameQueryService.playerHasProtectionFromColor(ctx.gameData(), ctx.playerId(),
                                gameQueryService.getDamageSourceColor(ctx.gameData(), sourceColor(ctx)))),
                preventAll("ProtectionFromOpponents", "Protection from your opponents",
                        ctx -> preventable(ctx) && gameQueryService.playerHasProtectionFromOpponents(
                                ctx.gameData(), ctx.playerId(), ctx.sourceControllerId())),
                preventAll("ProtectionFromName", "Protection from the chosen name",
                        ctx -> preventable(ctx)
                                && (gameQueryService.playerHasProtectionFromChosenName(
                                ctx.gameData(), ctx.playerId(), ctx.source().getName())
                                || gameQueryService.isDamageFromChosenNamePreventedForController(
                                ctx.gameData(), ctx.playerId(), ctx.source().getName()))),
                preventAll("ProtectionFromCardType", "Protection from the chosen card type",
                        ctx -> preventable(ctx) && gameQueryService.playerHasProtectionFromChosenCardType(
                                ctx.gameData(), ctx.playerId(), ctx.source(), ctx.sourcePermanent())),
                effect("ColorShield", PREVENTION,
                        ctx -> sourceColor(ctx) != null && prevention.hasColorDamagePreventionForPlayer(
                                ctx.gameData(), ctx.playerId(), sourceColor(ctx), ctx.combat()),
                        ctx -> "Prevent the damage from a " + sourceColor(ctx).name().toLowerCase()
                                + " source (one use)",
                        ctx -> {
                            prevention.applyColorDamagePreventionForPlayer(
                                    ctx.gameData(), ctx.playerId(), sourceColor(ctx), ctx.combat());
                            return prevented(ctx);
                        }),
                pure("OpponentSourceReduction", PREVENTION,
                        ctx -> opponentSourceReduction(ctx) < ctx.amount(),
                        ctx -> "Prevent " + (ctx.amount() - opponentSourceReduction(ctx))
                                + " damage from an opponent's source",
                        ctx -> ctx.event().withAmount(opponentSourceReduction(ctx)),
                        PlayerDamageCandidateSource::noLog),
                effect("TargetSourceShield", PREVENTION,
                        ctx -> preventable(ctx) && prevention.hasTargetSourcePreventionShield(
                                ctx.gameData(), ctx.playerId(), ctx.damageSourceId()),
                        ctx -> "Prevent damage from " + ctx.source().getName() + " (Healing Grace)",
                        ctx -> ctx.event().withAmount(prevention.applyTargetSourcePreventionShield(
                                ctx.gameData(), ctx.playerId(), ctx.damageSourceId(), ctx.amount(), ctx.combat()))),
                eyeForAnEye("EyeForAnEye", PlayerDamageContext::damageSourceId, false),
                eyeForAnEye("EyeForAnEyeSourcePermanent", ctx -> ctx.entry().getSourcePermanentId(), true),
                nextSourceShield("NextSourceShield", PlayerDamageContext::damageSourceId, false),
                nextSourceShield("NextSourceShieldBySource", PlayerDamagePreventions::spellOrPermanentSourceId, true),
                effect("ChosenSourceNextDamageShield", PREVENTION,
                        ctx -> prevention.hasChosenSourceNextDamageToAnyTargetShield(
                                ctx.gameData(), ctx.damageSourceId(), ctx.playerId(), ctx.combat()),
                        ctx -> "Use the shield against " + ctx.source().getName() + " (Sanctum Guardian)",
                        ctx -> {
                            int remaining = ctx.combat()
                                    ? prevention.applyChosenSourceNextDamageToAnyTargetShield(ctx.gameData(),
                                    ctx.damageSourceId(), ctx.amount(), ctx.playerId(), true)
                                    : prevention.applyChosenSourceNextDamageToAnyTargetShield(ctx.gameData(),
                                    ctx.damageSourceId(), ctx.amount(), ctx.playerId(), ctx.entry());
                            damageSupport.getObject().processEyeForAnEyeReflections(ctx.gameData());
                            return ctx.event().withAmount(remaining);
                        }),
                effect("ChannelHarm", PREVENTION,
                        ctx -> prevention.hasChannelHarmShield(ctx.gameData(), ctx.playerId(), ctx.sourceControllerId()),
                        ctx -> "Channel Harm: prevent the damage",
                        ctx -> ctx.event().withAmount(prevention.applyChannelHarmPrevention(
                                ctx.gameData(), ctx.playerId(), ctx.sourceControllerId(), ctx.amount()))),
                effect("Comeuppance", PREVENTION,
                        ctx -> prevention.hasComeuppanceShield(
                                ctx.gameData(), ctx.playerId(), ctx.sourceControllerId(), ctx.combat()),
                        ctx -> "Comeuppance: prevent the damage",
                        ctx -> {
                            int remaining = prevention.applyComeuppancePrevention(ctx.gameData(),
                                    ctx.playerId(), ctx.amount(), ctx.source(), ctx.sourcePermanent(),
                                    ctx.sourceControllerId(), ctx.combat());
                            damageSupport.getObject().processPendingRedirectDamage(ctx.gameData());
                            return ctx.event().withAmount(remaining);
                        }),
                effect("JudgmentOfAlexander", PREVENTION,
                        ctx -> prevention.hasJudgmentOfAlexanderShield(ctx.gameData(), ctx.playerId(),
                                ctx.source(), ctx.sourcePermanent(), ctx.sourceControllerId(), ctx.combat()),
                        ctx -> "Judgment of Alexander: prevent the damage",
                        ctx -> ctx.event().withAmount(prevention.applyJudgmentOfAlexanderPrevention(ctx.gameData(),
                                ctx.playerId(), ctx.amount(), ctx.source(), ctx.sourcePermanent(),
                                ctx.sourceControllerId(), ctx.combat()))),
                effect("CombatDamagePreventionTokenShield", PREVENTION,
                        ctx -> ctx.combat() && preventable(ctx)
                                && ctx.gameData().combatDamagePreventionTokenShields.get(ctx.playerId()) != null,
                        ctx -> "Prevent the combat damage and create that many tokens",
                        ctx -> ctx.event().withAmount(prevention.applyCombatDamagePreventionTokenShield(
                                ctx.gameData(), ctx.playerId(), ctx.amount()))).whole(),
                playerFlag("AllCombatDamageToPlayersPrevented", "Prevent all combat damage to players",
                        ctx -> ctx.combat() && ctx.gameData().preventAllCombatDamageToPlayers),
                playerFlag("AllCombatDamageToPlayerPrevented", "Prevent all combat damage to you and your planeswalkers",
                        ctx -> ctx.combat() && ctx.gameData().playersWithAllCombatDamageToPlayerAndPlaneswalkersPrevented
                                .contains(ctx.playerId())),
                playerFlag("AllDamagePrevented", "Prevent all damage dealt to you",
                        ctx -> ctx.gameData().playersWithAllDamagePrevented.contains(ctx.playerId())),
                playerFlag("AllPlayerDamagePrevented", "Prevent all damage dealt to you this turn",
                        ctx -> ctx.gameData().playersWithAllPlayerDamagePrevented.contains(ctx.playerId())),
                playerFlag("AllPlayerDamagePreventedUntilNextTurn", "Prevent all damage dealt to you until your next turn",
                        ctx -> ctx.gameData().playersWithAllPlayerDamagePreventedUntilNextTurn.contains(ctx.playerId())),
                pure("HalfDamagePrevention", PREVENTION,
                        ctx -> preventable(ctx) && prevention.hasHalfDamagePrevention(ctx.gameData(), ctx.playerId()),
                        ctx -> "Prevent half the damage",
                        ctx -> ctx.event().withAmount(
                                prevention.applyHalfDamagePrevention(ctx.gameData(), ctx.playerId(), ctx.amount())),
                        PlayerDamageCandidateSource::noLog),
                pure("ControllerAndCreaturesPerSource", PREVENTION,
                        ctx -> preventable(ctx) && controllerAndCreaturesPrevented(ctx) > 0,
                        ctx -> "Prevent " + controllerAndCreaturesPrevented(ctx) + " of the damage",
                        ctx -> ctx.event().withAmount(ctx.amount() - controllerAndCreaturesPrevented(ctx)),
                        PlayerDamageCandidateSource::noLog),
                effect("GlobalPreventionShield", PREVENTION,
                        ctx -> preventable(ctx) && ctx.gameData().globalDamagePreventionShield > 0,
                        ctx -> "Prevent damage with the shared shield",
                        ctx -> ctx.event().withAmount(prevention.applyGlobalPreventionShield(
                                ctx.gameData(), ctx.amount(), ctx.combat()))).whole(),
                effect("PreventionLifeGainShield", PREVENTION,
                        ctx -> preventable(ctx) && prevention.hasDamagePreventionLifeGainShield(ctx.gameData(), ctx.playerId()),
                        ctx -> "Prevent damage and gain life",
                        ctx -> ctx.event().withAmount(prevention.applyDamagePreventionLifeGainShield(
                                ctx.gameData(), ctx.playerId(), ctx.amount(), ctx.combat()))).whole(),
                effect("PreventNextCombatDamageShield", PREVENTION,
                        ctx -> ctx.combat() && preventable(ctx)
                                && ctx.gameData().playerCombatDamagePreventionShields.getOrDefault(ctx.playerId(), 0) > 0,
                        ctx -> "Prevent the next " + ctx.gameData().playerCombatDamagePreventionShields.get(ctx.playerId())
                                + " combat damage to you",
                        ctx -> ctx.event().withAmount(prevention.applyPlayerCombatDamagePreventionShield(
                                ctx.gameData(), ctx.playerId(), ctx.amount()))).whole(),
                effect("PreventNextDamageShield", PREVENTION,
                        ctx -> preventable(ctx)
                                && ctx.gameData().playerDamagePreventionShields.getOrDefault(ctx.playerId(), 0) > 0,
                        ctx -> "Prevent the next " + ctx.gameData().playerDamagePreventionShields.get(ctx.playerId())
                                + " damage to you",
                        ctx -> ctx.event().withAmount(prevention.applyPlayerDamagePreventionShield(
                                ctx.gameData(), ctx.playerId(), ctx.amount()))).whole(),
                playerFlag("NoncombatControllerAndPermanentsPrevented", "Prevent noncombat damage to you",
                        ctx -> !ctx.combat() && prevention.hasControllerAndPermanentsNoncombatDamagePrevention(
                                ctx.gameData(), ctx.playerId())),
                new Rule<PlayerDamageContext>("ClericPrevention", PREVENTION, false,
                        ctx -> !prevention.clericPreventionSources(ctx.gameData()).isEmpty() && preventable(ctx)
                                ? ONCE : List.of(),
                        (ctx, instance) -> "Let Clerics prevent some of the damage",
                        (ctx, instance) -> ctx.event(),
                        (ctx, instance) -> prevention.queueClericPreventionChoice(ctx.gameData(), ctx.entry(),
                                ctx.playerId(), ctx.amount(), ctx.combat(),
                                prevention.clericPreventionSources(ctx.gameData()))
                                ? new ApplyResult.Suspended<>(ctx.event())
                                : new ApplyResult.Applied<>(ctx.event())));
    }

    @Override
    protected List<Rule<PlayerDamageContext>> rules() {
        return rules;
    }

    private boolean preventable(PlayerDamageContext ctx) {
        return gameQueryService.isDamagePreventable(ctx.gameData(), ctx.combat());
    }

    /** The source's color for protection: an attacker's or blocker's current color, else the card's. */
    private CardColor sourceColor(PlayerDamageContext ctx) {
        return ctx.combat() && ctx.sourcePermanent() != null
                ? gameQueryService.getEffectiveColor(ctx.gameData(), ctx.sourcePermanent())
                : ctx.source().getColor();
    }

    private Rule<PlayerDamageContext> preventAll(String family, String label, Predicate<PlayerDamageContext> applies) {
        return pure(family, PREVENTION, applies, ctx -> label, PlayerDamageCandidateSource::prevented, this::logPrevented);
    }

    /** A blanket prevention for the damaged player; a prevented-damage trigger sees what it stops. */
    private Rule<PlayerDamageContext> playerFlag(String family, String label, Predicate<PlayerDamageContext> applies) {
        return pure(family, PREVENTION,
                ctx -> preventable(ctx) && applies.test(ctx),
                ctx -> label,
                PlayerDamageCandidateSource::prevented,
                (ctx, result) -> {
                    prevention.notifyControllerDamagePrevented(ctx.gameData(), ctx.playerId(), ctx.amount());
                    return null;
                }).whole();
    }

    /**
     * Eye for an Eye watches the source by its damage identity or by the permanent itself;
     * {@code skipSameSource} keeps the second reading from offering the same shield twice.
     */
    private Rule<PlayerDamageContext> eyeForAnEye(String family, Function<PlayerDamageContext, UUID> sourceId,
                                                  boolean skipSameSource) {
        return effect(family, ReplacementOption.Kind.REPLACEMENT,
                ctx -> !(skipSameSource && ctx.damageSourceId().equals(sourceId.apply(ctx)))
                        && prevention.hasEyeForAnEyeShield(ctx.gameData(), ctx.playerId(), sourceId.apply(ctx)),
                ctx -> "Eye for an Eye: " + ctx.source().getName() + "'s controller is dealt that much damage too",
                ctx -> {
                    prevention.applyEyeForAnEyeReflection(
                            ctx.gameData(), ctx.playerId(), sourceId.apply(ctx), ctx.amount());
                    return ctx.event();
                });
    }

    /**
     * Circle of Protection-style shields against the next damage from a chosen source. The source can
     * be named by its damage identity or by the spell or permanent itself; {@code skipSameSource}
     * keeps the second reading from offering the same shield twice.
     */
    private Rule<PlayerDamageContext> nextSourceShield(String family, Function<PlayerDamageContext, UUID> sourceId,
                                                       boolean skipSameSource) {
        return effect(family, PREVENTION,
                ctx -> preventable(ctx)
                        && !(skipSameSource && sourceId.apply(ctx).equals(ctx.damageSourceId()))
                        && prevention.hasPlayerNextSourceDamageShield(ctx.gameData(), ctx.playerId(),
                        sourceId.apply(ctx), ctx.combat(), ctx.combat() ? null : ctx.source(), unblocked(ctx)),
                ctx -> "Prevent the next damage from " + ctx.source().getName() + " (one use)",
                ctx -> {
                    int remaining = prevention.applyPlayerNextSourceDamageShield(ctx.gameData(), ctx.playerId(),
                            sourceId.apply(ctx), ctx.amount(), ctx.combat(), ctx.combat() ? null : ctx.source(),
                            unblocked(ctx));
                    damageSupport.getObject().processEyeForAnEyeReflections(ctx.gameData());
                    return ctx.event().withAmount(remaining);
                });
    }

    private boolean unblocked(PlayerDamageContext ctx) {
        return ctx.combat() && prevention.isUnblockedCreatureSource(ctx.gameData(), ctx.damageSourceId());
    }

    private DamageEvent preventedWithCreatureLifeGain(PlayerDamageContext ctx) {
        damageSupport.getObject().applyGlobalCreaturePreventionLifeGain(
                ctx.gameData(), ctx.entry(), ctx.amount(), ctx.playerId());
        logPrevented(ctx, null);
        return prevented(ctx);
    }

    private int opponentSourceReduction(PlayerDamageContext ctx) {
        return prevention.applyOpponentSourceDamageReduction(
                ctx.gameData(), ctx.playerId(), ctx.entry().getControllerId(), ctx.amount(), ctx.combat());
    }

    private int controllerAndCreaturesPrevented(PlayerDamageContext ctx) {
        return prevention.controllerAndCreaturesPerSourcePrevention(
                ctx.gameData(), ctx.playerId(), ctx.amount(), ctx.combat(), ctx.sourcePermanent(), null);
    }

    private static UUID spellOrPermanentSourceId(PlayerDamageContext ctx) {
        return ctx.entry().getSourcePermanentId() != null
                ? ctx.entry().getSourcePermanentId()
                : ctx.source().getId();
    }
}
