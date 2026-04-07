package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Read-only evaluation function that scores a game state from the AI's perspective.
 * Higher scores are better for the AI player.
 */
public class BoardEvaluator {

    private static final double LIFE_WEIGHT = 2.0;
    private static final double CARD_ADVANTAGE_WEIGHT = 6.0;
    private static final double CREATURE_WEIGHT = 1.0;
    private static final double MANA_SOURCE_WEIGHT = 1.0;
    private static final double NON_CREATURE_PERMANENT_WEIGHT = 1.0;
    private static final double LOW_LIFE_BONUS = 10.0;
    private static final double GAME_OVER_SCORE = 100000.0;

    private final GameQueryService gameQueryService;

    public BoardEvaluator(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    /**
     * Evaluates the board state from the perspective of the given player.
     * Returns a score where higher is better for the AI.
     */
    public double evaluate(GameData gameData, UUID aiPlayerId) {
        UUID opponentId = getOpponentId(gameData, aiPlayerId);

        int aiLife = gameData.getLife(aiPlayerId);
        int oppLife = gameData.getLife(opponentId);
        int aiPoison = gameData.playerPoisonCounters.getOrDefault(aiPlayerId, 0);
        int oppPoison = gameData.playerPoisonCounters.getOrDefault(opponentId, 0);

        // Game-over detection
        if (oppLife <= 0 || oppPoison >= 10) return GAME_OVER_SCORE;
        if (aiLife <= 0 || aiPoison >= 10) return -GAME_OVER_SCORE;

        double score = 0.0;

        // Life differential
        score += (aiLife - oppLife) * LIFE_WEIGHT;

        // Low-life bonus/penalty
        if (oppLife <= 5) score += LOW_LIFE_BONUS;
        if (aiLife <= 5) score -= LOW_LIFE_BONUS;

        // Poison counter pressure
        if (oppPoison >= 7) score += LOW_LIFE_BONUS;
        if (aiPoison >= 7) score -= LOW_LIFE_BONUS;
        score += (oppPoison - aiPoison) * LIFE_WEIGHT;

        // Card advantage
        int aiHandSize = gameData.playerHands.getOrDefault(aiPlayerId, List.of()).size();
        int oppHandSize = gameData.playerHands.getOrDefault(opponentId, List.of()).size();
        score += (aiHandSize - oppHandSize) * CARD_ADVANTAGE_WEIGHT;

        // Battlefield evaluation
        List<Permanent> aiBattlefield = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());
        List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        double aiCreatureQuality = 0;
        double oppCreatureQuality = 0;
        int aiLandCount = 0;
        int oppLandCount = 0;
        double aiNonCreatureValue = 0;
        double oppNonCreatureValue = 0;

        for (Permanent perm : aiBattlefield) {
            if (gameQueryService.isCreature(gameData, perm)) {
                aiCreatureQuality += creatureScore(gameData, perm, aiPlayerId, opponentId);
            }
            if (perm.getCard().hasType(CardType.LAND)) {
                aiLandCount++;
            }
            if (perm.getCard().hasType(CardType.ENCHANTMENT) || perm.getCard().hasType(CardType.ARTIFACT)) {
                if (!gameQueryService.isCreature(gameData, perm)) {
                    aiNonCreatureValue += perm.getCard().getManaValue() * 3.0;
                }
            }
        }

        for (Permanent perm : oppBattlefield) {
            if (gameQueryService.isCreature(gameData, perm)) {
                oppCreatureQuality += creatureScore(gameData, perm, opponentId, aiPlayerId);
            }
            if (perm.getCard().hasType(CardType.LAND)) {
                oppLandCount++;
            }
            if (perm.getCard().hasType(CardType.ENCHANTMENT) || perm.getCard().hasType(CardType.ARTIFACT)) {
                if (!gameQueryService.isCreature(gameData, perm)) {
                    oppNonCreatureValue += perm.getCard().getManaValue() * 3.0;
                }
            }
        }

        score += (aiCreatureQuality - oppCreatureQuality) * CREATURE_WEIGHT;
        score += (aiLandCount - oppLandCount) * MANA_SOURCE_WEIGHT;
        score += (aiNonCreatureValue - oppNonCreatureValue) * NON_CREATURE_PERMANENT_WEIGHT;

        return score;
    }

    /**
     * Scores a creature permanent based on its effective stats, keywords, and current state.
     * Accounts for marked damage, summoning sickness, and tapped status.
     */
    public double creatureScore(GameData gameData, Permanent perm, UUID controllerId, UUID opponentId) {
        int power = gameQueryService.getEffectivePower(gameData, perm);
        int toughness = gameQueryService.getEffectiveToughness(gameData, perm);

        // A 4/4 with 3 damage on it is barely a 4/1 — use remaining toughness for scoring
        int effectiveToughness = Math.max(0, toughness - perm.getMarkedDamage());

        double score = power * 3.0 + effectiveToughness * 1.5;
        score += keywordBonus(gameData, perm, controllerId, opponentId);

        // Summoning-sick creatures can't attack yet (unless they have haste)
        if (perm.isSummoningSick() && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) {
            score -= 2.0;
        }

        // Tapped creatures can't block — discount their defensive value
        if (perm.isTapped()) {
            score -= 1.5;
        }

        return score;
    }

    /**
     * Scores a creature for removal targeting, considering contextual threat beyond raw stats.
     * A 2/2 lord pumping four creatures is far more dangerous than a vanilla 4/4.
     */
    public double creatureThreatScore(GameData gameData, Permanent perm, UUID controllerId, UUID opponentId) {
        double score = creatureScore(gameData, perm, controllerId, opponentId);
        score += lordBonus(gameData, perm, controllerId);
        score += activatedAbilityThreat(perm);
        score += evasionContextBonus(gameData, perm, controllerId, opponentId);
        score += growthThreatBonus(perm);
        return score;
    }

    /**
     * Calculates the bonus value of a permanent that acts as a lord/anthem source.
     * Counts how many allied creatures benefit from each static boost and scores accordingly.
     */
    private double lordBonus(GameData gameData, Permanent perm, UUID controllerId) {
        double bonus = 0;
        List<Permanent> controllerBattlefield = gameData.playerBattlefields.getOrDefault(controllerId, List.of());

        for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof StaticBoostEffect boost) {
                GrantScope scope = boost.scope();
                if (scope != GrantScope.OWN_CREATURES && scope != GrantScope.ALL_OWN_CREATURES
                        && scope != GrantScope.ALL_CREATURES) {
                    continue;
                }
                int buffedCount = countBuffedCreatures(gameData, perm, controllerBattlefield, boost.filter(), scope);
                // Use same weights as creatureScore: power*3 + toughness*1.5
                double perCreatureValue = boost.powerBoost() * 3.0 + boost.toughnessBoost() * 1.5;
                if (boost.grantedKeywords() != null) {
                    perCreatureValue += boost.grantedKeywords().size() * 3.0;
                }
                bonus += buffedCount * perCreatureValue;
            } else if (effect instanceof GrantKeywordEffect grant) {
                GrantScope scope = grant.scope();
                if (scope != GrantScope.OWN_CREATURES && scope != GrantScope.ALL_OWN_CREATURES
                        && scope != GrantScope.ALL_CREATURES) {
                    continue;
                }
                int buffedCount = countBuffedCreatures(gameData, perm, controllerBattlefield, grant.filter(), scope);
                double perCreatureValue = grant.keywords().size() * 3.0;
                bonus += buffedCount * perCreatureValue;
            }
        }
        return bonus;
    }

    private int countBuffedCreatures(GameData gameData, Permanent source,
                                     List<Permanent> battlefield,
                                     com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter,
                                     GrantScope scope) {
        int count = 0;
        FilterContext ctx = FilterContext.of(gameData);
        for (Permanent p : battlefield) {
            if (!gameQueryService.isCreature(gameData, p)) continue;
            // OWN_CREATURES excludes the source itself
            if (scope == GrantScope.OWN_CREATURES && p.getId().equals(source.getId())) continue;
            if (filter != null && !gameQueryService.matchesPermanentPredicate(p, filter, ctx)) continue;
            count++;
        }
        return count;
    }

    /**
     * Bonus for creatures with dangerous activated abilities (damage, removal, card draw).
     */
    private double activatedAbilityThreat(Permanent perm) {
        double bonus = 0;
        for (ActivatedAbility ability : perm.getCard().getActivatedAbilities()) {
            for (CardEffect effect : ability.getEffects()) {
                if (effect instanceof CostEffect) continue;
                if (effect instanceof DealDamageToAnyTargetEffect dmg) {
                    bonus += dmg.damage() * 2.0;
                } else if (effect instanceof DealDamageToTargetCreatureEffect dmg) {
                    bonus += dmg.damage() * 2.0;
                } else if (effect instanceof DestroyTargetPermanentEffect) {
                    bonus += 8.0;
                } else if (effect instanceof ExileTargetPermanentEffect) {
                    bonus += 9.0;
                } else if (effect instanceof DrawCardEffect draw) {
                    bonus += draw.amount() * 4.0;
                }
            }
        }
        return bonus;
    }

    /**
     * Extra bonus for evasive creatures when the opponent cannot block them.
     * Covers all evasion types: cant-be-blocked, flying, fear, intimidate,
     * menace (≤1 blocker), and landwalk.
     */
    private double evasionContextBonus(GameData gameData, Permanent perm, UUID controllerId, UUID opponentId) {
        if (opponentId == null) return 0;

        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        List<Permanent> opponentCreatures = opponentBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .toList();

        // Unconditional cant-be-blocked is the strongest form of evasion
        if (gameQueryService.hasCantBeBlocked(gameData, perm)) {
            return 8.0;
        }

        double bonus = 0;

        if (gameQueryService.hasKeyword(gameData, perm, Keyword.FLYING)) {
            boolean opponentCanBlockFlyers = opponentCreatures.stream()
                    .anyMatch(p -> gameQueryService.hasKeyword(gameData, p, Keyword.FLYING)
                            || gameQueryService.hasKeyword(gameData, p, Keyword.REACH));
            if (!opponentCanBlockFlyers) {
                bonus += 6.0;
            }
        }

        if (gameQueryService.hasKeyword(gameData, perm, Keyword.FEAR)) {
            boolean opponentCanBlockFear = opponentCreatures.stream()
                    .anyMatch(p -> gameQueryService.isArtifact(p)
                            || p.getEffectiveColor() == CardColor.BLACK);
            if (!opponentCanBlockFear) {
                bonus += 6.0;
            }
        }

        if (gameQueryService.hasKeyword(gameData, perm, Keyword.INTIMIDATE)) {
            CardColor attackerColor = perm.getEffectiveColor();
            boolean opponentCanBlockIntimidate = opponentCreatures.stream()
                    .anyMatch(p -> gameQueryService.isArtifact(p)
                            || p.getEffectiveColor() == attackerColor);
            if (!opponentCanBlockIntimidate) {
                bonus += 6.0;
            }
        }

        if (gameQueryService.hasKeyword(gameData, perm, Keyword.MENACE)) {
            // Menace requires 2 blockers — if opponent has ≤1 creature, it's effectively unblockable
            if (opponentCreatures.size() <= 1) {
                bonus += 6.0;
            }
        }

        // Landwalk: unblockable if opponent has matching land type
        for (var entry : Keyword.LANDWALK_MAP.entrySet()) {
            if (gameQueryService.hasKeyword(gameData, perm, entry.getKey())
                    && opponentBattlefield.stream()
                            .anyMatch(p -> p.getCard().getSubtypes().contains(entry.getValue()))) {
                bonus += 6.0;
                break; // One landwalk is enough for the evasion bonus
            }
        }

        return bonus;
    }

    /**
     * Bonus for slith-type creatures that grow when they deal combat damage to a player.
     */
    private double growthThreatBonus(Permanent perm) {
        double bonus = 0;
        for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)) {
            if (effect instanceof PutCountersOnSourceEffect counters) {
                bonus += counters.amount() * (counters.powerModifier() + counters.toughnessModifier()) * 3.0;
            }
        }
        return bonus;
    }

    /**
     * Scores a creature card (not yet on the battlefield) based on its base stats and keywords.
     * Context-aware version: scales lifelink value by the AI's danger level.
     */
    public double creatureCardScore(GameData gameData, Card card, UUID aiPlayerId) {
        int power = card.getPower() != null ? card.getPower() : 0;
        int toughness = card.getToughness() != null ? card.getToughness() : 0;

        double score = power * 3.0 + toughness * 1.5;

        UUID opponentId = getOpponentId(gameData, aiPlayerId);
        int oppBoardDamage = computeOpponentBoardDamage(gameData, opponentId);
        int aiLife = gameData.getLife(aiPlayerId);
        double lgMultiplier = lifeGainMultiplier(oppBoardDamage, aiLife);

        Set<Keyword> keywords = card.getKeywords();
        if (keywords.contains(Keyword.FLYING)) score += 4;
        if (keywords.contains(Keyword.FIRST_STRIKE)) score += 3;
        if (keywords.contains(Keyword.DOUBLE_STRIKE)) score += 6;
        if (keywords.contains(Keyword.TRAMPLE)) score += 2;
        if (keywords.contains(Keyword.VIGILANCE)) score += 2;
        if (keywords.contains(Keyword.LIFELINK)) score += 3 * lgMultiplier;
        if (keywords.contains(Keyword.INDESTRUCTIBLE)) score += 5;
        if (keywords.contains(Keyword.MENACE)) score += 2;
        if (keywords.contains(Keyword.HEXPROOF)) score += 3;
        if (keywords.contains(Keyword.SHROUD)) score += 3;
        if (keywords.contains(Keyword.FEAR)) score += 2;
        if (keywords.contains(Keyword.INTIMIDATE)) score += 2;
        if (keywords.contains(Keyword.DEFENDER)) score -= 3;
        if (keywords.contains(Keyword.HASTE)) score += 1;
        if (keywords.contains(Keyword.INFECT)) score += 4;

        return score;
    }

    private double keywordBonus(GameData gameData, Permanent perm, UUID controllerId, UUID opponentId) {
        double bonus = 0;

        // Unconditional cant-be-blocked: highly valuable
        if (gameQueryService.hasCantBeBlocked(gameData, perm)) {
            bonus += 8;
        }

        // Flying is worth more when opponent has no flyers or reach (effectively unblockable)
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.FLYING)) {
            if (opponentId != null) {
                boolean opponentCanBlockFlyers = gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .anyMatch(p -> gameQueryService.hasKeyword(gameData, p, Keyword.FLYING)
                                || gameQueryService.hasKeyword(gameData, p, Keyword.REACH));
                bonus += opponentCanBlockFlyers ? 4 : 8;
            } else {
                bonus += 4;
            }
        }
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.FIRST_STRIKE)) bonus += 3;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.DOUBLE_STRIKE)) bonus += 6;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.TRAMPLE)) bonus += 2;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.VIGILANCE)) bonus += 2;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.LIFELINK)) {
            int oppBoardDamage = computeOpponentBoardDamage(gameData, opponentId);
            int controllerLife = gameData.getLife(controllerId);
            bonus += 3 * lifeGainMultiplier(oppBoardDamage, controllerLife);
        }
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) bonus += 5;

        // Menace: worth more when opponent has ≤1 creature (effectively unblockable)
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.MENACE)) {
            if (opponentId != null) {
                long opponentCreatureCount = gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .count();
                bonus += opponentCreatureCount <= 1 ? 6 : 2;
            } else {
                bonus += 2;
            }
        }

        if (gameQueryService.hasKeyword(gameData, perm, Keyword.HEXPROOF)) bonus += 3;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.SHROUD)) bonus += 3;

        // Fear: worth more when opponent has no black creatures or artifacts
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.FEAR)) {
            if (opponentId != null) {
                boolean opponentCanBlockFear = gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .anyMatch(p -> gameQueryService.isArtifact(p)
                                || p.getEffectiveColor() == CardColor.BLACK);
                bonus += opponentCanBlockFear ? 2 : 8;
            } else {
                bonus += 2;
            }
        }

        // Intimidate: worth more when opponent has no same-color creatures or artifacts
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.INTIMIDATE)) {
            if (opponentId != null) {
                CardColor attackerColor = perm.getEffectiveColor();
                boolean opponentCanBlockIntimidate = gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .anyMatch(p -> gameQueryService.isArtifact(p)
                                || p.getEffectiveColor() == attackerColor);
                bonus += opponentCanBlockIntimidate ? 2 : 8;
            } else {
                bonus += 2;
            }
        }

        if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) bonus -= 3;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) bonus += 1;
        if (gameQueryService.hasKeyword(gameData, perm, Keyword.INFECT)) bonus += 4;

        // Landwalk bonuses: effectively unblockable if opponent has matching land type
        if (opponentId != null) {
            List<Permanent> oppBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
            for (var entry : Keyword.LANDWALK_MAP.entrySet()) {
                if (gameQueryService.hasKeyword(gameData, perm, entry.getKey())
                        && oppBattlefield.stream().anyMatch(p -> p.getCard().getSubtypes().contains(entry.getValue()))) {
                    bonus += 6;
                }
            }
        }

        return bonus;
    }

    /**
     * Computes the total power of non-defender creatures an opponent controls.
     */
    int computeOpponentBoardDamage(GameData gameData, UUID opponentId) {
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

    /**
     * Returns a multiplier (0.3–3.0) that scales life gain/loss value by the controller's
     * danger level. At 20 life with 5 opponent damage, multiplier ≈ 0.3 (life gain is cheap).
     * At 5 life with 8 damage incoming, multiplier ≈ 1.6 (life gain is very valuable).
     */
    static double lifeGainMultiplier(int opponentBoardDamage, int controllerLife) {
        if (controllerLife <= 0 || opponentBoardDamage <= 0) return 0.3;
        return Math.max(0.3, Math.min(3.0, (double) opponentBoardDamage / controllerLife));
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
