package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceSpellDamageEffect;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Replacement effects that change how much damage a player is dealt: global multipliers such as
 * Furnace of Rath, Curse of Bloodletting and other recipient multipliers, the "that much damage
 * plus N" bonuses (Torbran-style), Benevolent Unicorn's reduction, and the halving, capping and
 * Ojer Axonil replacements. They only rewrite the amount, so they are pure; when they meet each
 * other or a prevention shield, the order is the affected player's (CR 616.1).
 */
@Component
class PlayerDamageModifiers extends PlayerDamageCandidateSource {

    private static final ReplacementOption.Kind REPLACEMENT = ReplacementOption.Kind.REPLACEMENT;
    private static final String GLOBAL_MULTIPLIER = "GlobalDamageMultiplier";

    private final List<Rule<PlayerDamageContext>> rules;

    PlayerDamageModifiers(GameQueryService gameQueryService, GameLogService gameLogService) {
        super(gameQueryService, gameLogService);
        List<Rule<PlayerDamageContext>> all = new ArrayList<>();
        all.add(globalMultiplierRule());
        all.add(multiplier("EnchantedPlayerMultiplier", "damage to enchanted player",
                ctx -> gameQueryService.getEnchantedPlayerDamageMultiplier(ctx.gameData(), ctx.playerId())).whole());
        all.add(multiplier("RecipientMultiplier", "damage to recipient",
                ctx -> ctx.combat()
                        ? gameQueryService.getDamageToRecipientMultiplier(
                        ctx.gameData(), ctx.playerId(), ctx.sourceControllerId(), true)
                        : gameQueryService.getDamageToRecipientMultiplier(
                        ctx.gameData(), ctx.playerId(), ctx.sourceControllerId())));
        all.add(bonus("NoncreatureSourceBonus", ctx -> gameQueryService.getNoncreatureSourceDamageBonus(
                ctx.gameData(), ctx.entry(), ctx.playerId(), null)));
        all.add(bonus("PlayersAndBattlesBonus", ctx -> ctx.combat() ? 0 : gameQueryService.getPlayersAndBattlesDamageBonus(
                ctx.gameData(), true, null)));
        all.add(bonus("ControllerToOpponentBonus", ctx -> gameQueryService.getControllerDamageToOpponentBonus(
                ctx.gameData(), ctx.sourceControllerId(), ctx.playerId(), ctx.combat(),
                ctx.sourcePermanent() == null ? ctx.entry().getSourcePermanentId() : ctx.sourcePermanent().getId())));
        all.add(bonus("SpellToOpponentsBonus", ctx -> gameQueryService.getAdditionalSpellDamageToOpponentsBonus(
                ctx.gameData(), ctx.entry(), ctx.playerId())));
        all.add(bonus("ControllerNoncombatBonus", ctx -> ctx.combat() ? 0 : gameQueryService.getControllerNoncombatDamageBonus(
                ctx.gameData(), ctx.sourceControllerId())));
        all.add(bonus("PerpetualNoncombatBonus", ctx -> ctx.combat() ? 0 : gameQueryService.getPerpetualNoncombatDamageBonus(
                ctx.gameData(), ctx.entry())));
        all.add(bonus("ColorSourceToPlayerBonus", ctx -> gameQueryService.getDamageToPlayerColorSourceBonus(
                ctx.gameData(), ctx.damageSourceColors())));
        all.add(bonus("ToOpponentsBonus", ctx -> gameQueryService.getAdditionalDamageToOpponentsBonus(
                ctx.gameData(), ctx.sourceControllerId(), ctx.source(), ctx.sourcePermanent(), ctx.playerId())));
        all.add(spellDamageReductionRule());
        all.add(pure("DamageReplacement", REPLACEMENT,
                ctx -> replacedDamage(ctx) != ctx.amount(),
                ctx -> "Change the damage to " + replacedDamage(ctx),
                ctx -> ctx.event().withAmount(replacedDamage(ctx)),
                PlayerDamageCandidateSource::noLog));
        all.add(pure("OjerAxonil", REPLACEMENT,
                ctx -> ojerDamage(ctx) != ctx.amount(),
                ctx -> "Ojer Axonil: deal " + ojerDamage(ctx) + " damage instead",
                ctx -> ctx.event().withAmount(ojerDamage(ctx)),
                PlayerDamageCandidateSource::noLog));
        this.rules = List.copyOf(all);
    }

    @Override
    protected List<Rule<PlayerDamageContext>> rules() {
        return rules;
    }

    private Rule<PlayerDamageContext> multiplier(String family, String description, ToIntFunction<PlayerDamageContext> factor) {
        return pure(family, REPLACEMENT,
                ctx -> factor.applyAsInt(ctx) > 1,
                ctx -> "Multiply " + description + " by " + factor.applyAsInt(ctx),
                ctx -> ctx.event().withAmount(ctx.amount() * factor.applyAsInt(ctx)),
                PlayerDamageCandidateSource::noLog);
    }

    private Rule<PlayerDamageContext> bonus(String family, ToIntFunction<PlayerDamageContext> bonus) {
        return pure(family, REPLACEMENT,
                ctx -> bonus.applyAsInt(ctx) != 0,
                ctx -> "Deal " + bonus.applyAsInt(ctx) + " more damage",
                ctx -> ctx.event().withAmount(ctx.amount() + bonus.applyAsInt(ctx)),
                PlayerDamageCandidateSource::noLog);
    }

    /**
     * Benevolent Unicorn: each permanent with {@link ReduceSpellDamageEffect} is its own option, so a
     * multiplier can be applied between two reductions. Only spells dealing damage as themselves are
     * reduced.
     */
    private Rule<PlayerDamageContext> spellDamageReductionRule() {
        return new Rule<PlayerDamageContext>("SpellDamageReduction", REPLACEMENT, true,
                ctx -> gameQueryService.getSpellDamageReduction(ctx.gameData(), ctx.entry()) <= 0 ? List.of()
                        : List.copyOf(spellDamageReductions(ctx.gameData()).keySet()),
                (ctx, instance) -> spellDamageReductions(ctx.gameData()).get(instance).label(),
                (ctx, instance) -> ctx.event().withAmount(
                        ctx.amount() - spellDamageReductions(ctx.gameData()).get(instance).value()),
                (ctx, instance) -> new ApplyResult.Applied<>(ctx.event().withAmount(
                        ctx.amount() - spellDamageReductions(ctx.gameData()).get(instance).value())));
    }

    private Map<String, NumericInstance> spellDamageReductions(GameData gameData) {
        Map<String, NumericInstance> reductions = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (gameQueryService.hasLostAllAbilities(gameData, permanent)) return;
            List<CardEffect> statics = permanent.getCard().getEffects(EffectSlot.STATIC);
            for (int i = 0; i < statics.size(); i++) {
                if (statics.get(i) instanceof ReduceSpellDamageEffect reduce && reduce.amount() > 0) {
                    reductions.put(permanent.getId() + "#" + i, new NumericInstance(
                            permanent.getCard().getName() + ": subtract " + reduce.amount() + " from the damage",
                            reduce.amount()));
                }
            }
        });
        return reductions;
    }

    private int replacedDamage(PlayerDamageContext ctx) {
        return ctx.combat()
                ? gameQueryService.applyDamageReplacementEffects(ctx.gameData(), ctx.amount())
                : gameQueryService.applyDamageReplacementEffects(ctx.gameData(), ctx.entry(), ctx.playerId(), ctx.amount());
    }

    /** Ojer Axonil replaces only noncombat damage. */
    private int ojerDamage(PlayerDamageContext ctx) {
        return ctx.combat() ? ctx.amount() : gameQueryService.applyOjerAxonilDamageReplacement(ctx.gameData(),
                ctx.amount(), ctx.damageSourceColors(), ctx.sourceControllerId(), ctx.playerId());
    }

    /**
     * Furnace of Rath-style multipliers, one option each. The effect dealing the damage already
     * multiplied it, so these only compete when that multiplication was taken back out of the
     * event ({@link DamageEvent#globalMultiplierPending()}).
     */
    private Rule<PlayerDamageContext> globalMultiplierRule() {
        return new Rule<PlayerDamageContext>(GLOBAL_MULTIPLIER, REPLACEMENT, true,
                ctx -> ctx.event().globalMultiplierPending()
                        ? List.copyOf(globalMultipliers(ctx.gameData()).keySet()) : List.of(),
                (ctx, instance) -> globalMultipliers(ctx.gameData()).get(instance).label(),
                (ctx, instance) -> ctx.event().withAmount(
                        ctx.amount() * globalMultipliers(ctx.gameData()).get(instance).value()),
                (ctx, instance) -> new ApplyResult.Applied<>(ctx.event().withAmount(
                        ctx.amount() * globalMultipliers(ctx.gameData()).get(instance).value())));
    }

    /** One effect instance: its option label and the number it multiplies or subtracts by. */
    private record NumericInstance(String label, int value) {}

    private Map<String, NumericInstance> globalMultipliers(GameData gameData) {
        Map<String, NumericInstance> multipliers = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, permanent)) return;
            collectGlobalMultipliers(gameData, permanent.getId().toString(), permanent.getCard(), multipliers);
        });
        if (gameData.planechase != null) {
            for (var plane : gameData.planechase.faceUp) {
                collectGlobalMultipliers(gameData, plane.getCard().getId().toString(), plane.getCard(), multipliers);
            }
        }
        return multipliers;
    }

    private static void collectGlobalMultipliers(GameData gameData, String sourceKey, Card card,
                                                 Map<String, NumericInstance> multipliers) {
        List<CardEffect> statics = card.getEffects(EffectSlot.STATIC);
        for (int i = 0; i < statics.size(); i++) {
            if (statics.get(i) instanceof GlobalDamageMultiplyingEffect global) {
                int factor = MaroGoneNutsSupport.apply(gameData, global, global.damageMultiplierFactor());
                if (factor > 1) {
                    multipliers.put(sourceKey + "#" + i,
                            new NumericInstance(card.getName() + ": multiply damage by " + factor, factor));
                }
            }
        }
    }
}
