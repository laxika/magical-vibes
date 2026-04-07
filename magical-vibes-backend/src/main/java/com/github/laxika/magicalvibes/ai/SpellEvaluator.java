package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongTargetCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesXLifeAndControllerGainsLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandWithManaValueConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Estimates the board evaluation delta from casting a specific card.
 * Uses instanceof pattern matching on the card's effects. No state mutation.
 */
public class SpellEvaluator {

    private final GameQueryService gameQueryService;
    private final BoardEvaluator boardEvaluator;

    public SpellEvaluator(GameQueryService gameQueryService, BoardEvaluator boardEvaluator) {
        this.gameQueryService = gameQueryService;
        this.boardEvaluator = boardEvaluator;
    }

    /**
     * Evaluates the value of activating an ability by scoring its non-cost effects.
     * Uses the same evaluation logic as spell effects, plus ability-specific effects
     * like BoostSelfEffect and RegenerateEffect.
     */
    public double evaluateAbilityEffects(GameData gameData, List<CardEffect> effects, UUID aiPlayerId) {
        UUID opponentId = getOpponentId(gameData, aiPlayerId);
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        List<Permanent> aiBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());

        double value = 0;
        for (CardEffect effect : effects) {
            if (effect instanceof CostEffect) continue;
            value += evaluateAbilityEffect(gameData, effect, aiPlayerId, opponentId,
                    aiBattlefield, oppBattlefield);
        }
        return value;
    }

    private double evaluateAbilityEffect(GameData gameData, CardEffect effect,
                                         UUID aiPlayerId, UUID opponentId,
                                         List<Permanent> aiBattlefield, List<Permanent> oppBattlefield) {
        // Self-pump (e.g. Shivan Dragon's {R}: +1/+0)
        if (effect instanceof BoostSelfEffect boost) {
            return boost.powerBoost() * 2.0 + boost.toughnessBoost();
        }
        // Regenerate (shield from destruction)
        if (effect instanceof RegenerateEffect) {
            return 4.0;
        }
        // Scry
        if (effect instanceof ScryEffect scry) {
            return scry.count() * 2.0;
        }
        // +1/+1 counters on all own creatures
        if (effect instanceof PutPlusOnePlusOneCounterOnEachOwnCreatureEffect counters) {
            long creatureCount = aiBattlefield.stream()
                    .filter(p -> gameQueryService.isCreature(gameData, p))
                    .count();
            return creatureCount * counters.count() * 3.5;
        }
        // +1/+1 counter on target creature
        if (effect instanceof PutPlusOnePlusOneCounterOnTargetCreatureEffect counters) {
            return counters.count() * 3.5;
        }
        // Tap target permanent
        if (effect instanceof TapTargetPermanentEffect) {
            double bestTapValue = oppBattlefield.stream()
                    .filter(p -> gameQueryService.isCreature(gameData, p) && !p.isTapped())
                    .mapToDouble(p -> boardEvaluator.creatureScore(gameData, p, opponentId, aiPlayerId) * 0.3)
                    .max()
                    .orElse(0);
            return bestTapValue;
        }
        // Fall through to the standard effect evaluation
        return evaluateSingleEffect(gameData, null, effect, aiPlayerId, opponentId,
                aiBattlefield, oppBattlefield);
    }

    /**
     * Estimates the value of casting a card. Returns a score where higher is better.
     * Returns 0 or negative for cards that shouldn't be cast.
     *
     * Applies game-phase multipliers: early game boosts cheap creatures and tempo,
     * late game boosts card draw, board wipes, and big finishers.
     */
    public double estimateSpellValue(GameData gameData, Card card, UUID aiPlayerId) {
        UUID opponentId = getOpponentId(gameData, aiPlayerId);
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        List<Permanent> aiBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());

        double baseValue;

        if (card.hasType(CardType.CREATURE)) {
            baseValue = evaluateCreature(gameData, card, aiPlayerId, opponentId);
        } else {
            // Evaluate by scanning effects across all slots
            baseValue = 0;

            baseValue += evaluateEffects(gameData, card, card.getEffects(EffectSlot.SPELL),
                    aiPlayerId, opponentId, aiBattlefield, oppBattlefield);
            baseValue += evaluateEffects(gameData, card, card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD),
                    aiPlayerId, opponentId, aiBattlefield, oppBattlefield);

            // Aura evaluation
            if (card.isAura()) {
                baseValue += evaluateAura(gameData, card, aiPlayerId, opponentId, aiBattlefield, oppBattlefield);
            }
        }

        baseValue += synergyBonus(gameData, card, aiPlayerId, aiBattlefield, oppBattlefield);

        if (baseValue <= 0) return baseValue;

        return baseValue * gamePhaseMultiplier(gameData, card, aiPlayerId)
                         * defensivePressureMultiplier(gameData, card, aiPlayerId);
    }

    /**
     * Returns a multiplier (0.7–1.3) that adjusts a spell's value based on the
     * current game phase. Early game rewards cheap creatures and tempo plays;
     * late game rewards card advantage, board wipes, and expensive finishers.
     */
    double gamePhaseMultiplier(GameData gameData, Card card, UUID aiPlayerId) {
        GamePhase phase = GamePhase.determine(gameData, aiPlayerId);
        if (phase == GamePhase.MID) return 1.0;

        if (phase == GamePhase.EARLY) {
            // Cheap creatures (mana value 1-3) are premium for board development
            if (card.hasType(CardType.CREATURE) && card.getManaValue() <= 3) {
                return 1.3;
            }
            // Card draw is weaker — you need board presence, not more cards
            if (hasCardDrawEffect(card)) {
                return 0.7;
            }
            // Board wipes are rarely good when boards are small
            if (hasBoardWipeEffect(card)) {
                return 0.7;
            }
            return 1.0;
        }

        // LATE game
        // Small creatures are outclassed — a 2/2 doesn't matter on a stalled board
        if (card.hasType(CardType.CREATURE) && card.getManaValue() <= 2) {
            return 0.7;
        }
        // Big finishers are at their best when you have mana to cast them
        if (card.hasType(CardType.CREATURE) && card.getManaValue() >= 5) {
            return 1.2;
        }
        // Card draw is excellent — find answers and threats
        if (hasCardDrawEffect(card)) {
            return 1.3;
        }
        // Board wipes are strongest when boards are developed
        if (hasBoardWipeEffect(card)) {
            return 1.2;
        }
        return 1.0;
    }

    private boolean hasCardDrawEffect(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (isDrawEffect(effect)) return true;
        }
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (isDrawEffect(effect)) return true;
        }
        return false;
    }

    private boolean isDrawEffect(CardEffect effect) {
        if (effect instanceof DrawCardEffect) return true;
        if (effect instanceof ChooseOneEffect coe) {
            return coe.options().stream().anyMatch(o -> isDrawEffect(o.effect()));
        }
        return false;
    }

    private boolean hasBoardWipeEffect(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (isBoardWipeEffect(effect)) return true;
        }
        return false;
    }

    private boolean isBoardWipeEffect(CardEffect effect) {
        if (effect instanceof MassDamageEffect || effect instanceof DestroyAllPermanentsEffect
                || effect instanceof ReturnCreaturesToOwnersHandEffect) {
            return true;
        }
        if (effect instanceof ChooseOneEffect coe) {
            return coe.options().stream().anyMatch(o -> isBoardWipeEffect(o.effect()));
        }
        return false;
    }

    /**
     * Returns a multiplier (1.0–3.0) that boosts defensive spells when the AI
     * is under heavy board pressure. When opponent's board damage >= AI life
     * (one attack from dead), life gain is boosted up to 3x, board wipes 1.5x,
     * removal and creatures 1.3x. Scales linearly from 50% pressure to 100%.
     */
    double defensivePressureMultiplier(GameData gameData, Card card, UUID aiPlayerId) {
        UUID opponentId = getOpponentId(gameData, aiPlayerId);
        int aiLife = gameData.getLife(aiPlayerId);
        if (aiLife <= 0) return 1.0;

        int opponentBoardDamage = computeOpponentBoardDamage(gameData, opponentId);
        if (opponentBoardDamage == 0) return 1.0;

        double pressureRatio = (double) opponentBoardDamage / aiLife;
        if (pressureRatio < 0.5) return 1.0;

        // Determine the maximum multiplier based on spell category
        double maxMultiplier;
        if (hasLifeGainEffect(card)) {
            maxMultiplier = 3.0;
        } else if (hasBoardWipeEffect(card)) {
            maxMultiplier = 1.5;
        } else if (hasRemovalEffects(card) || card.hasType(CardType.CREATURE)) {
            maxMultiplier = 1.3;
        } else {
            return 1.0;
        }

        // Linear interpolation from 1.0 at 50% pressure to maxMultiplier at 100%+ pressure
        double t = Math.min(1.0, (pressureRatio - 0.5) / 0.5);
        return 1.0 + (maxMultiplier - 1.0) * t;
    }

    private int computeOpponentBoardDamage(GameData gameData, UUID opponentId) {
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        int damage = 0;
        for (Permanent perm : oppBattlefield) {
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;
            int power = gameQueryService.getEffectivePower(gameData, perm);
            if (power > 0) damage += power;
        }
        return damage;
    }

    private boolean hasLifeGainEffect(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (isLifeGainEffect(effect)) return true;
        }
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (isLifeGainEffect(effect)) return true;
        }
        return false;
    }

    private boolean isLifeGainEffect(CardEffect effect) {
        if (effect instanceof GainLifeEffect) return true;
        if (effect instanceof ChooseOneEffect coe) {
            return coe.options().stream().anyMatch(o -> isLifeGainEffect(o.effect()));
        }
        return false;
    }

    private double evaluateCreature(GameData gameData, Card card, UUID aiPlayerId, UUID opponentId) {
        double value = boardEvaluator.creatureCardScore(gameData, card, aiPlayerId);

        // Add ETB effect value
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            value += evaluateEtbEffect(gameData, card, effect, aiPlayerId, opponentId);
        }

        return value;
    }

    private double evaluateEtbEffect(GameData gameData, Card card, CardEffect effect,
                                     UUID aiPlayerId, UUID opponentId) {
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        // Modal ETB: evaluate each option and return the best value
        if (effect instanceof ChooseOneEffect coe) {
            double bestValue = 0;
            for (ChooseOneEffect.ChooseOneOption option : coe.options()) {
                double optionValue = evaluateEtbEffect(gameData, card, option.effect(), aiPlayerId, opponentId);
                bestValue = Math.max(bestValue, optionValue);
            }
            return bestValue;
        }

        if (effect instanceof DestroyTargetPermanentEffect) {
            return bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId);
        }
        if (effect instanceof ExileTargetPermanentEffect) {
            return bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId) * 1.1;
        }
        if (effect instanceof GainControlOfTargetPermanentEffect) {
            return bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId) * 1.8;
        }
        if (effect instanceof DrawCardEffect draw) {
            return draw.amount() * 6.0;
        }
        if (effect instanceof CreateTokenEffect token) {
            if (token.primaryType() == CardType.CREATURE) {
                double tokenScore = token.power() * 3.0 + token.toughness() * 1.5;
                return tokenScore * token.amount();
            } else {
                return 3.0 * token.amount();
            }
        }
        if (effect instanceof DealDamageToAnyTargetEffect dmg) {
            return evaluateDamageEffect(gameData, dmg.damage(), oppBattlefield, opponentId, aiPlayerId);
        }
        if (effect instanceof DealDamageToTargetCreatureEffect dmg) {
            return evaluateDamageToCreature(gameData, dmg.damage(), oppBattlefield, opponentId, aiPlayerId);
        }
        if (effect instanceof ReturnTargetPermanentToHandEffect) {
            return bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId) * 0.6;
        }
        if (effect instanceof ReturnTargetPermanentToHandWithManaValueConditionalEffect) {
            return bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId) * 0.6;
        }
        if (effect instanceof GainLifeEffect gain) {
            return gain.amount() * 0.5 * lifeGainMultiplier(gameData, aiPlayerId, opponentId);
        }
        if (effect instanceof TargetPlayerDiscardsEffect discard) {
            int opponentHandSize = gameData.playerHands.getOrDefault(opponentId, List.of()).size();
            int effectiveDiscards = Math.min(discard.amount(), opponentHandSize);
            return effectiveDiscards * 4.0;
        }
        return 0;
    }

    private double evaluateEffects(GameData gameData, Card card, List<CardEffect> effects,
                                   UUID aiPlayerId, UUID opponentId,
                                   List<Permanent> aiBattlefield, List<Permanent> oppBattlefield) {
        double value = 0;
        for (CardEffect effect : effects) {
            value += evaluateSingleEffect(gameData, card, effect, aiPlayerId, opponentId,
                    aiBattlefield, oppBattlefield);
        }
        return value;
    }

    private double evaluateSingleEffect(GameData gameData, Card card, CardEffect effect,
                                        UUID aiPlayerId, UUID opponentId,
                                        List<Permanent> aiBattlefield, List<Permanent> oppBattlefield) {
        // Modal spells: evaluate each option and return the best value
        if (effect instanceof ChooseOneEffect coe) {
            double bestValue = 0;
            for (ChooseOneEffect.ChooseOneOption option : coe.options()) {
                double optionValue = evaluateSingleEffect(gameData, card, option.effect(),
                        aiPlayerId, opponentId, aiBattlefield, oppBattlefield);
                bestValue = Math.max(bestValue, optionValue);
            }
            return bestValue;
        }

        // Removal
        if (effect instanceof DestroyTargetPermanentEffect) {
            return bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId);
        }
        if (effect instanceof ExileTargetPermanentEffect) {
            return bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId) * 1.1;
        }

        // Steal (opponent loses creature + we gain it)
        if (effect instanceof GainControlOfTargetPermanentEffect) {
            return bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId) * 1.8;
        }
        // Temporary steal (attack with opponent's creature this turn)
        if (effect instanceof GainControlOfTargetPermanentUntilEndOfTurnEffect) {
            return bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId) * 1.2;
        }

        // Damage
        if (effect instanceof DealDamageToAnyTargetEffect dmg) {
            return evaluateDamageEffect(gameData, dmg.damage(), oppBattlefield, opponentId, aiPlayerId);
        }
        if (effect instanceof DealDamageToTargetCreatureEffect dmg) {
            return evaluateDamageToCreature(gameData, dmg.damage(), oppBattlefield, opponentId, aiPlayerId);
        }
        if (effect instanceof DealDamageToTargetPlayerEffect dmg) {
            return dmg.damage() * 1.5;
        }
        if (effect instanceof DealDamageToControllerEffect dmg) {
            return -dmg.damage() * 1.5;
        }

        // Board wipes
        if (effect instanceof MassDamageEffect aoe) {
            return evaluateBoardWipeDamage(gameData, aoe.damage(), aiPlayerId, opponentId,
                    aiBattlefield, oppBattlefield);
        }
        if (effect instanceof DestroyAllPermanentsEffect wipe) {
            return evaluateDestroyAllValue(gameData, wipe, aiPlayerId, opponentId,
                    aiBattlefield, oppBattlefield);
        }

        // Draw
        if (effect instanceof DrawCardEffect draw) {
            return draw.amount() * 6.0;
        }

        // Bounce
        if (effect instanceof ReturnTargetPermanentToHandEffect) {
            return bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId) * 0.6;
        }
        if (effect instanceof ReturnTargetPermanentToHandWithManaValueConditionalEffect) {
            return bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId) * 0.6;
        }
        if (effect instanceof ReturnCreaturesToOwnersHandEffect) {
            double oppValue = oppBattlefield.stream()
                    .filter(p -> gameQueryService.isCreature(gameData, p))
                    .mapToDouble(p -> boardEvaluator.creatureScore(gameData, p, opponentId, aiPlayerId))
                    .sum();
            double aiValue = aiBattlefield.stream()
                    .filter(p -> gameQueryService.isCreature(gameData, p))
                    .mapToDouble(p -> boardEvaluator.creatureScore(gameData, p, aiPlayerId, opponentId))
                    .sum();
            return (oppValue - aiValue) * 0.6;
        }

        // Tokens
        if (effect instanceof CreateTokenEffect token) {
            if (token.primaryType() == CardType.CREATURE) {
                double tokenScore = token.power() * 3.0 + token.toughness() * 1.5;
                return tokenScore * token.amount();
            } else {
                return 3.0 * token.amount();
            }
        }

        // Life — scaled by danger level: more valuable when AI is under pressure
        if (effect instanceof GainLifeEffect gain) {
            return gain.amount() * 0.5 * lifeGainMultiplier(gameData, aiPlayerId, opponentId);
        }
        if (effect instanceof LoseLifeEffect lose) {
            return -lose.amount() * 0.5 * lifeGainMultiplier(gameData, aiPlayerId, opponentId);
        }

        // Discard
        if (effect instanceof TargetPlayerDiscardsEffect discard) {
            int opponentHandSize = gameData.playerHands.getOrDefault(opponentId, List.of()).size();
            int effectiveDiscards = Math.min(discard.amount(), opponentHandSize);
            return effectiveDiscards * 4.0;
        }

        // Counter
        if (effect instanceof CounterSpellEffect) {
            // Counter spells are reactive - value depends on what's on the stack
            if (!gameData.stack.isEmpty()) {
                return gameData.stack.getFirst().getCard().getManaValue() * 5.0;
            }
            return 0;
        }

        // P/T boost to target creature
        if (effect instanceof BoostTargetCreatureEffect boost) {
            return (boost.powerBoost() * 2.0 + boost.toughnessBoost());
        }

        // Divided damage among creatures
        if (effect instanceof DealDividedDamageAmongTargetCreaturesEffect divided) {
            return evaluateDamageToCreature(gameData, divided.totalDamage(), oppBattlefield, opponentId, aiPlayerId);
        }

        // X-damage effects
        if (effect instanceof DealXDamageToAnyTargetEffect) {
            int estimatedX = estimateMaxX(gameData, card, aiPlayerId);
            if (estimatedX <= 0) return 0;
            return evaluateDamageEffect(gameData, estimatedX, oppBattlefield, opponentId, aiPlayerId);
        }
        if (effect instanceof DealXDamageToAnyTargetAndGainXLifeEffect) {
            int estimatedX = estimateMaxX(gameData, card, aiPlayerId);
            if (estimatedX <= 0) return 0;
            return evaluateDamageEffect(gameData, estimatedX, oppBattlefield, opponentId, aiPlayerId)
                    + estimatedX * 0.5 * lifeGainMultiplier(gameData, aiPlayerId, opponentId);
        }
        if (effect instanceof DealXDamageToTargetCreatureEffect) {
            int estimatedX = estimateMaxX(gameData, card, aiPlayerId);
            if (estimatedX <= 0) return 0;
            return evaluateDamageToCreature(gameData, estimatedX, oppBattlefield, opponentId, aiPlayerId);
        }
        if (effect instanceof EachOpponentLosesXLifeAndControllerGainsLifeLostEffect) {
            int estimatedX = estimateMaxX(gameData, card, aiPlayerId);
            if (estimatedX <= 0) return 0;
            return estimatedX * 1.5 + estimatedX * 0.5 * lifeGainMultiplier(gameData, aiPlayerId, opponentId); // drain value
        }

        return 0;
    }

    // ===== Synergy detection =====

    /**
     * Adds bonus value when a spell synergizes with the current board or hand state.
     * Checks 5 common patterns: sacrifice + tokens, equipment + evasion,
     * death triggers + sacrifice outlets, anthem + wide board, and token makers + death triggers.
     */
    double synergyBonus(GameData gameData, Card card, UUID aiPlayerId,
                        List<Permanent> aiBattlefield, List<Permanent> oppBattlefield) {
        double bonus = 0;
        bonus += sacrificeWithTokensBonus(gameData, card, aiBattlefield);
        bonus += equipmentWithEvasionBonus(gameData, card, aiBattlefield);
        bonus += deathTriggerWithSacOutletBonus(gameData, card, aiBattlefield);
        bonus += anthemWithWideBoardBonus(gameData, card, aiBattlefield);
        bonus += tokenMakerWithDeathTriggersBonus(gameData, card, aiBattlefield);
        return bonus;
    }

    /**
     * If the spell requires sacrificing a creature and the AI controls expendable tokens,
     * the sacrifice cost is effectively cheaper. Returns a bonus that partially offsets
     * the sacrifice penalty.
     */
    private double sacrificeWithTokensBonus(GameData gameData, Card card, List<Permanent> aiBattlefield) {
        boolean hasSacCost = false;

        // Check spell-level sacrifice costs
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof SacrificeCreatureCost) {
                hasSacCost = true;
                break;
            }
        }

        // Check activated abilities for sacrifice costs
        if (!hasSacCost) {
            for (ActivatedAbility ability : card.getActivatedAbilities()) {
                for (CardEffect effect : ability.getEffects()) {
                    if (effect instanceof SacrificeCreatureCost || effect instanceof SacrificeSelfCost) {
                        hasSacCost = true;
                        break;
                    }
                }
                if (hasSacCost) break;
            }
        }

        if (!hasSacCost) return 0;

        // Count expendable token creatures
        long tokenCount = aiBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p) && p.getCard().isToken())
                .count();

        if (tokenCount == 0) return 0;

        // Each token makes sacrifice costs cheaper — a 1/1 token has ~4.5 creature score,
        // so having tokens reduces the effective cost. Cap at 8.0 bonus.
        return Math.min(tokenCount * 4.0, 8.0);
    }

    /**
     * Equipment that grants P/T or keywords is more valuable when the AI controls evasive
     * creatures (flying, menace, cant-be-blocked, etc.) that can deliver the extra damage.
     */
    private double equipmentWithEvasionBonus(GameData gameData, Card card, List<Permanent> aiBattlefield) {
        if (!card.getSubtypes().contains(CardSubtype.EQUIPMENT)) return 0;

        // Check if this equipment grants a meaningful P/T boost
        double equipBoost = 0;
        for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
            if (effect instanceof StaticBoostEffect boost
                    && boost.scope() == GrantScope.EQUIPPED_CREATURE) {
                equipBoost += boost.powerBoost() * 3.0 + boost.toughnessBoost() * 1.5;
            }
        }
        if (equipBoost <= 0) return 0;

        // Check for evasive creatures on the AI's board
        boolean hasEvasive = aiBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .anyMatch(p -> gameQueryService.hasKeyword(gameData, p, Keyword.FLYING)
                        || gameQueryService.hasKeyword(gameData, p, Keyword.MENACE)
                        || gameQueryService.hasKeyword(gameData, p, Keyword.FEAR)
                        || gameQueryService.hasKeyword(gameData, p, Keyword.INTIMIDATE)
                        || gameQueryService.hasCantBeBlocked(gameData, p));

        // Equipment on an evasive creature is ~50% more effective
        return hasEvasive ? equipBoost * 0.5 : 0;
    }

    /**
     * A creature with "whenever a creature dies" triggers is more valuable when the AI
     * already controls sacrifice outlets (activated abilities with SacrificeCreatureCost).
     */
    private double deathTriggerWithSacOutletBonus(GameData gameData, Card card, List<Permanent> aiBattlefield) {
        boolean hasDeathTrigger = !card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).isEmpty()
                || !card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).isEmpty()
                || !card.getEffects(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES).isEmpty();

        if (!hasDeathTrigger) return 0;

        // Check if the AI controls a sacrifice outlet
        boolean hasSacOutlet = aiBattlefield.stream().anyMatch(p -> hasSacrificeAbility(p.getCard()));

        // A death-trigger creature with a sacrifice outlet gets a significant bonus
        // because deaths can be triggered at will
        return hasSacOutlet ? 6.0 : 0;
    }

    /**
     * Anthem/lord effects are more valuable the wider the board is.
     * If the AI already controls 3+ creatures, casting a lord/anthem is a bigger swing.
     */
    private double anthemWithWideBoardBonus(GameData gameData, Card card, List<Permanent> aiBattlefield) {
        // Check if this card has an anthem/lord static boost
        double perCreatureBoost = 0;
        for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
            if (effect instanceof StaticBoostEffect boost) {
                GrantScope scope = boost.scope();
                if (scope == GrantScope.OWN_CREATURES || scope == GrantScope.ALL_OWN_CREATURES
                        || scope == GrantScope.ALL_CREATURES) {
                    perCreatureBoost += boost.powerBoost() * 3.0 + boost.toughnessBoost() * 1.5;
                    if (boost.grantedKeywords() != null) {
                        perCreatureBoost += boost.grantedKeywords().size() * 3.0;
                    }
                }
            } else if (effect instanceof GrantKeywordEffect grant) {
                GrantScope scope = grant.scope();
                if (scope == GrantScope.OWN_CREATURES || scope == GrantScope.ALL_OWN_CREATURES
                        || scope == GrantScope.ALL_CREATURES) {
                    perCreatureBoost += grant.keywords().size() * 3.0;
                }
            }
        }
        if (perCreatureBoost <= 0) return 0;

        long creatureCount = aiBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .count();

        // The lordBonus in BoardEvaluator already counts buffed creatures, but that's
        // for threat scoring. Here we add extra synergy when the board is wide (3+).
        // The wider the board, the more the anthem overperforms its base value.
        if (creatureCount >= 5) return perCreatureBoost * 1.5;
        if (creatureCount >= 3) return perCreatureBoost * 0.75;
        return 0;
    }

    /**
     * Token-making spells are more valuable when the AI controls permanents with
     * "whenever a creature dies" or "whenever a creature enters" triggers.
     */
    private double tokenMakerWithDeathTriggersBonus(GameData gameData, Card card, List<Permanent> aiBattlefield) {
        // Check if this card creates creature tokens
        int tokenCount = 0;
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof CreateTokenEffect token && token.primaryType() == CardType.CREATURE) {
                tokenCount += token.amount();
            }
        }
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof CreateTokenEffect token && token.primaryType() == CardType.CREATURE) {
                tokenCount += token.amount();
            }
        }
        if (tokenCount == 0) return 0;

        double bonus = 0;

        // Check for "creature dies" triggers on the battlefield
        for (Permanent perm : aiBattlefield) {
            if (!perm.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_DIES).isEmpty()
                    || !perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).isEmpty()
                    || !perm.getCard().getEffects(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES).isEmpty()) {
                bonus += 3.0;
            }
            // Sacrifice outlets make tokens doubly useful — both as sac fodder and trigger fuel
            if (hasSacrificeAbility(perm.getCard())) {
                bonus += 3.0;
            }
        }

        // Scale by number of tokens created
        return Math.min(bonus * tokenCount, 15.0);
    }

    /**
     * Checks whether a card has an activated ability with a sacrifice-creature cost,
     * indicating it's a sacrifice outlet.
     */
    private boolean hasSacrificeAbility(Card card) {
        for (ActivatedAbility ability : card.getActivatedAbilities()) {
            for (CardEffect effect : ability.getEffects()) {
                if (effect instanceof SacrificeCreatureCost) {
                    return true;
                }
            }
        }
        return false;
    }

    private double evaluateAura(GameData gameData, Card card, UUID aiPlayerId, UUID opponentId,
                                List<Permanent> aiBattlefield, List<Permanent> oppBattlefield) {
        double value = 0;
        boolean isBeneficial = false;

        for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
            if (effect instanceof StaticBoostEffect boost
                    && (boost.scope() == GrantScope.ENCHANTED_CREATURE || boost.scope() == GrantScope.EQUIPPED_CREATURE)) {
                isBeneficial = true;
                value += boost.powerBoost() * 2.0 + boost.toughnessBoost();
            }
            if (effect instanceof GrantKeywordEffect grant
                    && grant.scope() == GrantScope.ENCHANTED_CREATURE) {
                isBeneficial = true;
                value += 3;
            }
        }

        if (isBeneficial) {
            // Only worth casting if we have a creature
            boolean hasCreature = aiBattlefield.stream()
                    .anyMatch(p -> gameQueryService.isCreature(gameData, p));
            return hasCreature ? value : 0;
        }

        // Detrimental aura - value based on neutralizing opponent's best creature
        double bestOppCreature = bestTargetCreatureValue(gameData, oppBattlefield, opponentId, aiPlayerId);
        return bestOppCreature > 0 ? bestOppCreature * 0.8 : 0;
    }

    private double evaluateDamageEffect(GameData gameData, int damage, List<Permanent> oppBattlefield,
                                        UUID opponentId, UUID aiPlayerId) {
        // Check if we can kill a creature — use threat score to value lords/abilities higher
        double bestKillValue = oppBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> gameQueryService.getEffectiveToughness(gameData, p) <= damage)
                .mapToDouble(p -> boardEvaluator.creatureThreatScore(gameData, p, opponentId, aiPlayerId))
                .max()
                .orElse(0);

        // Compare with face damage value
        double faceDamage = damage * 1.5;
        return Math.max(bestKillValue, faceDamage);
    }

    private double evaluateDamageToCreature(GameData gameData, int damage, List<Permanent> oppBattlefield,
                                            UUID opponentId, UUID aiPlayerId) {
        return oppBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> gameQueryService.getEffectiveToughness(gameData, p) <= damage)
                .mapToDouble(p -> boardEvaluator.creatureThreatScore(gameData, p, opponentId, aiPlayerId))
                .max()
                .orElse(damage * 1.0);
    }

    private double evaluateBoardWipeDamage(GameData gameData, int damage,
                                           UUID aiPlayerId, UUID opponentId,
                                           List<Permanent> aiBattlefield, List<Permanent> oppBattlefield) {
        double oppLosses = oppBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> gameQueryService.getEffectiveToughness(gameData, p) <= damage)
                .mapToDouble(p -> boardEvaluator.creatureScore(gameData, p, opponentId, aiPlayerId))
                .sum();

        double aiLosses = aiBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> gameQueryService.getEffectiveToughness(gameData, p) <= damage)
                .mapToDouble(p -> boardEvaluator.creatureScore(gameData, p, aiPlayerId, opponentId))
                .sum();

        return oppLosses - aiLosses;
    }

    private double evaluateDestroyAllValue(GameData gameData, DestroyAllPermanentsEffect wipe,
                                           UUID aiPlayerId, UUID opponentId,
                                           List<Permanent> aiBattlefield, List<Permanent> oppBattlefield) {
        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(aiPlayerId);

        double oppValue = oppBattlefield.stream()
                .filter(p -> gameQueryService.matchesPermanentPredicate(p, wipe.filter(), filterContext))
                .mapToDouble(p -> {
                    if (gameQueryService.isCreature(gameData, p)) {
                        return boardEvaluator.creatureScore(gameData, p, opponentId, aiPlayerId);
                    }
                    return p.getCard().getManaValue() * 3.0;
                })
                .sum();

        double aiValue = aiBattlefield.stream()
                .filter(p -> gameQueryService.matchesPermanentPredicate(p, wipe.filter(), filterContext))
                .mapToDouble(p -> {
                    if (gameQueryService.isCreature(gameData, p)) {
                        return boardEvaluator.creatureScore(gameData, p, aiPlayerId, opponentId);
                    }
                    return p.getCard().getManaValue() * 3.0;
                })
                .sum();

        return oppValue - aiValue;
    }

    private double bestTargetCreatureValue(GameData gameData, List<Permanent> battlefield,
                                           UUID controllerId, UUID opponentId) {
        return battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .mapToDouble(p -> boardEvaluator.creatureThreatScore(gameData, p, controllerId, opponentId))
                .max()
                .orElse(0);
    }

    /**
     * Estimates the maximum X value the AI could afford for an X spell by building
     * a virtual mana pool from the current pool + all untapped mana sources.
     */
    private int estimateMaxX(GameData gameData, Card card, UUID playerId) {
        if (card == null || card.getManaCost() == null) return 0;
        ManaCost cost = new ManaCost(card.getManaCost());
        if (!cost.hasX()) return 0;

        ManaPool virtualPool = new ManaPool();
        ManaPool currentPool = gameData.playerManaPools.get(playerId);
        if (currentPool != null) {
            for (ManaColor color : ManaColor.values()) {
                for (int i = 0; i < currentPool.get(color); i++) {
                    virtualPool.add(color);
                }
            }
        }

        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        for (Permanent perm : battlefield) {
            if (perm.isTapped()) continue;
            if (gameQueryService.isCreature(gameData, perm) && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) continue;
            for (CardEffect manaEffect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                if (manaEffect instanceof com.github.laxika.magicalvibes.model.effect.AwardManaEffect me) {
                    virtualPool.add(me.color(), me.amount());
                } else if (manaEffect instanceof com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect) {
                    virtualPool.add(ManaColor.COLORLESS);
                } else if (manaEffect instanceof com.github.laxika.magicalvibes.model.effect.AwardAnyColorChosenSubtypeCreatureManaEffect) {
                    virtualPool.add(ManaColor.COLORLESS);
                }
            }
        }

        if (card.getXColorRestriction() != null) {
            return cost.calculateMaxX(virtualPool, card.getXColorRestriction(), 0);
        }
        return cost.calculateMaxX(virtualPool);
    }

    /**
     * Evaluates how valuable a card is to keep in hand given the current board state,
     * for discard-to-hand-size decisions. Higher score = more worth keeping.
     *
     * Unlike estimateSpellValue (which scores "how good is this spell in a vacuum"),
     * this method adjusts for:
     * - Removal value: removal is more valuable when opponent has threats
     * - Land value: lands are more valuable when still ramping, less when flooded
     * - Redundancy: duplicate cards in hand are worth less (second copy discounted)
     * - Castability: uncastable cards (too expensive for available mana sources) are worth less
     */
    public double evaluateCardForDiscard(GameData gameData, Card card, List<Card> hand, UUID aiPlayerId) {
        UUID opponentId = getOpponentId(gameData, aiPlayerId);
        List<Permanent> aiBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        // Lands: value depends on how many mana sources the AI already has
        if (card.hasType(CardType.LAND)) {
            return evaluateLandForDiscard(gameData, card, hand, aiPlayerId, aiBattlefield);
        }

        // Start with base spell value
        double value = estimateSpellValue(gameData, card, aiPlayerId);

        // Removal bonus: if opponent has threatening creatures, removal is more valuable
        value += removalContextBonus(gameData, card, opponentId, oppBattlefield, aiPlayerId);

        // Castability penalty: cards we can't cast soon are less valuable to keep
        value *= castabilityMultiplier(card, aiBattlefield);

        // Redundancy penalty: second+ copy of same card in hand is worth less
        value *= redundancyMultiplier(card, hand);

        return value;
    }

    private double evaluateLandForDiscard(GameData gameData, Card card, List<Card> hand,
                                          UUID aiPlayerId, List<Permanent> aiBattlefield) {
        long manaSourceCount = aiBattlefield.stream()
                .filter(p -> p.getCard().hasType(CardType.LAND) || hasOnTapManaEffects(p.getCard()))
                .count();

        // Count how many lands are already in hand (excluding this one)
        long landsInHand = hand.stream()
                .filter(c -> c.hasType(CardType.LAND) && c != card)
                .count();

        // Still developing mana base (0-4 sources): lands are very valuable
        if (manaSourceCount <= 4) {
            return 12.0;
        }

        // Mid-game (5-6 sources): lands still useful but less critical
        if (manaSourceCount <= 6) {
            // Check if hand has expensive spells that need more mana
            boolean hasExpensiveSpells = hand.stream()
                    .filter(c -> !c.hasType(CardType.LAND))
                    .anyMatch(c -> c.getManaValue() > manaSourceCount);
            return hasExpensiveSpells ? 8.0 : 4.0;
        }

        // Late game (7+ sources): extra lands are mostly dead draws
        // But keep at least one land if we have expensive spells
        if (landsInHand > 0) {
            // Multiple lands in hand with 7+ mana sources — extra lands are low value
            return 1.0;
        }
        return 2.0;
    }

    private double removalContextBonus(GameData gameData, Card card, UUID opponentId,
                                       List<Permanent> oppBattlefield, UUID aiPlayerId) {
        // Check if this card has removal effects
        boolean isRemoval = hasRemovalEffects(card);
        if (!isRemoval) return 0;

        // Score based on the biggest threat the opponent has
        double bestThreat = oppBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .mapToDouble(p -> boardEvaluator.creatureThreatScore(gameData, p, opponentId, aiPlayerId))
                .max()
                .orElse(0);

        // If opponent has big threats, removal is extra valuable
        if (bestThreat >= 15.0) return 8.0;
        if (bestThreat >= 10.0) return 4.0;
        if (bestThreat >= 5.0) return 2.0;
        return 0;
    }

    private boolean hasRemovalEffects(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (isRemovalEffect(effect)) return true;
        }
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (isRemovalEffect(effect)) return true;
        }
        return false;
    }

    private boolean isRemovalEffect(CardEffect effect) {
        if (effect instanceof ChooseOneEffect coe) {
            return coe.options().stream().anyMatch(o -> isRemovalEffect(o.effect()));
        }
        return effect instanceof DestroyTargetPermanentEffect
                || effect instanceof ExileTargetPermanentEffect
                || effect instanceof DealDamageToAnyTargetEffect
                || effect instanceof DealDamageToTargetCreatureEffect
                || effect instanceof ReturnTargetPermanentToHandEffect
                || effect instanceof ReturnTargetPermanentToHandWithManaValueConditionalEffect
                || effect instanceof GainControlOfTargetPermanentEffect;
    }

    /**
     * Returns a multiplier (0.0–1.0) based on how likely we are to cast this card
     * given our available mana sources. Uncastable cards are discounted.
     */
    private double castabilityMultiplier(Card card, List<Permanent> aiBattlefield) {
        if (card.getManaCost() == null || card.getManaCost().isEmpty()) return 1.0;

        int manaValue = card.getManaValue();
        long totalMana = aiBattlefield.stream()
                .filter(p -> p.getCard().hasType(CardType.LAND) || hasOnTapManaEffects(p.getCard()))
                .count();

        // Can cast right now or next turn (with one more land drop)
        if (manaValue <= totalMana + 1) return 1.0;

        // 2-3 turns away: still keepable but slightly discounted
        if (manaValue <= totalMana + 3) return 0.8;

        // 4+ turns away: significantly discounted
        return 0.5;
    }

    /**
     * Returns a multiplier (0.5–1.0) that penalizes redundant copies of the same card.
     */
    private double redundancyMultiplier(Card card, List<Card> hand) {
        long copies = hand.stream()
                .filter(c -> c.getName() != null && c.getName().equals(card.getName()))
                .count();
        // First copy: full value. Second+: 50% value (discard the extra)
        return copies > 1 ? 0.5 : 1.0;
    }

    private boolean hasOnTapManaEffects(Card card) {
        return card.getEffects(EffectSlot.ON_TAP).stream()
                .anyMatch(ManaProducingEffect.class::isInstance);
    }

    private double lifeGainMultiplier(GameData gameData, UUID aiPlayerId, UUID opponentId) {
        int aiLife = gameData.getLife(aiPlayerId);
        int opponentBoardDamage = computeOpponentBoardDamage(gameData, opponentId);
        return BoardEvaluator.lifeGainMultiplier(opponentBoardDamage, aiLife);
    }

    private UUID getOpponentId(GameData gameData, UUID playerId) {
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }
}
