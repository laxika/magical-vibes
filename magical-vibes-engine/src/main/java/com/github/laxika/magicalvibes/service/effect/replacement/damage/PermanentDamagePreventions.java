package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.CreatureDamageStep;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatDamageService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Prevention effects (CR 615) for damage to a permanent: the source- and spell-based preventions,
 * protection-like color and name effects, the static {@code PreventDamageToRecipientsEffect}s, Swans of Bryn
 * Argoll, Gloom Surgeon, Comeuppance for planeswalkers, Dralnu's sacrifice replacement, and every step of
 * {@link DamagePreventionService#creatureDamageSteps()} (counters, shields, Temper, …). Steps that
 * don't care about the source are asked once about all the combat damage the permanent is dealt.
 */
@Component
class PermanentDamagePreventions extends PermanentDamageCandidateSource {

    private static final ReplacementOption.Kind PREVENTION = ReplacementOption.Kind.PREVENTION;

    private final DamagePreventionService prevention;
    private final ObjectProvider<DamageSupport> damageSupport;
    private final ObjectProvider<CombatDamageService> combatDamageService;
    private volatile List<Rule<PermanentDamageContext>> rules;

    /** Steps about the source of the damage; every other step is about all the damage the permanent is dealt. */
    private static final Set<String> PER_SOURCE_STEPS = Set.of("CombatDamageToPlusOneCounter",
            "ControllerAndCreaturesPerSource", "AllButOneToHero",
            "ArtifactToEnchantedCreature", "ArtifactToSelf", "CreatureSourceToSelf", "DesertsToCamelOrBanded",
            "SelfDamagePrevention", "ControlledCreaturesReduction", "HalfDamage",
            "ControlledCreatureDamageLimit");

    PermanentDamagePreventions(GameQueryService gameQueryService, GameLogService gameLogService,
                               DamagePreventionService prevention, ObjectProvider<DamageSupport> damageSupport,
                               ObjectProvider<CombatDamageService> combatDamageService) {
        super(gameQueryService, gameLogService);
        this.prevention = prevention;
        this.damageSupport = damageSupport;
        this.combatDamageService = combatDamageService;
    }

    private int opponentSourceReduction(PermanentDamageContext ctx) {
        return prevention.applyOpponentSourceDamageReductionToPermanent(
                ctx.gameData(), ctx.targetControllerId(), ctx.sourceControllerId(), ctx.amount(), ctx.combat());
    }

    @Override
    protected List<Rule<PermanentDamageContext>> rules() {
        List<Rule<PermanentDamageContext>> built = rules;
        if (built == null) {
            built = buildRules();
            rules = built;
        }
        return built;
    }

    private List<Rule<PermanentDamageContext>> buildRules() {
        List<Rule<PermanentDamageContext>> all = new ArrayList<>();
        all.add(pure("SourcePreventedFromDealing", PREVENTION,
                ctx -> !ctx.combat() && preventionSource(ctx) != null
                        && gameQueryService.isPreventedFromDealingDamage(
                        ctx.gameData(), preventionSource(ctx), false, false),
                ctx -> "Prevent all damage " + ctx.nameShownToChooser(preventionSource(ctx)) + " would deal",
                PermanentDamageCandidateSource::prevented,
                (ctx, result) -> logSourcePrevented(ctx)));
        all.add(pure("SourceDamagePrevented", PREVENTION,
                ctx -> !ctx.combat() && (preventionSource(ctx) != null
                        ? gameQueryService.isDamageFromPermanentSourcePrevented(ctx.gameData(), preventionSource(ctx))
                        : gameQueryService.isDamageFromStackEntryPrevented(ctx.gameData(), ctx.entry())),
                ctx -> "Prevent the damage from this source",
                PermanentDamageCandidateSource::prevented,
                (ctx, result) -> logSourcePrevented(ctx)));
        all.add(effect("DamageByCreaturePrevented", PREVENTION,
                this::damageByCreaturePrevented,
                ctx -> "Prevent damage dealt by creatures",
                ctx -> {
                    prevention.applyAllByCreaturesPreventionLifeGain(ctx.gameData(), ctx.amount(), ctx.targetControllerId());
                    gameLogService.append(ctx.gameData(), GameLog.textCardText("Damage dealt by ",
                            preventionSource(ctx).getCard(), " is prevented."));
                    return prevented(ctx);
                }));
        all.add(effect("TargetSpellPrevented", PREVENTION,
                ctx -> !ctx.targetProtectedFromPrevention() && ctx.dealtByStackEntry()
                        && gameQueryService.isDamageFromTargetSpellPrevented(ctx.gameData(), ctx.entry())
                        && !(!ctx.combat() && preventionSource(ctx) != null
                        && gameQueryService.isPreventedFromDealingDamage(
                        ctx.gameData(), preventionSource(ctx), false, false)
                        && gameQueryService.isTargetSpellShieldPlainPermanentSourcePrevention(
                        ctx.gameData(), ctx.entry())),
                ctx -> "Prevent the targeted spell's damage",
                ctx -> {
                    damageSupport.getObject().gainLifeForTargetSpellDamage(ctx.gameData(), ctx.entry(), ctx.amount());
                    logSpellPrevented(ctx);
                    return prevented(ctx);
                }));
        all.add(spellPrevention("TargetingSpellPrevented", "Prevent damage from spells that target it",
                ctx -> gameQueryService.isDamageFromTargetingSpellPrevented(ctx.gameData(), ctx.entry(), ctx.target())));
        all.add(preventAll("TargetingSpellOrAbilityPrevented", "Prevent damage from spells and abilities that target it",
                ctx -> !ctx.combat() && gameQueryService.isDamageFromTargetingSpellOrAbilityPrevented(
                        ctx.gameData(), ctx.entry(), ctx.target())));
        all.add(preventAll("Protection", "Prevent the damage (protection)", this::protectionPrevents).whole());
        all.add(effect("Dralnu", ReplacementOption.Kind.REPLACEMENT,
                ctx -> damageSupport.getObject().hasDralnuDamageReplacement(ctx.gameData(), ctx.target()),
                ctx -> "Dralnu: sacrifice that many permanents instead",
                ctx -> {
                    if (ctx.combat()) {
                        combatDamageService.getObject().queueDralnuReplacement(ctx.gameData(), ctx.target(), ctx.amount());
                        return prevented(ctx);
                    }
                    return ctx.event().withAmount(ctx.amount()
                            - damageSupport.getObject().applyDralnuReplacement(ctx.gameData(), ctx.target(), ctx.amount()));
                }).whole());
        all.add(pure("OpponentSourceReduction", PREVENTION,
                ctx -> !ctx.targetProtectedFromPrevention() && opponentSourceReduction(ctx) < ctx.amount(),
                ctx -> "Prevent " + (ctx.amount() - opponentSourceReduction(ctx))
                        + " damage from an opponent's source",
                ctx -> ctx.event().withAmount(opponentSourceReduction(ctx)),
                (ctx, result) -> {
                    gameLogService.append(ctx.gameData(), GameLog.textCardText((ctx.amount() - result.amount()) + " of ",
                            ctx.target().getCard(), "'s damage is prevented."));
                    return null;
                }));
        all.add(effect("TargetSourceShield", PREVENTION,
                ctx -> !ctx.targetProtectedFromPrevention()
                        && gameQueryService.isDamagePreventable(ctx.gameData(), ctx.combat())
                        && prevention.hasTargetSourcePreventionShield(
                        ctx.gameData(), ctx.target().getId(), ctx.damageSourceKey()),
                ctx -> "Prevent damage from " + ctx.sourceName() + " (Healing Grace)",
                ctx -> ctx.event().withAmount(prevention.applyTargetSourcePreventionShield(
                        ctx.gameData(), ctx.target().getId(), ctx.damageSourceKey(), ctx.amount(), ctx.combat()))));
        all.add(effect("ChosenSourceNextDamageShield", PREVENTION,
                ctx -> !ctx.targetProtectedFromPrevention() && prevention.hasChosenSourceNextDamageToAnyTargetShield(
                        ctx.gameData(), ctx.damageSourceKey(), ctx.target().getId(), ctx.combat(),
                        ctx.combat() ? null : ctx.entry()),
                ctx -> "Use the shield against " + ctx.sourceName() + " (Sanctum Guardian)",
                ctx -> {
                    int remaining = ctx.combat()
                            ? prevention.applyChosenSourceNextDamageToAnyTargetShield(ctx.gameData(),
                            ctx.damageSourceKey(), ctx.amount(), ctx.target().getId(), true)
                            : prevention.applyChosenSourceNextDamageToAnyTargetShield(ctx.gameData(),
                            ctx.damageSourceKey(), ctx.amount(), ctx.target().getId(), ctx.entry());
                    damageSupport.getObject().processEyeForAnEyeReflections(ctx.gameData());
                    return ctx.event().withAmount(remaining);
                }));
        all.add(effect("ControllerCreaturesNextSourceShield", PREVENTION,
                ctx -> !ctx.targetProtectedFromPrevention() && prevention.hasControllerCreaturesNextSourceDamageShield(
                        ctx.gameData(), ctx.targetControllerId(), ctx.damageSourceKey(), ctx.combat()),
                ctx -> "Prevent the next damage from " + ctx.sourceName() + " to your creature (Shadowbane)",
                ctx -> ctx.event().withAmount(prevention.applyControllerCreaturesNextSourceDamageShield(
                        ctx.gameData(), ctx.targetControllerId(), ctx.damageSourceKey(), ctx.amount(), ctx.combat()))));
        all.add(effect("ChannelHarm", PREVENTION,
                ctx -> !ctx.targetProtectedFromPrevention() && prevention.hasChannelHarmShield(
                        ctx.gameData(), ctx.targetControllerId(), ctx.sourceControllerId()),
                ctx -> "Channel Harm: prevent the damage",
                ctx -> ctx.event().withAmount(prevention.applyChannelHarmPreventionToPermanent(
                        ctx.gameData(), ctx.target(), ctx.sourceControllerId(), ctx.amount()))));
        all.add(effect("PreventAndSourceControllerDraws", PREVENTION,
                ctx -> !ctx.targetProtectedFromPrevention()
                        && prevention.hasSwansSourceControllerDraw(ctx.gameData(), ctx.target(), ctx.combat()),
                ctx -> "Prevent the damage; the source's controller draws that many cards",
                ctx -> {
                    prevention.applySwansSourceControllerDraw(ctx.gameData(), ctx.target(), ctx.amount(),
                            ctx.sourceControllerId(), ctx.combat());
                    logPrevented(ctx, null);
                    return prevented(ctx);
                }));
        all.add(effect("PreventCombatDamageAndExile", PREVENTION,
                ctx -> ctx.combat()
                        && prevention.hasPreventCombatDamageToSelfAndExile(ctx.gameData(), ctx.target()),
                ctx -> ctx.nameShownToChooser(ctx.target()) + ": prevent the combat damage and exile that many cards",
                ctx -> {
                    if (!prevention.applyPreventCombatDamageToSelfAndExile(ctx.gameData(), ctx.target(), ctx.amount())
                            || ctx.targetProtectedFromPrevention()) {
                        return ctx.event();
                    }
                    logPrevented(ctx, null);
                    return prevented(ctx);
                }).wholeIncludingUnpreventable());
        all.add(preventAll("StaticRecipientPrevention", "Prevent the damage to it",
                ctx -> staticPreventions(ctx).stream().anyMatch(DamagePreventionService.StaticPrevention::preventsAll)));
        all.add(new Rule<PermanentDamageContext>("StaticRecipientPartialPrevention", PREVENTION, true,
                ctx -> ctx.targetProtectedFromPrevention() ? List.of() : staticPreventions(ctx).stream()
                        .filter(found -> !found.preventsAll())
                        .map(DamagePreventionService.StaticPrevention::instance).toList(),
                (ctx, instance) -> "Prevent " + partialAmount(ctx, instance) + " of the damage ("
                        + partialPrevention(ctx, instance).sourceName() + ")",
                (ctx, instance) -> ctx.event().withAmount(Math.max(0, ctx.amount() - partialAmount(ctx, instance))),
                (ctx, instance) -> {
                    int prevented = Math.min(ctx.amount(), partialAmount(ctx, instance));
                    gameLogService.append(ctx.gameData(), GameLog.textCardText(prevented + " damage to ",
                            ctx.target().getCard(), " is prevented."));
                    return new ApplyResult.Applied<>(ctx.event().withAmount(ctx.amount() - prevented));
                }));
        all.add(effect("PreventCombatDamageAndShuffle", PREVENTION,
                ctx -> ctx.combat() && ctx.damageSource() != null
                        && gameQueryService.shufflesTargetIntoOwnersLibraryAfterCombatDamagePrevention(
                        ctx.gameData(), ctx.damageSource(), ctx.target()),
                ctx -> "Prevent the damage and shuffle " + ctx.nameShownToChooser(ctx.target()) + " into its owner's library",
                ctx -> {
                    combatDamageService.getObject().queueShuffleAfterCombatDamage(ctx.gameData(), ctx.target());
                    boolean preventable = !ctx.targetProtectedFromPrevention()
                            && gameQueryService.isDamagePreventable(ctx.gameData(), true)
                            && !gameQueryService.damageCantBePreventedFromSource(
                            ctx.gameData(), ctx.damageSource(), true);
                    return preventable ? prevented(ctx) : ctx.event();
                }));
        all.add(preventAll("ColorPreventedForTarget", "Prevent damage from sources of this color",
                ctx -> prevention.isColorDamagePreventedForTarget(
                        ctx.gameData(), ctx.target().getId(), ctx.damageSourceColors(), ctx.combat())));
        all.add(preventAll("SharedColorPrevented", "Prevent damage between creatures that share a color",
                ctx -> gameQueryService.isDamageBetweenCreaturesOfSharedColorPrevented(
                        ctx.gameData(), ctx.target(), preventionSource(ctx), ctx.combat())));
        all.add(preventAll("NamedPlanePrevented", "Prevent damage from the named plane",
                ctx -> !ctx.combat() && gameQueryService.isDamageFromNamedPlanePreventedForControlledPermanent(
                        ctx.gameData(), ctx.entry(), ctx.targetControllerId())));
        all.add(preventAll("CreatureSourceToSelf", "Prevent damage from creatures",
                ctx -> !ctx.combat() && gameQueryService.isCreatureSourceDamageToSelfPrevented(
                        ctx.gameData(), ctx.target(), ctx.entry(), ctx.event().explicitSource())));
        all.add(preventAll("DesertsToCamelOrBanded", "Prevent damage from Deserts",
                ctx -> !ctx.combat() && gameQueryService.isDamageFromDesertsToCamelOrBandedCreaturePrevented(
                        ctx.gameData(), ctx.target(), ctx.entry(), ctx.event().explicitSource(), false)));
        all.add(preventAll("MatchingSourceToControlledCreature", "Prevent damage from matching sources",
                ctx -> gameQueryService.isDamageFromMatchingSourcePreventedForControlledCreature(
                        ctx.gameData(), ctx.target(), ctx.damageSource())));
        all.add(effect("ComeuppancePlaneswalker", PREVENTION,
                ctx -> !ctx.targetProtectedFromPrevention() && gameQueryService.isPlaneswalker(ctx.gameData(), ctx.target())
                        && prevention.hasComeuppanceShield(
                        ctx.gameData(), ctx.targetControllerId(), ctx.sourceControllerId(), ctx.combat()),
                ctx -> "Comeuppance: prevent the damage",
                ctx -> {
                    int remaining = prevention.applyComeuppancePrevention(ctx.gameData(), ctx.targetControllerId(),
                            ctx.amount(), ctx.sourceCard(), ctx.damageSource(), ctx.sourceControllerId(), ctx.combat());
                    damageSupport.getObject().processPendingRedirectDamage(ctx.gameData());
                    return ctx.event().withAmount(remaining);
                }));
        all.add(preventAll("ArtifactToEnchantedCreature", "Prevent damage from artifacts",
                ctx -> !ctx.combat() && gameQueryService.isArtifactDamageToEnchantedCreaturePrevented(ctx.gameData(), ctx.target(),
                        ctx.damageSource(), ctx.damageSource() == null ? ctx.entry().getEffectiveDamageSourceCard() : null)));
        all.add(preventAll("ArtifactToSelf", "Prevent damage from artifacts",
                ctx -> !ctx.combat() && gameQueryService.isArtifactDamageToSelfPrevented(ctx.gameData(), ctx.target(),
                        ctx.damageSource(), ctx.damageSource() == null ? ctx.entry().getEffectiveDamageSourceCard() : null)));
        for (CreatureDamageStep step : prevention.creatureDamageSteps()) {
            all.add(stepRule(step));
        }
        all.add(pure("AllButOneToPlaneswalker", PREVENTION,
                ctx -> !ctx.targetProtectedFromPrevention() && gameQueryService.isPlaneswalker(ctx.gameData(), ctx.target())
                        && prevention.applyAllButOneDamageToPermanentPrevention(
                        ctx.gameData(), ctx.target(), ctx.amount(), ctx.combat()) > 0,
                ctx -> "Prevent all but 1 of the damage to your planeswalker",
                ctx -> ctx.event().withAmount(ctx.amount() - prevention.applyAllButOneDamageToPermanentPrevention(
                        ctx.gameData(), ctx.target(), ctx.amount(), ctx.combat())),
                PermanentDamageCandidateSource::noLog));
        return List.copyOf(all);
    }

    /**
     * The static preventions of the damage, none while it can't be prevented (CR 615.12): damage can't be prevented
     * this turn, or its source's damage can't be.
     */
    private List<DamagePreventionService.StaticPrevention> staticPreventions(PermanentDamageContext ctx) {
        if (!gameQueryService.isDamagePreventable(ctx.gameData(), ctx.combat())
                || ctx.damageSource() != null && gameQueryService.damageCantBePreventedFromSource(
                ctx.gameData(), ctx.damageSource(), ctx.combat())) {
            return List.of();
        }
        return prevention.staticPreventionsOfPermanentDamage(ctx.gameData(), ctx.target(), ctx.targetControllerId(),
                new DamagePreventionService.StaticDamage(ctx.entry(), ctx.sourceCard(), ctx.damageSource(),
                        ctx.sourceControllerId(), ctx.combat(), ctx.dealtByStackEntry(), ctx.damageSourceColors()));
    }

    private DamagePreventionService.StaticPrevention partialPrevention(PermanentDamageContext ctx, String instance) {
        return staticPreventions(ctx).stream().filter(found -> found.instance().equals(instance)).findFirst()
                .orElse(new DamagePreventionService.StaticPrevention(instance, "", 0));
    }

    private int partialAmount(PermanentDamageContext ctx, String instance) {
        Integer amount = partialPrevention(ctx, instance).amount();
        return amount == null ? 0 : amount;
    }

    /** One {@link CreatureDamageStep} as an option. */
    private Rule<PermanentDamageContext> stepRule(CreatureDamageStep step) {
        Rule<PermanentDamageContext> rule = new Rule<>(step.family(), step.kind(), step.pure(),
                ctx -> step.applies().test(ctx.gameData(), stepContext(ctx), ctx.amount()) ? ONCE : List.of(),
                (ctx, instance) -> step.label(),
                (ctx, instance) -> ctx.event().withAmount(
                        step.apply().apply(ctx.gameData(), stepContext(ctx), ctx.amount())),
                (ctx, instance) -> {
                    // CR 616.1f: the departure this step performs instead of the damage is asked about first.
                    if (damageSupport.getObject().askBeforeDamageDeparture(
                            ctx.gameData(), step, stepContext(ctx), ctx.amount())) {
                        return new ApplyResult.Suspended<>(ctx.event());
                    }
                    return new ApplyResult.Applied<>(ctx.event().withAmount(
                            step.apply().apply(ctx.gameData(), stepContext(ctx), ctx.amount())));
                });
        return PER_SOURCE_STEPS.contains(step.family()) ? rule : rule.whole();
    }

    private static CreatureDamageStep.Context stepContext(PermanentDamageContext ctx) {
        return new CreatureDamageStep.Context(ctx.target(), ctx.combat(), ctx.damageSource());
    }

    /** A spell-only prevention, skipped when "damage can't be prevented" protects the permanent. */
    private Rule<PermanentDamageContext> spellPrevention(String family, String label,
                                                         Predicate<PermanentDamageContext> applies) {
        return preventAll(family, label, ctx -> ctx.dealtByStackEntry() && applies.test(ctx));
    }

    private Rule<PermanentDamageContext> preventAll(String family, String label,
                                                    Predicate<PermanentDamageContext> applies) {
        return pure(family, PREVENTION, ctx -> !ctx.targetProtectedFromPrevention() && applies.test(ctx),
                ctx -> label, PermanentDamageCandidateSource::prevented, this::logPrevented);
    }

    /**
     * "Prevent all damage that would be dealt by creatures" (Chant of Vitu-Ghazi, Ethereal Haze, Thwart the Enemy)
     * applies to this damage. A spell or ability whose creature source has left reads the source's last known
     * information, its controller included (CR 608.2h).
     */
    private boolean damageByCreaturePrevented(PermanentDamageContext ctx) {
        Permanent source = preventionSource(ctx);
        if (source == null) {
            return false;
        }
        if (ctx.damageSource() != null) {
            return gameQueryService.isDamageByCreaturePrevented(ctx.gameData(), source);
        }
        return damageSupport.getObject().isGlobalCreaturePreventionForEntry(ctx.gameData(), ctx.entry());
    }

    /**
     * Protection (CR 702.16e): damage from a source with the stated quality is prevented. A spell or ability's own
     * damage is judged by its card and controller; noncombat damage that the dealing entry's own source permanent
     * deals as itself ("it deals 4 damage to each creature blocking it", read live or as it last existed; a fight or
     * bite carrier's dealer) by that permanent. Any other permanent dealing damage itself (combat, a dealer that isn't
     * the entry's source) only when the damaged permanent also has its own +1/+1-counter prevention to order it
     * against (Phantom Centaur).
     */
    private boolean protectionPrevents(PermanentDamageContext ctx) {
        if (!gameQueryService.isDamagePreventable(ctx.gameData(), ctx.combat())) {
            return false;
        }
        Permanent source = ctx.damageSource();
        boolean abilitySourceDealsIt = source != null && !ctx.combat()
                && source.getId().equals(ctx.entry().getSourcePermanentId());
        if (source != null && (abilitySourceDealsIt
                || prevention.hasPreventRemovePlusOneCountersEffect(ctx.gameData(), ctx.target()))
                && !gameQueryService.damageCantBePreventedFromSource(ctx.gameData(), source, ctx.combat())
                && gameQueryService.hasProtectionFromDamageSource(ctx.gameData(), ctx.target(), source)) {
            return true;
        }
        return !ctx.combat() && ctx.dealtByStackEntry()
                && gameQueryService.hasProtectionFromDamageSource(ctx.gameData(), ctx.target(),
                ctx.entry().getEffectiveDamageSourceCard(), ctx.entry().getControllerId());
    }

    /** The source the "can't deal damage" checks read: the dealing permanent, or its last known state. */
    private static Permanent preventionSource(PermanentDamageContext ctx) {
        if (ctx.damageSource() != null) {
            return ctx.damageSource();
        }
        Permanent snapshot = ctx.entry().getSourcePermanentSnapshot();
        boolean otherObjectDealsDamage = snapshot != null && ctx.entry().getDamageSourceCard() != null
                && ctx.entry().getDamageSourceCard() != snapshot.getCard();
        return otherObjectDealsDamage ? null : snapshot;
    }

    private Void logSourcePrevented(PermanentDamageContext ctx) {
        Permanent source = preventionSource(ctx);
        gameLogService.append(ctx.gameData(), GameLog.cardThen(
                source != null ? source.getCard() : ctx.entry().getEffectiveDamageSourceCard(), "'s damage is prevented."));
        return null;
    }

    private Void logSpellPrevented(PermanentDamageContext ctx) {
        gameLogService.append(ctx.gameData(), GameLog.cardThen(ctx.entry().getEffectiveDamageSourceCard(),
                "'s damage is prevented."));
        return null;
    }
}
