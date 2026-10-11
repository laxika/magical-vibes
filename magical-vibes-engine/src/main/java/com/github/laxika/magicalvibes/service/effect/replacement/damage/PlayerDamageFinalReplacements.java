package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CrumblingSanctuaryDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.NefariousLichDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndExileFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndMillEffect;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatDamageService;
import com.github.laxika.magicalvibes.service.effect.DamagePreventionReplacementSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Effects that take over damage to a player wholesale or by a fixed amount: Urza's Armor,
 * all-but-one, Purity, Hostility, Glacial Chasm, Angel of Suffering, Nefarious Lich, Crumbling
 * Sanctuary, Immortal Coil, the opponent-mill replacement, Delaying Shield-style counters, Soul
 * Echo and permanents that take counters for preventing damage to their controller. Effects
 * printed on several permanents offer one option per permanent, so a player with two Delaying
 * Shields chooses which one takes the counters.
 */
@Component
class PlayerDamageFinalReplacements extends PlayerDamageCandidateSource {

    private static final ReplacementOption.Kind PREVENTION = ReplacementOption.Kind.PREVENTION;
    private static final ReplacementOption.Kind REPLACEMENT = ReplacementOption.Kind.REPLACEMENT;

    private final DamagePreventionService prevention;
    private final DamagePreventionReplacementSupport preventionReplacementSupport;
    private final ObjectProvider<DamageSupport> damageSupport;
    private final LifeSupport lifeSupport;
    private final GraveyardService graveyardService;
    private final ObjectProvider<PermanentControlSupport> permanentControlSupport;
    private final ObjectProvider<CombatDamageService> combatDamageService;
    private final List<Rule<PlayerDamageContext>> rules;

    PlayerDamageFinalReplacements(GameQueryService gameQueryService, GameLogService gameLogService,
                                  DamagePreventionService prevention,
                                  DamagePreventionReplacementSupport preventionReplacementSupport,
                                  ObjectProvider<DamageSupport> damageSupport, LifeSupport lifeSupport,
                                  GraveyardService graveyardService,
                                  ObjectProvider<PermanentControlSupport> permanentControlSupport,
                                  ObjectProvider<CombatDamageService> combatDamageService) {
        super(gameQueryService, gameLogService);
        this.prevention = prevention;
        this.preventionReplacementSupport = preventionReplacementSupport;
        this.damageSupport = damageSupport;
        this.lifeSupport = lifeSupport;
        this.graveyardService = graveyardService;
        this.permanentControlSupport = permanentControlSupport;
        this.combatDamageService = combatDamageService;
        this.rules = List.of(
                partialPrevention("FixedPerSourcePrevention", ctx -> prevention.applyControllerFixedPerSourceDamagePrevention(
                        ctx.gameData(), ctx.playerId(), ctx.amount(),
                        gameQueryService.isDamageSourceCreature(ctx.gameData(), ctx.entry(), ctx.sourcePermanent()),
                        gameQueryService.isDamageSourceArtifact(ctx.gameData(), ctx.entry(), ctx.sourcePermanent()),
                        ctx.damageSourceColors(), ctx.combat(), ctx.combat() ? ctx.damageSourceId() : null)),
                partialPrevention("AllButOnePrevention", ctx -> prevention.applyAllButOneDamagePrevention(
                        ctx.gameData(), ctx.playerId(), ctx.amount(), ctx.combat())),
                effect("NoncombatPreventionLifeGain", PREVENTION,
                        ctx -> !ctx.combat() && prevention.applyControllerNoncombatDamagePrevention(
                                ctx.gameData(), ctx.playerId(), ctx.amount()) > 0,
                        ctx -> "Prevent the noncombat damage and gain that much life (Purity)",
                        ctx -> {
                            int prevented = prevention.applyControllerNoncombatDamagePrevention(
                                    ctx.gameData(), ctx.playerId(), ctx.amount());
                            gameLogService.append(ctx.gameData(), GameLog.cardThen(ctx.source(),
                                    "'s " + prevented + " damage to " + ctx.playerName() + " is prevented."));
                            lifeSupport.applyGainLife(ctx.gameData(), ctx.playerId(), prevented, "prevented damage");
                            return ctx.event().withAmount(ctx.amount() - prevented);
                        }),
                effect("OpponentDamageMill", REPLACEMENT,
                        ctx -> prevention.hasControllerOpponentDamageMillReplacement(
                                ctx.gameData(), ctx.sourceControllerId(), ctx.playerId(), ctx.amount()),
                        ctx -> "Mill instead of being dealt the damage",
                        ctx -> {
                            gameLogService.append(ctx.gameData(), GameLog.cardThen(ctx.source(),
                                    "'s " + ctx.amount() + " damage to " + ctx.playerName()
                                            + " is prevented and replaced with milling."));
                            for (UUID opponentId : ctx.gameData().orderedPlayerIds) {
                                if (!opponentId.equals(ctx.sourceControllerId())) {
                                    graveyardService.resolveMillPlayer(ctx.gameData(), opponentId, ctx.amount());
                                }
                            }
                            return prevented(ctx);
                        }),
                effect("CombatDamageMill", REPLACEMENT,
                        ctx -> ctx.combat() && ctx.sourcePermanent() != null
                                && combatDamageService.getObject().hasCombatDamageMillReplacement(
                                ctx.gameData(), ctx.sourcePermanent()),
                        ctx -> ctx.source().getName() + ": mill that many cards instead",
                        ctx -> {
                            combatDamageService.getObject().replaceCombatDamageWithMill(
                                    ctx.gameData(), ctx.sourcePermanent(), ctx.playerId(), ctx.amount());
                            return prevented(ctx);
                        }),
                effect("SokraticDialogue", PREVENTION,
                        ctx -> ctx.combat() && prevention.hasSokraticDialogue(ctx.gameData(), ctx.sourcePermanent()),
                        ctx -> "Prevent the combat damage; each of you draws half that many cards",
                        ctx -> prevention.applySokraticDialogue(ctx.gameData(), ctx.sourcePermanent(), ctx.amount(),
                                ctx.sourceControllerId(), ctx.playerId()) ? prevented(ctx) : ctx.event()),
                effect("SpellDamageToOpponentPrevention", PREVENTION,
                        ctx -> !ctx.combat() && prevention.findSpellDamageToOpponentPrevention(
                                ctx.gameData(), ctx.entry(), ctx.playerId(), ctx.amount()) != null,
                        ctx -> "Prevent the spell's damage and create tokens (Hostility)",
                        ctx -> {
                            var hostility = prevention.findSpellDamageToOpponentPrevention(
                                    ctx.gameData(), ctx.entry(), ctx.playerId(), ctx.amount());
                            logPrevented(ctx, null);
                            permanentControlSupport.getObject().applyCreateToken(ctx.gameData(),
                                    ctx.entry().getControllerId(), hostility.token(), ctx.amount(),
                                    ctx.entry().getCard().getSetCode());
                            return prevented(ctx);
                        }),
                pure("ControllerAllDamagePrevention", PREVENTION,
                        ctx -> damageSupport.getObject().applyControllerAllDamagePrevention(
                                ctx.gameData(), ctx.playerId(), ctx.amount(), ctx.combat()) > 0,
                        ctx -> "Prevent all damage dealt to you (Glacial Chasm)",
                        PlayerDamageCandidateSource::prevented,
                        this::logPrevented).whole(),
                effect("PreventAndMill", REPLACEMENT,
                        ctx -> controlsEffect(ctx, PreventAllDamageToControllerAndMillEffect.class),
                        ctx -> "Angel of Suffering: mill twice that many cards instead",
                        ctx -> {
                            int prevented = damageSupport.getObject().applyAngelOfSufferingReplacement(
                                    ctx.gameData(), ctx.playerId(), ctx.amount(), ctx.amount());
                            if (prevented > 0) {
                                gameLogService.append(ctx.gameData(), GameLog.cardThen(ctx.source(),
                                        "'s " + prevented + " damage to " + ctx.playerName()
                                                + " is prevented by Angel of Suffering."));
                            }
                            return ctx.event().withAmount(ctx.amount() - prevented);
                        }).whole(),
                wholeEventReplacement("NefariousLich", "Nefarious Lich: exile that many cards from your graveyard instead",
                        ctx -> printedOnBattlefield(ctx.gameData().playerBattlefields.getOrDefault(ctx.playerId(), List.of()),
                                NefariousLichDamageReplacementEffect.class),
                        ctx -> damageSupport.getObject().applyNefariousLichReplacement(
                                ctx.gameData(), ctx.playerId(), ctx.amount()),
                        " is replaced by Nefarious Lich.").whole(),
                wholeEventReplacement("CrumblingSanctuary", "Crumbling Sanctuary: exile cards from your library instead",
                        ctx -> ctx.gameData().playerBattlefields.values().stream().anyMatch(battlefield ->
                                printedOnBattlefield(battlefield, CrumblingSanctuaryDamageReplacementEffect.class)),
                        ctx -> damageSupport.getObject().applyCrumblingSanctuaryReplacement(
                                ctx.gameData(), ctx.playerId(), ctx.amount()),
                        " is replaced by Crumbling Sanctuary.").whole(),
                effect("PreventAndExileFromGraveyard", PREVENTION,
                        ctx -> gameQueryService.isDamagePreventable(ctx.gameData(), ctx.combat())
                                && printedOnBattlefield(ctx.gameData().playerBattlefields.getOrDefault(
                                ctx.playerId(), List.of()), PreventAllDamageToControllerAndExileFromGraveyardEffect.class),
                        ctx -> "Immortal Coil: prevent the damage and exile that many cards from your graveyard",
                        ctx -> {
                            int prevented = damageSupport.getObject().applyImmortalCoilPrevention(
                                    ctx.gameData(), ctx.playerId(), ctx.amount(), ctx.combat());
                            logPrevented(ctx, null);
                            return ctx.event().withAmount(ctx.amount() - prevented);
                        }).whole(),
                perPermanent("CounterReplacement",
                        ctx -> damageSupport.getObject().damageToControllerCounterReplacementSources(
                                ctx.gameData(), ctx.playerId()),
                        permanent -> permanent.getCard().getName() + ": put counters on it instead",
                        (ctx, permanent) -> damageSupport.getObject().applyDamageToControllerCounterReplacement(
                                ctx.gameData(), ctx.playerId(), permanent, ctx.amount())).whole(),
                perPermanent("SoulEcho",
                        ctx -> ctx.gameData().playerBattlefields.getOrDefault(ctx.playerId(), List.of()).stream()
                                .filter(Permanent::isEchoDamageRedirectionActive).toList(),
                        permanent -> permanent.getCard().getName() + ": remove echo counters instead",
                        (ctx, permanent) -> damageSupport.getObject().applySoulEchoCounterRemoval(
                                ctx.gameData(), ctx.playerId(), permanent, ctx.amount())).whole(),
                perPermanent("PreventAndPutCounterOnSelf",
                        ctx -> gameQueryService.isDamagePreventable(ctx.gameData(), ctx.combat())
                                ? preventionReplacementSupport.damageToControllerCounterOnSelfSources(
                                ctx.gameData(), ctx.playerId())
                                : List.of(),
                        permanent -> permanent.getCard().getName() + ": prevent the damage and put a counter on it",
                        (ctx, permanent) -> preventionReplacementSupport.preventDamageToControllerAndPutCounterOnSelf(
                                ctx.gameData(), permanent, ctx.amount())).whole());
    }

    @Override
    protected List<Rule<PlayerDamageContext>> rules() {
        return rules;
    }

    /** Urza's Armor-style prevention of part of the damage, logged as "N of Source's damage ...". */
    private Rule<PlayerDamageContext> partialPrevention(String family, ToIntFunction<PlayerDamageContext> prevented) {
        return pure(family, PREVENTION,
                ctx -> prevented.applyAsInt(ctx) > 0,
                ctx -> "Prevent " + prevented.applyAsInt(ctx) + " of the damage",
                ctx -> ctx.event().withAmount(ctx.amount() - prevented.applyAsInt(ctx)),
                (ctx, result) -> {
                    gameLogService.append(ctx.gameData(), GameLog.textCardText(
                            (ctx.amount() - result.amount()) + " of ", ctx.source(),
                            "'s damage to " + ctx.playerName() + " is prevented."));
                    return null;
                });
    }

    private Rule<PlayerDamageContext> wholeEventReplacement(String family, String label,
                                       Predicate<PlayerDamageContext> applies,
                                       ToIntFunction<PlayerDamageContext> replace, String logSuffix) {
        return effect(family, REPLACEMENT, applies, ctx -> label, ctx -> {
            int replaced = replace.applyAsInt(ctx);
            gameLogService.append(ctx.gameData(), GameLog.cardThen(ctx.source(),
                    "'s " + replaced + " damage to " + ctx.playerName() + logSuffix));
            return ctx.event().withAmount(ctx.amount() - replaced);
        });
    }

    /** One option per permanent carrying the effect; applying it returns how much damage it took. */
    private Rule<PlayerDamageContext> perPermanent(String family, Function<PlayerDamageContext, List<Permanent>> sources,
                              Function<Permanent, String> label,
                              BiFunction<PlayerDamageContext, Permanent, Integer> replace) {
        return new Rule<PlayerDamageContext>(family, REPLACEMENT, false,
                ctx -> sources.apply(ctx).stream().map(permanent -> permanent.getId().toString()).toList(),
                (ctx, instance) -> label.apply(permanent(ctx, instance)),
                (ctx, instance) -> ctx.event(),
                (ctx, instance) -> new ApplyResult.Applied<>(ctx.event().withAmount(
                        ctx.amount() - replace.apply(ctx, permanent(ctx, instance)))));
    }

    private Permanent permanent(PlayerDamageContext ctx, String instance) {
        return gameQueryService.findPermanentById(ctx.gameData(), UUID.fromString(instance));
    }

    private boolean controlsEffect(PlayerDamageContext ctx,
                                   Class<? extends CardEffect> effectType) {
        return ctx.gameData().playerBattlefields.getOrDefault(ctx.playerId(), List.of()).stream()
                .anyMatch(permanent -> gameQueryService.hasActiveStaticEffect(ctx.gameData(), permanent, effectType));
    }

    private static boolean printedOnBattlefield(List<Permanent> battlefield,
                                                Class<? extends CardEffect> effectType) {
        return battlefield.stream().anyMatch(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(effectType::isInstance));
    }
}
