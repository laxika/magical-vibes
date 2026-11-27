package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceSpellDamageEffect;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Replacement effects that change how much damage a permanent is dealt: Benevolent Unicorn's
 * reduction, the "that much damage plus N" bonuses, the source's, its controller's and the recipient's
 * multipliers (Gisela's doubling for an opponent's permanent), each one option per effect, and the halving
 * and capping replacements. They only rewrite the amount, so they are pure.
 */
@Component
class PermanentDamageModifiers extends PermanentDamageCandidateSource {

    private static final ReplacementOption.Kind REPLACEMENT = ReplacementOption.Kind.REPLACEMENT;

    private final List<Rule<PermanentDamageContext>> rules;

    PermanentDamageModifiers(GameQueryService gameQueryService, GameLogService gameLogService) {
        super(gameQueryService, gameLogService);
        this.rules = List.of(
                globalMultiplierRule(gameQueryService),
                multiplierRule("SourceMultiplier", this::sourceMultipliers),
                bonusRule("SourceBonus", ctx -> ctx.combat()
                        ? gameQueryService.getCombatSourceDamageBonuses(ctx.gameData(), ctx.damageSource())
                        : gameQueryService.getDealingSourceDamageBonuses(ctx.gameData(), ctx.entry(), ctx.damageSource())),
                spellDamageReductionRule(),
                bonusRule("AdditionalDamageBonus", ctx -> gameQueryService.getAdditionalDamageBonuses(ctx.gameData(),
                        gameQueryService.additionalDamageEvent(ctx.entry(), ctx.damageSource(), ctx.sourceCard(),
                                ctx.sourceControllerId(), ctx.dealtByStackEntry(), ctx.combat(),
                                ctx.targetControllerId(), ctx.target()))),
                bonusRule("ControllerNoncombatBonus", ctx -> ctx.combat() ? List.of()
                        : gameQueryService.getControllerNoncombatDamageBonuses(ctx.gameData(), ctx.sourceControllerId())),
                bonusRule("PerpetualNoncombatBonus", ctx -> ctx.combat() ? List.of()
                        : gameQueryService.getPerpetualNoncombatDamageBonuses(ctx.gameData(), ctx.entry())),
                multiplierRule("RecipientMultiplier", this::recipientMultipliers),
                pure("DamageReplacement", REPLACEMENT,
                        ctx -> replacedDamage(ctx) != ctx.amount(),
                        ctx -> "Change the damage to " + replacedDamage(ctx),
                        ctx -> ctx.event().withAmount(replacedDamage(ctx)),
                        PermanentDamageCandidateSource::noLog));
    }

    @Override
    protected List<Rule<PermanentDamageContext>> rules() {
        return rules;
    }

    /** Gisela: doubling keyed on who controls the damage source, as the spell or the fighting permanent. */
    private List<GameQueryService.DamageMultiplier> recipientMultipliers(PermanentDamageContext ctx) {
        if (ctx.combat()) {
            return gameQueryService.getDamageToRecipientMultipliers(ctx.gameData(), ctx.targetControllerId(),
                    ctx.sourceControllerId(), ctx.target().getId(), true);
        }
        UUID sourceControllerId = ctx.dealtByStackEntry() ? ctx.entry().getControllerId()
                : gameQueryService.findPermanentController(ctx.gameData(), ctx.event().explicitSource().getId());
        if (sourceControllerId == null) {
            sourceControllerId = ctx.entry().getControllerId();
        }
        return gameQueryService.getDamageToRecipientMultipliers(ctx.gameData(), ctx.targetControllerId(),
                sourceControllerId, ctx.target().getId(), false);
    }

    /**
     * The controller's and the source's doublings of the damage; a fighting or biting permanent's
     * own doublings count too when it isn't the stack entry's source. Combat damage to a creature
     * also gets that creature's own doublings.
     */
    private List<GameQueryService.DamageMultiplier> sourceMultipliers(PermanentDamageContext ctx) {
        if (ctx.combat()) {
            List<GameQueryService.DamageMultiplier> multipliers = new ArrayList<>(
                    gameQueryService.getCombatSourceDamageMultipliers(ctx.gameData(), ctx.damageSource(),
                            ctx.target(), null));
            if (gameQueryService.isCreature(ctx.gameData(), ctx.target())) {
                multipliers.addAll(gameQueryService.getCombatDamageToCreatureMultipliers(ctx.gameData(), ctx.target()));
            }
            return multipliers;
        }
        Permanent explicitSource = ctx.event().explicitSource();
        if (explicitSource != null && !explicitSource.getId().equals(ctx.entry().getSourcePermanentId())) {
            return gameQueryService.getExplicitSourceDamageMultipliers(ctx.gameData(), ctx.entry(), explicitSource);
        }
        return gameQueryService.getDealingSourceDamageMultipliers(ctx.gameData(), ctx.entry());
    }

    private int replacedDamage(PermanentDamageContext ctx) {
        return gameQueryService.applyDamageReplacementEffects(ctx.gameData(), ctx.amount());
    }

    /** Benevolent Unicorn, one option each; only spells dealing damage as themselves are reduced. */
    private Rule<PermanentDamageContext> spellDamageReductionRule() {
        return new Rule<>("SpellDamageReduction", REPLACEMENT, true,
                ctx -> !ctx.dealtByStackEntry()
                        || gameQueryService.getSpellDamageReduction(ctx.gameData(), ctx.entry()) <= 0 ? List.of()
                        : List.copyOf(spellDamageReductions(ctx.gameData()).keySet()),
                (ctx, instance) -> spellDamageReductions(ctx.gameData()).get(instance).label(),
                (ctx, instance) -> ctx.event().withAmount(
                        ctx.amount() - spellDamageReductions(ctx.gameData()).get(instance).value()),
                (ctx, instance) -> new ApplyResult.Applied<>(ctx.event().withAmount(
                        ctx.amount() - spellDamageReductions(ctx.gameData()).get(instance).value())));
    }

    private Map<String, NumericInstance> spellDamageReductions(GameData gameData) {
        Map<String, NumericInstance> reductions = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> collectReductions(gameData, permanent, reductions));
        return reductions;
    }

    private void collectReductions(GameData gameData, Permanent permanent, Map<String, NumericInstance> reductions) {
        if (gameQueryService.hasLostAllAbilities(gameData, permanent)) return;
        List<CardEffect> statics = permanent.getCard().getStaticEffects();
        for (int i = 0; i < statics.size(); i++) {
            if (statics.get(i) instanceof ReduceSpellDamageEffect reduce && reduce.amount() > 0) {
                reductions.put(permanent.getId() + "#" + i, new NumericInstance(
                        FaceDownVisibility.publicName(gameData, permanent) + ": subtract " + reduce.amount() + " from the damage",
                        reduce.amount()));
            }
        }
    }
}
