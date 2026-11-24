package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.TurnDamageRedirectToCreatureShield;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Redirection effects (CR 614.9) for damage to a player: Harsh Judgment, the source-keyed redirections
 * (Reflect Damage, Opal-Eye, Mirror Strike, Soltari Guerrillas, Goblin Psychopath, Reverberation), Aegis of
 * Honor, Harm's Way, General's Regalia, Saving Grace, Glarecaster, Martyrdom, Vengeful Archon and the creatures
 * and Auras that absorb their controller's damage. A redirect to another player keeps the same event, so that player's effects
 * now apply to it (CR 616.2) while effects already applied stay spent (CR 614.5); damage redirected
 * to a permanent is dealt to it at once.
 */
@Component
class PlayerDamageRedirects extends PlayerDamageCandidateSource {

    private static final ReplacementOption.Kind REDIRECT = ReplacementOption.Kind.REDIRECT;

    private final DamagePreventionService prevention;
    private final PermanentRemovalService permanentRemovalService;
    private final ObjectProvider<DamageSupport> damageSupport;
    private final List<Rule<PlayerDamageContext>> rules;

    PlayerDamageRedirects(GameQueryService gameQueryService, GameLogService gameLogService,
                          DamagePreventionService prevention, PermanentRemovalService permanentRemovalService,
                          ObjectProvider<DamageSupport> damageSupport) {
        super(gameQueryService, gameLogService);
        this.prevention = prevention;
        this.permanentRemovalService = permanentRemovalService;
        this.damageSupport = damageSupport;
        this.rules = List.of(
                pure("HarshJudgmentRedirect", REDIRECT,
                        ctx -> harshJudgmentTarget(ctx) != null,
                        ctx -> "Harsh Judgment: " + ctx.sourceName() + "'s damage is dealt to its controller",
                        ctx -> ctx.event().withRecipient(harshJudgmentTarget(ctx)),
                        this::logRedirectedToController),
                new Rule<PlayerDamageContext>("SourceKeyedRedirect", REDIRECT, false,
                        ctx -> prevention.applicableSourceKeyedRedirects(ctx.gameData(), ctx.damageSourceId(),
                                ctx.sourceControllerId(), ctx.playerId(), null, ctx.combat()),
                        (ctx, instance) -> ctx.sourceName() + "'s damage is dealt to "
                                + destinationName(ctx, prevention.sourceKeyedRedirectDestination(ctx.gameData(),
                                        instance, ctx.sourceControllerId(), ctx.combat())) + " instead",
                        (ctx, instance) -> ctx.event(),
                        (ctx, instance) -> new ApplyResult.Applied<>(redirected(ctx,
                                prevention.applySourceKeyedRedirect(ctx.gameData(), instance, ctx.damageSourceId(),
                                        ctx.sourceControllerId(), ctx.playerId(), null, ctx.amount(),
                                        ctx.combat())))),
                effect("NextInstantOrSorceryRedirect", REDIRECT,
                        ctx -> prevention.hasNextInstantOrSorceryDamageRedirectShield(
                                ctx.gameData(), ctx.entry(), ctx.playerId()),
                        ctx -> "Aegis of Honor: the damage is dealt to the spell's controller",
                        ctx -> ctx.event().withRecipient(prevention.applyNextInstantOrSorceryDamageRedirectShield(
                                ctx.gameData(), ctx.entry(), ctx.playerId(), ctx.amount()))),
                effect("SourceRedirectShield", REDIRECT,
                        ctx -> prevention.hasSourceRedirectShield(ctx.gameData(), ctx.playerId(), ctx.damageSourceId()),
                        ctx -> "Redirect damage from " + ctx.sourceName() + " (Harm's Way)",
                        ctx -> redirected(ctx, prevention.applySourceRedirectShields(
                                ctx.gameData(), ctx.playerId(), ctx.damageSourceId(), ctx.amount()))),
                effect("PlayerSourceNextRedirect", REDIRECT,
                        ctx -> prevention.hasPlayerSourceNextDamageRedirectShield(
                                ctx.gameData(), ctx.playerId(), ctx.damageSourceId()),
                        ctx -> "Redirect the next damage from " + ctx.sourceName() + " to a creature",
                        ctx -> redirected(ctx, prevention.applyPlayerSourceNextDamageRedirectShield(
                                ctx.gameData(), ctx.playerId(), ctx.damageSourceId(), ctx.amount()))),
                new Rule<PlayerDamageContext>("TurnRedirectToCreature", REDIRECT, false,
                        ctx -> prevention.applicableTurnDamageRedirectToCreatureShields(ctx.gameData(),
                                        ctx.playerId(), null, ctx.damageSourceId(), ctx.combat())
                                .stream().map(String::valueOf).toList(),
                        (ctx, instance) -> "Redirect the damage to "
                                + turnRedirectDestinationName(ctx, instance),
                        (ctx, instance) -> ctx.event(),
                        (ctx, instance) -> new ApplyResult.Applied<>(redirected(ctx,
                                prevention.applyTurnDamageRedirectToCreatureShield(ctx.gameData(),
                                        Integer.parseInt(instance), ctx.playerId(), ctx.amount())))),
                effect("SourceAndControllerNextRedirect", REDIRECT,
                        ctx -> prevention.hasSourcePermanentAndControllerNextDamageRedirectToPlayer(
                                ctx.gameData(), ctx.playerId()),
                        ctx -> "Redirect the next damage dealt to you (Glarecaster)",
                        ctx -> redirected(ctx, prevention.applySourcePermanentAndControllerNextDamageRedirectToPlayer(
                                ctx.gameData(), ctx.playerId(), ctx.damageSourceId(), ctx.amount()))),
                effect("PlayerNextRedirect", REDIRECT,
                        ctx -> prevention.hasPlayerNextDamageRedirectShield(ctx.gameData(), ctx.playerId()),
                        ctx -> "Redirect the next damage dealt to you (Martyrdom)",
                        ctx -> redirected(ctx, prevention.applyPlayerNextDamageRedirectShields(
                                ctx.gameData(), ctx.playerId(), ctx.entry().getSourcePermanentId(), ctx.amount()))),
                effect("PlayerRedirectShield", REDIRECT,
                        ctx -> gameQueryService.isDamagePreventable(ctx.gameData(), ctx.combat())
                                && prevention.hasPlayerRedirectShield(ctx.gameData(), ctx.playerId()),
                        ctx -> "Redirect the damage (Vengeful Archon)",
                        ctx -> {
                            int remaining = prevention.applyPlayerRedirectShields(
                                    ctx.gameData(), ctx.playerId(), ctx.amount(), ctx.combat());
                            damageSupport.getObject().processPendingRedirectDamage(ctx.gameData());
                            return ctx.event().withAmount(remaining);
                        }).whole(),
                new Rule<PlayerDamageContext>("PlayerDamageAbsorber", REDIRECT, false,
                        ctx -> absorbers(ctx).stream().map(permanent -> permanent.getId().toString()).toList(),
                        (ctx, instance) -> absorberName(ctx, instance) + ": deal the damage to it instead",
                        (ctx, instance) -> ctx.event(),
                        (ctx, instance) -> {
                            Permanent absorber = absorbers(ctx).stream()
                                    .filter(permanent -> permanent.getId().toString().equals(instance))
                                    .findFirst().orElse(null);
                            if (absorber == null) {
                                return new ApplyResult.Applied<>(ctx.event());
                            }
                            return new ApplyResult.Applied<>(redirected(ctx,
                                    prevention.applyStaticPermanentDamageRedirectToSelf(
                                            ctx.gameData(), ctx.playerId(), ctx.amount(), absorber)));
                        }));
    }

    /** The permanents that would be dealt this damage instead of the player (Palisade Giant, Royal Guard). */
    private List<Permanent> absorbers(PlayerDamageContext ctx) {
        return prevention.playerDamageAbsorbers(ctx.gameData(), ctx.playerId(), ctx.entry(), ctx.source(),
                ctx.entry().getSourcePermanentId(), ctx.combat());
    }

    /**
     * The option label for an absorber. Replacement choices are answered by label, so when several
     * absorbers share a name they are numbered in battlefield order to keep every label distinct.
     */
    private String absorberName(PlayerDamageContext ctx, String instance) {
        Permanent absorber = gameQueryService.findPermanentById(ctx.gameData(), UUID.fromString(instance));
        if (absorber == null) {
            return "A permanent";
        }
        String name = ctx.nameShownToChooser(absorber);
        List<Permanent> sameName = absorbers(ctx).stream()
                .filter(candidate -> ctx.nameShownToChooser(candidate).equals(name))
                .toList();
        if (sameName.size() < 2) {
            return name;
        }
        int position = 1;
        for (Permanent candidate : sameName) {
            if (candidate.getId().equals(absorber.getId())) {
                break;
            }
            position++;
        }
        return name + " (" + position + ")";
    }

    @Override
    protected List<Rule<PlayerDamageContext>> rules() {
        return rules;
    }

    private UUID harshJudgmentTarget(PlayerDamageContext ctx) {
        return damageSupport.getObject().findHarshJudgmentRedirectTarget(ctx.gameData(), ctx.entry(), ctx.playerId());
    }

    private String turnRedirectDestinationName(PlayerDamageContext ctx, String shieldIndex) {
        UUID destinationId = ctx.gameData().shields(TurnDamageRedirectToCreatureShield.class)
                .get(Integer.parseInt(shieldIndex)).redirectTargetCreatureId();
        Permanent destination = gameQueryService.findPermanentById(ctx.gameData(), destinationId);
        return destination == null ? "a creature" : ctx.nameShownToChooser(destination);
    }

    private String destinationName(PlayerDamageContext ctx, UUID destinationId) {
        if (destinationId == null) {
            return "another player";
        }
        if (ctx.gameData().playerIds.contains(destinationId)) {
            return ctx.gameData().playerIdToName.get(destinationId);
        }
        Permanent destination = gameQueryService.findPermanentById(ctx.gameData(), destinationId);
        return destination == null ? "a creature" : ctx.nameShownToChooser(destination);
    }

    private DamageEvent redirected(PlayerDamageContext ctx, int remaining) {
        damageSupport.getObject().processRedirects(ctx.gameData(), ctx.event());
        return ctx.event().withAmount(remaining);
    }

    private Void logRedirectedToController(PlayerDamageContext ctx, DamageEvent ignored) {
        gameLogService.append(ctx.gameData(), GameLog.cardThen(ctx.source(),
                "'s damage to " + ctx.playerName() + " is dealt to its controller instead."));
        return null;
    }
}
