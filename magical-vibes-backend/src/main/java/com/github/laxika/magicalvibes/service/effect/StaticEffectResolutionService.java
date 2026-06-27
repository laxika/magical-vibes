package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.BlockedByMinCreaturesConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerControlsPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerGraveyardCardThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeAtOrBelowThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SelfHasKeywordConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TopCardOfLibraryColorConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.EquippedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentControlsPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentPoisonedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class StaticEffectResolutionService {

    private final GameQueryService gameQueryService;
    private final StaticEffectSupport support;

    @HandlesStaticEffect(value = GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect.class, selfOnly = true)
    private void resolveGainActivatedAbilitiesOfCreatureCardsInAllGraveyards(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        GameData gameData = context.gameData();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.hasType(CardType.CREATURE)) {
                    for (var ability : card.getActivatedAbilities()) {
                        accumulator.addActivatedAbility(ability);
                    }
                }
            }
        }
    }

    @HandlesStaticEffect(value = GainActivatedAbilitiesOfExiledCardsEffect.class, selfOnly = true)
    private void resolveGainActivatedAbilitiesOfExiledCards(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        List<Card> exiledCards = context.gameData().getCardsExiledByPermanent(context.source().getId());
        if (exiledCards.isEmpty()) return;
        for (Card card : exiledCards) {
            for (var ability : card.getActivatedAbilities()) {
                accumulator.addActivatedAbility(ability);
            }
        }
    }

    @HandlesStaticEffect(value = AnyPlayerControlsPermanentConditionalEffect.class, selfOnly = true)
    private void resolveAnyPlayerControlsPermanentConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (AnyPlayerControlsPermanentConditionalEffect) effect;
        final boolean[] found = {false};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (!found[0] && matchesStaticFilter(permanent, conditional.filter())) {
                found[0] = true;
            }
        });
        if (found[0]) {
            applySelfOnlyConditionalStaticEffect(context, conditional.wrapped(), accumulator);
        }
    }

    @HandlesStaticEffect(value = ControlsPermanentConditionalEffect.class, selfOnly = true)
    private void resolveControlsPermanentConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControlsPermanentConditionalEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) return;
        boolean found = battlefield.stream()
                .anyMatch(p -> matchesStaticFilter(p, conditional.filter()));
        if (found) {
            applySelfOnlyConditionalStaticEffect(context, conditional.wrapped(), accumulator);
        }
    }

    @HandlesStaticEffect(value = OpponentControlsPermanentConditionalEffect.class, selfOnly = true)
    private void resolveOpponentControlsPermanentConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (OpponentControlsPermanentConditionalEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        final boolean[] found = {false};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (!found[0]
                    && !playerId.equals(controllerId)
                    && matchesStaticFilter(permanent, conditional.filter())) {
                found[0] = true;
            }
        });
        if (found[0]) {
            applySelfOnlyConditionalStaticEffect(context, conditional.wrapped(), accumulator);
        }
    }

    private void applySelfOnlyConditionalStaticEffect(StaticEffectContext context, CardEffect wrapped, StaticBonusAccumulator accumulator) {
        support.applySelfOnlyConditionalStaticEffect(context, wrapped, accumulator);
    }

    @HandlesStaticEffect(value = EquippedConditionalEffect.class, selfOnly = true)
    private void resolveEquippedConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!isEquipped(context)) return;
        var equipped = (EquippedConditionalEffect) effect;
        CardEffect wrapped = equipped.wrapped();
        if (wrapped instanceof GrantKeywordEffect grant) {
            if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        } else if (wrapped instanceof StaticBoostEffect boost) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
            accumulator.addKeywords(boost.grantedKeywords());
        } else if (wrapped instanceof ProtectionFromColorsEffect protection) {
            accumulator.addProtectionColors(protection.colors());
        }
    }

    @HandlesStaticEffect(value = BlockedByMinCreaturesConditionalEffect.class, selfOnly = true)
    private void resolveBlockedByMinCreaturesConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (BlockedByMinCreaturesConditionalEffect) effect;
        UUID sourceId = context.source().getId();

        final int[] blockerCount = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.isBlocking() && permanent.getBlockingTargetIds().contains(sourceId)) {
                blockerCount[0]++;
            }
        });

        if (blockerCount[0] < conditional.minBlockers()) return;

        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
            accumulator.addKeywords(boost.grantedKeywords());
        } else if (wrapped instanceof GrantKeywordEffect grant) {
            accumulator.addKeywords(grant.keywords());
        }
    }

    @HandlesStaticEffect(ControllerTurnConditionalEffect.class)
    private void resolveControllerTurnConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerTurnConditionalEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        if (!controllerId.equals(context.gameData().activePlayerId)) return;

        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof GrantKeywordEffect grant) {
            if (matchesCreatureScope(context, grant.scope(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        } else if (wrapped instanceof StaticBoostEffect boost) {
            if (matchesCreatureScope(context, boost.scope(), null)) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            }
        }
    }

    private boolean isEquipped(StaticEffectContext context) {
        return support.isEquipped(context);
    }

    @HandlesStaticEffect(value = OpponentPoisonedConditionalEffect.class, selfOnly = true)
    private void resolveOpponentPoisonedConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (OpponentPoisonedConditionalEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        boolean opponentPoisoned = false;
        for (UUID playerId : context.gameData().orderedPlayerIds) {
            if (!playerId.equals(controllerId)
                    && context.gameData().playerPoisonCounters.getOrDefault(playerId, 0) > 0) {
                opponentPoisoned = true;
                break;
            }
        }
        if (opponentPoisoned) {
            CardEffect wrapped = conditional.wrapped();
            if (wrapped instanceof GrantKeywordEffect grant) {
                accumulator.addKeywords(grant.keywords());
            } else if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            } else if (wrapped instanceof ProtectionFromColorsEffect protection) {
                accumulator.addProtectionColors(protection.colors());
            }
        }
    }

    @HandlesStaticEffect(value = ControllerLifeThresholdConditionalEffect.class, selfOnly = true)
    private void resolveControllerLifeThresholdConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerLifeThresholdConditionalEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        int lifeTotal = context.gameData().playerLifeTotals.getOrDefault(controllerId, 20);
        if (lifeTotal >= conditional.lifeThreshold()) {
            CardEffect wrapped = conditional.wrapped();
            if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            } else if (wrapped instanceof GrantKeywordEffect grant) {
                if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                    accumulator.addKeywords(grant.keywords());
                }
            }
        }
    }

    @HandlesStaticEffect(value = ControllerLifeAtOrBelowThresholdConditionalEffect.class, selfOnly = true)
    private void resolveControllerLifeAtOrBelowThresholdConditionalSelf(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerLifeAtOrBelowThresholdConditionalEffect) effect;
        if (!isControllerLifeAtOrBelow(context, conditional.lifeThreshold())) return;
        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost) {
            if (boost.scope() == GrantScope.SELF || boost.scope() == GrantScope.ALL_OWN_CREATURES) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            }
        } else if (wrapped instanceof GrantKeywordEffect grant) {
            if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        }
    }

    @HandlesStaticEffect(ControllerLifeAtOrBelowThresholdConditionalEffect.class)
    private void resolveControllerLifeAtOrBelowThresholdConditionalOthers(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerLifeAtOrBelowThresholdConditionalEffect) effect;
        if (!isControllerLifeAtOrBelow(context, conditional.lifeThreshold())) return;
        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost && boost.scope() != GrantScope.SELF) {
            if (matchesCreatureScope(context, boost.scope(), boost.filter())) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            }
        } else if (wrapped instanceof GrantKeywordEffect grant && grant.scope() != GrantScope.SELF) {
            if (matchesCreatureScope(context, grant.scope(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        }
    }

    private boolean isControllerLifeAtOrBelow(StaticEffectContext context, int threshold) {
        return support.isControllerLifeAtOrBelow(context, threshold);
    }

    @HandlesStaticEffect(value = ControllerGraveyardCardThresholdConditionalEffect.class, selfOnly = true)
    private void resolveControllerGraveyardCardThresholdConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControllerGraveyardCardThresholdConditionalEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        List<Card> graveyard = context.gameData().playerGraveyards.get(controllerId);
        int count = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (gameQueryService.matchesCardPredicate(card, conditional.filter(), null)) {
                    count++;
                }
            }
        }
        if (count >= conditional.threshold()) {
            CardEffect wrapped = conditional.wrapped();
            if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            } else if (wrapped instanceof GrantKeywordEffect grant) {
                if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                    accumulator.addKeywords(grant.keywords());
                }
            }
        }
    }

    @HandlesStaticEffect(value = TopCardOfLibraryColorConditionalEffect.class, selfOnly = true)
    private void resolveTopCardColorConditionalSelf(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (TopCardOfLibraryColorConditionalEffect) effect;
        if (!isTopCardOfLibraryColor(context, conditional.color())) return;
        // Cards with "CARDNAME and other [type]" always buff themselves regardless of filter (CR 201.5).
        // The filter only applies to "other" creatures via the non-self handler.
        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
            accumulator.addKeywords(boost.grantedKeywords());
        } else if (wrapped instanceof GrantKeywordEffect grant) {
            accumulator.addKeywords(grant.keywords());
        }
    }

    @HandlesStaticEffect(TopCardOfLibraryColorConditionalEffect.class)
    private void resolveTopCardColorConditionalOthers(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (TopCardOfLibraryColorConditionalEffect) effect;
        CardEffect wrapped = conditional.wrapped();
        if (wrapped instanceof StaticBoostEffect boost && boost.scope() != GrantScope.SELF) {
            if (!isTopCardOfLibraryColor(context, conditional.color())) return;
            boolean scopeMatch = switch (boost.scope()) {
                case OWN_CREATURES, ALL_OWN_CREATURES -> context.targetOnSameBattlefield();
                case OPPONENT_CREATURES -> !context.targetOnSameBattlefield();
                case ALL_CREATURES -> true;
                default -> false;
            };
            if (scopeMatch && matchesStaticFilter(context.target(), boost.filter())) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            }
        } else if (wrapped instanceof GrantKeywordEffect grant && grant.scope() != GrantScope.SELF) {
            if (!isTopCardOfLibraryColor(context, conditional.color())) return;
            if (matchesCreatureScope(context, grant.scope(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        }
    }

    @HandlesStaticEffect(value = ControlsAnotherPermanentConditionalEffect.class, selfOnly = true)
    private void resolveControlsAnotherPermanentConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControlsAnotherPermanentConditionalEffect) effect;
        int matchCount = countControlledPermanents(context, p ->
                !p.getId().equals(context.source().getId())
                        && matchesStaticFilter(p, conditional.filter()));
        if (matchCount > 0) {
            CardEffect wrapped = conditional.wrapped();
            if (wrapped instanceof GrantKeywordEffect grant) {
                if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                    accumulator.addKeywords(grant.keywords());
                }
            } else if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            } else if (wrapped instanceof ProtectionFromColorsEffect protection) {
                accumulator.addProtectionColors(protection.colors());
            }
        }
    }

    @HandlesStaticEffect(value = SelfHasKeywordConditionalEffect.class, selfOnly = true)
    private void resolveSelfHasKeywordConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (SelfHasKeywordConditionalEffect) effect;
        if (context.source().hasKeyword(conditional.keyword())) {
            CardEffect wrapped = conditional.wrapped();
            if (wrapped instanceof GrantKeywordEffect grant) {
                accumulator.addKeywords(grant.keywords());
            } else if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            }
        }
    }

    private boolean isTopCardOfLibraryColor(StaticEffectContext context, CardColor color) {
        return support.isTopCardOfLibraryColor(context, color);
    }

    private int countControlledPermanents(StaticEffectContext context, Predicate<Permanent> filter) {
        return support.countControlledPermanents(context, filter);
    }

    private UUID findControllerId(GameData gameData, Permanent permanent) {
        return support.findControllerId(gameData, permanent);
    }

    private boolean matchesCreatureScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter) {
        return support.matchesCreatureScope(context, scope, filter);
    }

    private boolean isEffectivelyCreature(Permanent permanent, boolean hasAnimateArtifacts) {
        return support.isEffectivelyCreature(permanent, hasAnimateArtifacts);
    }

    private boolean isEffectivelyCreature(GameData gameData, Permanent permanent, boolean hasAnimateArtifacts) {
        return support.isEffectivelyCreature(gameData, permanent, hasAnimateArtifacts);
    }

    private boolean matchesStaticFilter(Permanent target, PermanentPredicate filter) {
        return support.matchesStaticFilter(target, filter);
    }

    private int countCardsInAllGraveyards(GameData gameData, CardPredicate filter) {
        return support.countCardsInAllGraveyards(gameData, filter);
    }

    private boolean hasAnimateArtifactEffect(GameData gameData) {
        return support.hasAnimateArtifactEffect(gameData);
    }
}

