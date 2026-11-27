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

/**
 * Replacement effects that change how much damage a player is dealt: global multipliers such as
 * Furnace of Rath, the source's, its controller's and the recipient's multipliers (Fiery Emancipation, Gisela,
 * Curse of Bloodletting), the "that much damage plus N" bonuses (Torbran), each one option per effect, Benevolent Unicorn's reduction, and the halving, capping and
 * Ojer Axonil replacements. They only rewrite the amount, so they are pure; when they meet each
 * other or a prevention shield, the order is the affected player's (CR 616.1).
 */
@Component
class PlayerDamageModifiers extends PlayerDamageCandidateSource {

    private static final ReplacementOption.Kind REPLACEMENT = ReplacementOption.Kind.REPLACEMENT;

    private final List<Rule<PlayerDamageContext>> rules;

    PlayerDamageModifiers(GameQueryService gameQueryService, GameLogService gameLogService) {
        super(gameQueryService, gameLogService);
        List<Rule<PlayerDamageContext>> all = new ArrayList<>();
        all.add(globalMultiplierRule(gameQueryService));
        all.add(multiplierRule("SourceMultiplier", this::sourceMultipliers));
        all.add(bonusRule("SourceBonus", ctx -> ctx.combat()
                ? gameQueryService.getCombatSourceDamageBonuses(ctx.gameData(), combatSource(ctx))
                : gameQueryService.getDealingSourceDamageBonuses(ctx.gameData(), ctx.entry())));
        all.add(DamageCandidateSource.<PlayerDamageContext>multiplierRule("EnchantedPlayerMultiplier",
                ctx -> gameQueryService.getEnchantedPlayerDamageMultipliers(ctx.gameData(), ctx.playerId())).whole());
        all.add(multiplierRule("RecipientMultiplier", ctx -> gameQueryService.getDamageToRecipientMultipliers(
                ctx.gameData(), ctx.playerId(), ctx.sourceControllerId(), null, ctx.combat())));
        all.add(bonusRule("AdditionalDamageBonus", ctx -> gameQueryService.getAdditionalDamageBonuses(ctx.gameData(),
                gameQueryService.additionalDamageEvent(ctx.entry(), ctx.sourcePermanent(), ctx.source(),
                        ctx.sourceControllerId(), true, ctx.combat(), ctx.playerId(), null))));
        all.add(bonusRule("ControllerNoncombatBonus", ctx -> ctx.combat() ? List.of()
                : gameQueryService.getControllerNoncombatDamageBonuses(ctx.gameData(), ctx.sourceControllerId())));
        all.add(bonusRule("PerpetualNoncombatBonus", ctx -> ctx.combat() ? List.of()
                : gameQueryService.getPerpetualNoncombatDamageBonuses(ctx.gameData(), ctx.entry())));
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

    /** The controller's and the source's doublings of the damage, one per effect. */
    private List<GameQueryService.DamageMultiplier> sourceMultipliers(PlayerDamageContext ctx) {
        return ctx.combat()
                ? gameQueryService.getCombatSourceDamageMultipliers(ctx.gameData(), combatSource(ctx), null,
                        ctx.playerId())
                : gameQueryService.getDealingSourceDamageMultipliers(ctx.gameData(), ctx.entry());
    }

    /** The creature dealing combat damage, as it last existed on the battlefield. */
    private static Permanent combatSource(PlayerDamageContext ctx) {
        return ctx.sourcePermanent() != null ? ctx.sourcePermanent() : ctx.entry().getSourcePermanentSnapshot();
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
            List<CardEffect> statics = permanent.getCard().getStaticEffects();
            for (int i = 0; i < statics.size(); i++) {
                if (statics.get(i) instanceof ReduceSpellDamageEffect reduce && reduce.amount() > 0) {
                    reductions.put(permanent.getId() + "#" + i, new NumericInstance(
                            FaceDownVisibility.publicName(gameData, permanent) + ": subtract " + reduce.amount() + " from the damage",
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
}
