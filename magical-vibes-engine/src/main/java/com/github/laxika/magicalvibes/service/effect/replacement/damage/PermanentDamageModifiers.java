package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceSpellDamageEffect;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Replacement effects that change how much damage a permanent is dealt: Benevolent Unicorn's
 * reduction, the "that much damage plus N" bonuses, Gisela's doubling for an opponent's permanent
 * and the halving and capping replacements. They only rewrite the amount, so they are pure.
 */
@Component
class PermanentDamageModifiers extends PermanentDamageCandidateSource {

    private static final ReplacementOption.Kind REPLACEMENT = ReplacementOption.Kind.REPLACEMENT;

    private final List<Rule<PermanentDamageContext>> rules;

    PermanentDamageModifiers(GameQueryService gameQueryService, GameLogService gameLogService) {
        super(gameQueryService, gameLogService);
        this.rules = List.of(
                globalMultiplierRule(gameQueryService),
                pure("SourceMultiplier", REPLACEMENT,
                        ctx -> sourceMultiplier(ctx) > 1,
                        ctx -> "Multiply damage from the source by " + sourceMultiplier(ctx),
                        ctx -> ctx.event().withAmount(ctx.amount() * sourceMultiplier(ctx)),
                        PermanentDamageCandidateSource::noLog),
                bonusRule("SourceBonus", ctx -> ctx.combat()
                        ? gameQueryService.getCombatSourceDamageBonuses(ctx.gameData(), ctx.damageSource())
                        : gameQueryService.getDealingSourceDamageBonuses(ctx.gameData(), ctx.entry())),
                spellDamageReductionRule(),
                bonusRule("NoncreatureSourceBonus", ctx -> ctx.combat() || isCreatureExplicitSource(ctx) ? List.of()
                        : gameQueryService.getNoncreatureSourceDamageBonuses(ctx.gameData(), ctx.entry(), null, ctx.target())),
                bonusRule("PlayersAndBattlesBonus", ctx ->
                        gameQueryService.getPlayersAndBattlesDamageBonuses(ctx.gameData(), false, ctx.target())),
                bonusRule("ToOpponentsBonus", ctx -> gameQueryService.getAdditionalDamageToOpponentsBonuses(
                        ctx.gameData(), ctx.sourceControllerId(), ctx.sourceCard(), ctx.damageSource(),
                        ctx.targetControllerId())),
                bonusRule("ControllerToOpponentBonus", ctx -> gameQueryService.getControllerDamageToOpponentBonuses(
                        ctx.gameData(), ctx.sourceControllerId(), ctx.targetControllerId(), ctx.combat(), true,
                        ctx.damageSource() == null ? ctx.entry().getSourcePermanentId() : ctx.damageSource().getId())),
                bonusRule("SpellToOpponentsBonus", ctx -> ctx.combat() ? List.of()
                        : gameQueryService.getAdditionalSpellDamageToOpponentsBonuses(
                        ctx.gameData(), ctx.entry(), ctx.targetControllerId())),
                bonusRule("ControllerNoncombatBonus", ctx -> ctx.combat() ? List.of()
                        : gameQueryService.getControllerNoncombatDamageBonuses(ctx.gameData(), ctx.sourceControllerId())),
                bonusRule("PerpetualNoncombatBonus", ctx -> ctx.combat() ? List.of()
                        : gameQueryService.getPerpetualNoncombatDamageBonuses(ctx.gameData(), ctx.entry())),
                pure("RecipientMultiplier", REPLACEMENT,
                        ctx -> recipientMultiplier(ctx) > 1,
                        ctx -> "Multiply damage to recipient by " + recipientMultiplier(ctx),
                        ctx -> ctx.event().withAmount(ctx.amount() * recipientMultiplier(ctx)),
                        PermanentDamageCandidateSource::noLog),
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
    private int recipientMultiplier(PermanentDamageContext ctx) {
        if (ctx.combat()) {
            return gameQueryService.getDamageToRecipientMultiplier(ctx.gameData(), ctx.targetControllerId(),
                    ctx.sourceControllerId(), ctx.target().getId(), true);
        }
        UUID sourceControllerId = ctx.dealtByStackEntry() ? ctx.entry().getControllerId()
                : gameQueryService.findPermanentController(ctx.gameData(), ctx.event().explicitSource().getId());
        if (sourceControllerId == null) {
            sourceControllerId = ctx.entry().getControllerId();
        }
        return gameQueryService.getDamageToRecipientMultiplier(ctx.gameData(), ctx.targetControllerId(),
                sourceControllerId, ctx.target().getId());
    }

    /**
     * The controller's and the source's doublings of the damage; a fighting or biting permanent's
     * own doublings count too when it isn't the stack entry's source. Combat damage to a creature
     * also gets that creature's own doublings.
     */
    /**
     * A permanent that deals damage on its own behalf (a Dragon making Sarkhan's trigger deal damage) is a
     * creature source, so noncreature-source damage bonuses from the stack entry's spell or ability don't apply.
     */
    private boolean isCreatureExplicitSource(PermanentDamageContext ctx) {
        Permanent explicitSource = ctx.event().explicitSource();
        return explicitSource != null && gameQueryService.isCreature(ctx.gameData(), explicitSource);
    }

    private int sourceMultiplier(PermanentDamageContext ctx) {
        if (ctx.combat()) {
            int multiplier = gameQueryService.getCombatSourceDamageMultiplier(ctx.gameData(), ctx.damageSource(),
                    ctx.target(), null);
            return gameQueryService.isCreature(ctx.gameData(), ctx.target())
                    ? multiplier * gameQueryService.getCombatDamageToCreatureMultiplier(ctx.gameData(), ctx.target())
                    : multiplier;
        }
        Permanent explicitSource = ctx.event().explicitSource();
        if (explicitSource != null && !explicitSource.getId().equals(ctx.entry().getSourcePermanentId())) {
            return gameQueryService.getExplicitSourceDamageMultiplier(ctx.gameData(), ctx.entry(), explicitSource);
        }
        return gameQueryService.getDealingSourceDamageMultiplier(ctx.gameData(), ctx.entry());
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
        List<CardEffect> statics = permanent.getCard().getEffects(EffectSlot.STATIC);
        for (int i = 0; i < statics.size(); i++) {
            if (statics.get(i) instanceof ReduceSpellDamageEffect reduce && reduce.amount() > 0) {
                reductions.put(permanent.getId() + "#" + i, new NumericInstance(
                        permanent.getCard().getName() + ": subtract " + reduce.amount() + " from the damage",
                        reduce.amount()));
            }
        }
    }
}
