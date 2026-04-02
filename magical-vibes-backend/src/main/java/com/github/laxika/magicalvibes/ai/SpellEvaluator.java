package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
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
     */
    public double estimateSpellValue(GameData gameData, Card card, UUID aiPlayerId) {
        UUID opponentId = getOpponentId(gameData, aiPlayerId);
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        List<Permanent> aiBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());

        if (card.hasType(CardType.CREATURE)) {
            return evaluateCreature(gameData, card, aiPlayerId, opponentId);
        }

        // Evaluate by scanning effects across all slots
        double totalValue = 0;

        totalValue += evaluateEffects(gameData, card, card.getEffects(EffectSlot.SPELL),
                aiPlayerId, opponentId, aiBattlefield, oppBattlefield);
        totalValue += evaluateEffects(gameData, card, card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD),
                aiPlayerId, opponentId, aiBattlefield, oppBattlefield);

        // Aura evaluation
        if (card.isAura()) {
            totalValue += evaluateAura(gameData, card, aiPlayerId, opponentId, aiBattlefield, oppBattlefield);
        }

        return totalValue;
    }

    private double evaluateCreature(GameData gameData, Card card, UUID aiPlayerId, UUID opponentId) {
        double value = boardEvaluator.creatureCardScore(card);

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
            return gain.amount() * 0.5;
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

        // Life
        if (effect instanceof GainLifeEffect gain) {
            return gain.amount() * 0.5;
        }
        if (effect instanceof LoseLifeEffect lose) {
            return -lose.amount() * 0.5;
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
                    + estimatedX * 0.5;
        }
        if (effect instanceof DealXDamageToTargetCreatureEffect) {
            int estimatedX = estimateMaxX(gameData, card, aiPlayerId);
            if (estimatedX <= 0) return 0;
            return evaluateDamageToCreature(gameData, estimatedX, oppBattlefield, opponentId, aiPlayerId);
        }
        if (effect instanceof EachOpponentLosesXLifeAndControllerGainsLifeLostEffect) {
            int estimatedX = estimateMaxX(gameData, card, aiPlayerId);
            if (estimatedX <= 0) return 0;
            return estimatedX * 1.5 + estimatedX * 0.5; // drain value
        }

        return 0;
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

    private UUID getOpponentId(GameData gameData, UUID playerId) {
        for (UUID id : gameData.orderedPlayerIds) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }
}
