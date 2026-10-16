package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatDamageService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Redirection effects (CR 614.9) for damage to a permanent: a sorcery's damage sent back to its
 * controller, Harm's Way, Reflect Damage, Opal-Eye, Saving Grace, Palisade Giant, Ascent of the
 * Worthy, the "have it dealt to you instead" choice, Treacherous Link, Glarecaster and Oracle's
 * Attendants. Damage that can't be redirected skips all of them. A redirect to a player keeps the
 * same event (CR 616.2); damage redirected to another permanent is dealt to it at once.
 */
@Component
class PermanentDamageRedirects extends PermanentDamageCandidateSource {

    private static final ReplacementOption.Kind REDIRECT = ReplacementOption.Kind.REDIRECT;

    private final DamagePreventionService prevention;
    private final ObjectProvider<DamageSupport> damageSupport;
    private final ObjectProvider<CombatDamageService> combatDamageService;
    private final List<Rule<PermanentDamageContext>> rules;

    PermanentDamageRedirects(GameQueryService gameQueryService, GameLogService gameLogService,
                             DamagePreventionService prevention, ObjectProvider<DamageSupport> damageSupport,
                             ObjectProvider<CombatDamageService> combatDamageService) {
        super(gameQueryService, gameLogService);
        this.prevention = prevention;
        this.damageSupport = damageSupport;
        this.combatDamageService = combatDamageService;
        this.rules = List.of(
                pure("TargetSorceryRedirect", REDIRECT,
                        ctx -> redirectable(ctx) && !ctx.targetProtectedFromPrevention() && ctx.dealtByStackEntry()
                                && prevention.getTargetSorceryDamageRedirectController(ctx.gameData(), ctx.entry()) != null,
                        ctx -> ctx.sourceCard().getName() + "'s damage is dealt to its controller",
                        ctx -> ctx.event().withRecipient(
                                prevention.getTargetSorceryDamageRedirectController(ctx.gameData(), ctx.entry())),
                        (ctx, result) -> {
                            gameLogService.append(ctx.gameData(), GameLog.cardThen(ctx.entry().getEffectiveDamageSourceCard(),
                                    "'s damage to " + ctx.target().getCard().getName()
                                            + " is dealt to its controller instead."));
                            return null;
                        }),
                redirect("SourceNextCombatDamageToController", "The next combat damage from the source is dealt to another player",
                        ctx -> ctx.combat() && prevention.hasSourceNextCombatDamageToControllerShield(
                                ctx.gameData(), ctx.sourcePermanentId()),
                        ctx -> prevention.applySourceNextCombatDamageToControllerShield(
                                ctx.gameData(), ctx.sourcePermanentId(), ctx.amount())),
                redirect("SourceRedirectShield", "Redirect damage from the chosen source (Harm's Way)",
                        ctx -> ctx.targetControllerId() != null
                                && (ctx.sourcePermanentId() != null || ctx.entry() != null)
                                && prevention.hasSourceRedirectShield(
                                ctx.gameData(), ctx.targetControllerId(), chosenSourceId(ctx)),
                        ctx -> prevention.applySourceRedirectShields(ctx.gameData(), ctx.targetControllerId(),
                                chosenSourceId(ctx), ctx.amount())),
                effect("ReflectDamage", REDIRECT,
                        ctx -> redirectable(ctx)
                                && prevention.hasReflectDamageToSourceControllerShield(ctx.gameData(), chosenSourceId(ctx)),
                        ctx -> "Reflect Damage: the source deals the damage to its controller",
                        ctx -> {
                            int remaining = prevention.applyReflectDamageToSourceControllerShield(
                                    ctx.gameData(), chosenSourceId(ctx), ctx.amount());
                            damageSupport.getObject().processEyeForAnEyeReflections(ctx.gameData());
                            return ctx.event().withAmount(remaining);
                        }),
                redirect("SourceNextRedirectToPermanent", "Redirect the next damage from the source to a creature (Opal-Eye)",
                        ctx -> prevention.hasSourceNextDamageRedirectToPermanent(
                                ctx.gameData(), chosenSourceId(ctx), ctx.target().getId()),
                        ctx -> prevention.applySourceNextDamageRedirectToPermanent(
                                ctx.gameData(), chosenSourceId(ctx), ctx.target().getId(), ctx.amount())),
                new Rule<PermanentDamageContext>("TurnRedirectToCreature", REDIRECT, false,
                        ctx -> redirectable(ctx) && !turnRedirectAlreadyMoved(ctx)
                                ? prevention.applicableTurnDamageRedirectToCreatureShields(ctx.gameData(),
                                        ctx.targetControllerId(), ctx.target().getId(), null, ctx.combat())
                                .stream().map(String::valueOf).toList()
                                : List.of(),
                        (ctx, instance) -> "Redirect the damage to "
                                + turnRedirectDestinationName(ctx, instance),
                        (ctx, instance) -> ctx.event(),
                        (ctx, instance) -> new ApplyResult.Applied<>(redirected(ctx,
                                prevention.applyTurnDamageRedirectToCreatureShield(ctx.gameData(),
                                        Integer.parseInt(instance), ctx.targetControllerId(), ctx.amount())))),
                new Rule<PermanentDamageContext>("PermanentAbsorbsDamage", REDIRECT, false,
                        ctx -> redirectable(ctx)
                                ? prevention.staticPermanentDamageRedirectSources(ctx.gameData(),
                                ctx.targetControllerId(), ctx.target().getId()).stream()
                                .map(permanent -> permanent.getId().toString()).toList()
                                : List.of(),
                        (ctx, instance) -> absorber(ctx, instance).getCard().getName() + ": deal the damage to it instead",
                        (ctx, instance) -> ctx.event(),
                        (ctx, instance) -> new ApplyResult.Applied<>(redirected(ctx,
                                prevention.applyStaticPermanentDamageRedirectToSelf(ctx.gameData(),
                                        ctx.targetControllerId(), ctx.amount(), absorber(ctx, instance))))),
                redirect("ControllerCreatureRedirectUntilNextTurn", "Redirect the damage to the chosen creature",
                        ctx -> prevention.hasCreatureControllerDamageRedirectUntilNextTurn(
                                ctx.gameData(), ctx.targetControllerId(), ctx.target()),
                        ctx -> prevention.applyCreatureControllerDamageRedirectUntilNextTurn(ctx.gameData(),
                                ctx.targetControllerId(), ctx.target(), ctx.sourcePermanentId(), ctx.amount())),
                new Rule<PermanentDamageContext>("AllCreatureDamageRedirectChoice", REDIRECT, false,
                        ctx -> redirectable(ctx) && !allCreatureRedirectControllers(ctx).isEmpty() ? ONCE : List.of(),
                        (ctx, instance) -> "Let a player have the damage dealt to them instead",
                        (ctx, instance) -> ctx.event(),
                        (ctx, instance) -> {
                            if (ctx.combat()) {
                                combatDamageService.getObject().queueCombatCreatureRedirectChoice(ctx.gameData(),
                                        ctx.damageSource(), ctx.target(), ctx.amount(), allCreatureRedirectControllers(ctx));
                                return new ApplyResult.Suspended<>(ctx.event());
                            }
                            damageSupport.getObject().queueCreatureDamageRedirectChoice(ctx.gameData(), ctx.entry(),
                                    ctx.target().getId(), ctx.amount(), allCreatureRedirectControllers(ctx));
                            return new ApplyResult.Applied<>(prevented(ctx));
                        }).whole(),
                redirect("EnchantedCreatureRedirectToController", "Its controller is dealt the damage instead",
                        ctx -> prevention.hasEnchantedCreatureDamageRedirectToController(ctx.gameData(), ctx.target()),
                        ctx -> prevention.applyEnchantedCreatureDamageRedirectToController(ctx.gameData(), ctx.target(),
                                ctx.sourcePermanentId(), ctx.amount())),
                redirect("SourceAndControllerNextRedirect", "Redirect the next damage dealt to it (Glarecaster)",
                        ctx -> prevention.hasSourcePermanentAndControllerNextDamageRedirectToPermanent(
                                ctx.gameData(), ctx.target().getId()),
                        ctx -> prevention.applySourcePermanentAndControllerNextDamageRedirectToPermanent(
                                ctx.gameData(), ctx.target().getId(), ctx.sourcePermanentId(), ctx.amount())),
                redirect("CreatureRedirectShield", "Redirect the damage dealt to it",
                        ctx -> prevention.hasCreatureRedirectShield(
                                ctx.gameData(), ctx.target().getId(), ctx.damageSourceKey(), ctx.combat()),
                        ctx -> ctx.combat()
                                ? prevention.applyCreatureRedirectShields(ctx.gameData(), ctx.target().getId(),
                                ctx.damageSourceKey(), ctx.amount(), true)
                                : prevention.applyCreatureRedirectShields(
                                ctx.gameData(), ctx.target().getId(), ctx.damageSourceKey(), ctx.amount())));
    }

    @Override
    protected List<Rule<PermanentDamageContext>> rules() {
        return rules;
    }

    private Rule<PermanentDamageContext> redirect(String family, String label, Predicate<PermanentDamageContext> applies,
                                                  Function<PermanentDamageContext, Integer> remaining) {
        return effect(family, REDIRECT, ctx -> redirectable(ctx) && applies.test(ctx), ctx -> label,
                ctx -> redirected(ctx, remaining.apply(ctx)));
    }

    private static boolean redirectable(PermanentDamageContext ctx) {
        return !ctx.event().cantBeRedirected();
    }

    /** The identity Reflect Damage and Opal-Eye recorded the chosen source under. */
    private static UUID chosenSourceId(PermanentDamageContext ctx) {
        return ctx.sourcePermanentId() != null ? ctx.sourcePermanentId() : ctx.entry().getCard().getId();
    }

    private List<UUID> allCreatureRedirectControllers(PermanentDamageContext ctx) {
        if (ctx.gameData().resolvingDeclinedAllCreatureDamageRedirect
                || ctx.gameData().playersRedirectingAllCreatureDamage.isEmpty()) {
            return List.of();
        }
        return ctx.gameData().orderedPlayerIds.stream()
                .filter(ctx.gameData().playersRedirectingAllCreatureDamage::contains).toList();
    }

    /**
     * Damage one of these shields already moved stays where it went: with several Saving Graces, all
     * damage dealt at once goes to the one enchanted creature its player chooses (Saving Grace
     * ruling, 2017-07-14), so another shield doesn't move it again.
     */
    private static boolean turnRedirectAlreadyMoved(PermanentDamageContext ctx) {
        return ctx.event().appliedKeys().stream().anyMatch(key -> key.startsWith("TurnRedirectToCreature"));
    }

    private String turnRedirectDestinationName(PermanentDamageContext ctx, String shieldIndex) {
        UUID destinationId = ctx.gameData().turnDamageRedirectToCreatureShields
                .get(Integer.parseInt(shieldIndex)).redirectTargetCreatureId();
        Permanent destination = gameQueryService.findPermanentById(ctx.gameData(), destinationId);
        return destination == null ? "a creature" : destination.getCard().getName();
    }

    private Permanent absorber(PermanentDamageContext ctx, String instance) {
        return gameQueryService.findPermanentById(ctx.gameData(), UUID.fromString(instance));
    }

    private DamageEvent redirected(PermanentDamageContext ctx, int remaining) {
        damageSupport.getObject().processRedirects(ctx.gameData(), ctx.event());
        return ctx.event().withAmount(remaining);
    }
}
