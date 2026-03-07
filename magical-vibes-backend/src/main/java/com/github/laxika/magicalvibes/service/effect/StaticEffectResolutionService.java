package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEquipByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreaturePerCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreaturePerControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreaturePerMatchingLandNameEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreaturePerControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerEquipmentAttachedEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerEnchantmentOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfByImprintedCreaturePTEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOpponentPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.EquippedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledPermanentCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledLandCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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

    @HandlesStaticEffect(AnimateNoncreatureArtifactsEffect.class)
    private void resolveAnimateNoncreatureArtifacts(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (gameQueryService.isArtifact(context.target())) {
            accumulator.setAnimatedCreature(true);
        }
    }

    @HandlesStaticEffect(GrantEquipByManaValueEffect.class)
    private void resolveGrantEquipByManaValue(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantEquipByManaValueEffect) effect;
        Permanent target = context.target();
        GameData gameData = context.gameData();
        boolean hasAnimateArtifacts = hasAnimateArtifactEffect(gameData);

        // Grant equip ability to matching permanents
        if (matchesStaticFilter(target, grant.filter())) {
            int manaValue = target.getCard().getManaValue();
            String cost = "{" + manaValue + "}";
            accumulator.addActivatedAbility(new ActivatedAbility(
                    false,
                    cost,
                    List.of(new EquipEffect()),
                    "Equip " + cost,
                    new ControlledPermanentPredicateTargetFilter(
                            new PermanentIsCreaturePredicate(),
                            "Target must be a creature you control"
                    ),
                    null,
                    null,
                    ActivationTimingRestriction.SORCERY_SPEED
            ));
        }

        // Boost creatures with matching permanents attached
        if (isEffectivelyCreature(gameData, target, hasAnimateArtifacts)) {
            gameData.forEachPermanent((playerId, permanent) -> {
                if (permanent.getAttachedTo() != null
                        && permanent.getAttachedTo().equals(target.getId())
                        && matchesStaticFilter(permanent, grant.filter())) {
                    accumulator.addPower(permanent.getCard().getManaValue());
                }
            });
        }
    }

    @HandlesStaticEffect(BoostAttachedCreatureEffect.class)
    private void resolveBoostAttachedCreature(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostAttachedCreatureEffect) effect;
        if (context.source().getAttachedTo() != null
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
        }
    }

    @HandlesStaticEffect(BoostAttachedCreaturePerCardsInAllGraveyardsEffect.class)
    private void resolveBoostAttachedCreaturePerCardsInAllGraveyards(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostAttachedCreaturePerCardsInAllGraveyardsEffect) effect;
        if (context.source().getAttachedTo() == null
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }

        int count = countCardsInAllGraveyards(context.gameData(), boost.filter());
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    @HandlesStaticEffect(BoostAttachedCreaturePerMatchingLandNameEffect.class)
    private void resolveBoostAttachedCreaturePerMatchingLandName(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostAttachedCreaturePerMatchingLandNameEffect) effect;
        if (context.source().getAttachedTo() == null
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }

        Card imprintedCard = context.source().getCard().getImprintedCard();
        if (imprintedCard == null) {
            return;
        }

        String imprintedName = imprintedCard.getName();
        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().getType() == CardType.LAND
                    || permanent.getCard().getAdditionalTypes().contains(CardType.LAND)) {
                if (imprintedName.equals(permanent.getCard().getName())) {
                    count[0]++;
                }
            }
        });

        accumulator.addPower(count[0] * boost.powerPerMatch());
        accumulator.addToughness(count[0] * boost.toughnessPerMatch());
    }

    @HandlesStaticEffect(BoostAttachedCreaturePerControlledSubtypeEffect.class)
    private void resolveBoostAttachedCreaturePerControlledSubtype(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostAttachedCreaturePerControlledSubtypeEffect) effect;
        if (context.source().getAttachedTo() == null
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }

        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) {
            return;
        }

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getSubtypes().contains(boost.subtype())) {
                count++;
            }
        }

        accumulator.addPower(count * boost.powerPerSubtype());
        accumulator.addToughness(count * boost.toughnessPerSubtype());
    }

    @HandlesStaticEffect(ProtectionFromColorsEffect.class)
    private void resolveProtectionFromColors(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var protection = (ProtectionFromColorsEffect) effect;
        if (context.source().getAttachedTo() != null
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addProtectionColors(protection.colors());
        }
    }

    @HandlesStaticEffect(BoostEnchantedCreaturePerControlledSubtypeEffect.class)
    private void resolveBoostEnchantedCreaturePerControlledSubtype(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostEnchantedCreaturePerControlledSubtypeEffect) effect;
        if (context.source().getAttachedTo() == null
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }

        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) {
            return;
        }

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getSubtypes().contains(boost.subtype())) {
                count++;
            }
        }

        accumulator.addPower(count * boost.powerPerSubtype());
        accumulator.addToughness(count * boost.toughnessPerSubtype());
    }

    @HandlesStaticEffect(GrantKeywordEffect.class)
    private void resolveGrantKeyword(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantKeywordEffect) effect;
        if (matchesCreatureScope(context, grant.scope(), grant.filter())) {
            accumulator.addKeyword(grant.keyword());
        }
    }

    @HandlesStaticEffect(RemoveKeywordEffect.class)
    private void resolveRemoveKeyword(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var remove = (RemoveKeywordEffect) effect;
        if (matchesCreatureScope(context, remove.scope(), remove.filter())) {
            accumulator.removeKeyword(remove.keyword());
        }
    }

    @HandlesStaticEffect(GrantColorEffect.class)
    private void resolveGrantColor(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantColorEffect) effect;
        if (matchesCreatureScope(context, grant.scope(), null)) {
            accumulator.addGrantedColor(grant.color());
            if (grant.overriding()) {
                accumulator.setColorOverriding(true);
            }
        }
    }

    @HandlesStaticEffect(GrantSubtypeEffect.class)
    private void resolveGrantSubtype(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantSubtypeEffect) effect;
        boolean matches = grant.scope() == GrantScope.ALL_PERMANENTS
                ? matchesStaticFilter(context.target(), grant.filter())
                : matchesCreatureScope(context, grant.scope(), null);
        if (matches) {
            accumulator.addGrantedSubtype(grant.subtype());
            if (grant.overriding()) {
                accumulator.setSubtypeOverriding(true);
            }
        }
    }

    @HandlesStaticEffect(EnchantedPermanentBecomesTypeEffect.class)
    private void resolveEnchantedPermanentBecomesType(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var becomesType = (EnchantedPermanentBecomesTypeEffect) effect;
        if (context.source().getAttachedTo() != null
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addGrantedSubtype(becomesType.subtype());
            accumulator.setSubtypeOverriding(true);
            if (becomesType.isBasicLandSubtype()) {
                accumulator.setLandSubtypeOverriding(true);
            }
        }
    }

    @HandlesStaticEffect(GrantCardTypeEffect.class)
    private void resolveGrantCardType(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantCardTypeEffect) effect;
        if (matchesCreatureScope(context, grant.scope(), null)) {
            accumulator.addGrantedCardType(grant.cardType());
        }
    }

    @HandlesStaticEffect(GrantEffectEffect.class)
    private void resolveGrantEffectEffect(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantEffectEffect) effect;
        if (matchesCreatureScope(context, grant.scope(), grant.filter())) {
            accumulator.addGrantedEffect(grant.effect());
        }
    }

    @HandlesStaticEffect(BoostCreaturesOfChosenColorEffect.class)
    private void resolveBoostCreaturesOfChosenColor(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturesOfChosenColorEffect) effect;
        CardColor chosenColor = context.source().getChosenColor();
        if (chosenColor == null) return;
        if (!context.targetOnSameBattlefield()) return;
        Permanent target = context.target();
        boolean colorMatch = false;
        if (target.isColorOverridden()) {
            colorMatch = target.getGrantedColors().contains(chosenColor);
        } else {
            CardColor effectiveColor = target.getEffectiveColor();
            colorMatch = (effectiveColor != null && effectiveColor == chosenColor)
                    || target.getGrantedColors().contains(chosenColor);
        }
        if (colorMatch) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
        }
    }

    @HandlesStaticEffect(StaticBoostEffect.class)
    private void resolveStaticBoost(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (StaticBoostEffect) effect;
        boolean scopeMatch = switch (boost.scope()) {
            case OWN_CREATURES -> context.targetOnSameBattlefield();
            case OPPONENT_CREATURES -> !context.targetOnSameBattlefield();
            case ALL_CREATURES -> true;
            default -> false;
        };
        if (scopeMatch && matchesStaticFilter(context.target(), boost.filter())) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
            accumulator.addKeywords(boost.grantedKeywords());
        }
    }

    @HandlesStaticEffect(GrantActivatedAbilityEffect.class)
    private void resolveGrantActivatedAbility(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantActivatedAbilityEffect) effect;
        boolean scopeMatch = switch (grant.scope()) {
            case OWN_PERMANENTS -> context.targetOnSameBattlefield()
                    && matchesStaticFilter(context.target(), grant.filter());
            default -> matchesCreatureScope(context, grant.scope(), grant.filter());
        };
        if (scopeMatch) {
            accumulator.addActivatedAbility(grant.ability());
        }
    }

    @HandlesStaticEffect(BoostBySharedCreatureTypeEffect.class)
    private void resolveBoostBySharedCreatureType(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Permanent target = context.target();
        GameData gameData = context.gameData();

        List<CardSubtype> targetTypes = new ArrayList<>(target.getCard().getSubtypes());
        targetTypes.addAll(target.getGrantedSubtypes());
        boolean targetIsChangeling = target.hasKeyword(Keyword.CHANGELING);

        if (targetTypes.isEmpty() && !targetIsChangeling) return;

        boolean hasAnimateArtifacts = hasAnimateArtifactEffect(gameData);
        final int[] count = {0};

        gameData.forEachPermanent((playerId, other) -> {
            if (other == target) return;
            if (!isEffectivelyCreature(other, hasAnimateArtifacts)) return;

            List<CardSubtype> otherTypes = new ArrayList<>(other.getCard().getSubtypes());
            otherTypes.addAll(other.getGrantedSubtypes());
            boolean otherIsChangeling = other.hasKeyword(Keyword.CHANGELING);

            if (otherTypes.isEmpty() && !otherIsChangeling) return;

            boolean sharesType = (targetIsChangeling && (otherIsChangeling || !otherTypes.isEmpty()))
                    || (otherIsChangeling && !targetTypes.isEmpty())
                    || targetTypes.stream().anyMatch(otherTypes::contains);

            if (sharesType) count[0]++;
        });

        accumulator.addPower(count[0]);
        accumulator.addToughness(count[0]);
    }

    @HandlesStaticEffect(value = MetalcraftConditionalEffect.class, selfOnly = true)
    private void resolveMetalcraftConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var metalcraft = (MetalcraftConditionalEffect) effect;
        int artifactCount = countControlledPermanents(context, gameQueryService::isArtifact);
        if (artifactCount >= 3) {
            CardEffect wrapped = metalcraft.wrapped();
            if (wrapped instanceof GrantKeywordEffect grant) {
                // For SELF scope, always apply; for broader scopes, only apply if self matches filter
                if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                    accumulator.addKeyword(grant.keyword());
                }
            } else if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            } else if (wrapped instanceof ProtectionFromColorsEffect protection) {
                accumulator.addProtectionColors(protection.colors());
            } else if (wrapped instanceof AnimateSelfWithStatsEffect animate) {
                accumulator.setSelfBecomeCreature(true);
                accumulator.addPower(animate.power());
                accumulator.addToughness(animate.toughness());
                for (CardSubtype subtype : animate.grantedSubtypes()) {
                    accumulator.addGrantedSubtype(subtype);
                }
                accumulator.addKeywords(animate.grantedKeywords());
            }
        }
    }

    @HandlesStaticEffect(value = MetalcraftConditionalEffect.class)
    private void resolveMetalcraftConditionalOthers(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var metalcraft = (MetalcraftConditionalEffect) effect;
        CardEffect wrapped = metalcraft.wrapped();
        // Only handle broader-scoped effects in the non-self handler
        if (wrapped instanceof GrantKeywordEffect grant && grant.scope() != GrantScope.SELF) {
            int artifactCount = countControlledPermanents(context, gameQueryService::isArtifact);
            if (artifactCount >= 3) {
                boolean scopeMatch = switch (grant.scope()) {
                    case OWN_PERMANENTS -> context.targetOnSameBattlefield()
                            && matchesStaticFilter(context.target(), grant.filter());
                    default -> matchesCreatureScope(context, grant.scope(), grant.filter());
                };
                if (scopeMatch) {
                    accumulator.addKeyword(grant.keyword());
                }
            }
        } else if (wrapped instanceof StaticBoostEffect boost && boost.scope() != GrantScope.SELF) {
            int artifactCount = countControlledPermanents(context, gameQueryService::isArtifact);
            if (artifactCount >= 3) {
                boolean scopeMatch = switch (boost.scope()) {
                    case OWN_CREATURES -> context.targetOnSameBattlefield();
                    case OPPONENT_CREATURES -> !context.targetOnSameBattlefield();
                    case ALL_CREATURES -> true;
                    default -> false;
                };
                if (scopeMatch && matchesStaticFilter(context.target(), boost.filter())) {
                    accumulator.addPower(boost.powerBoost());
                    accumulator.addToughness(boost.toughnessBoost());
                    accumulator.addKeywords(boost.grantedKeywords());
                }
            }
        } else if (wrapped instanceof GrantActivatedAbilityEffect grant) {
            int artifactCount = countControlledPermanents(context, gameQueryService::isArtifact);
            if (artifactCount >= 3) {
                boolean scopeMatch = switch (grant.scope()) {
                    case OWN_PERMANENTS -> context.targetOnSameBattlefield()
                            && matchesStaticFilter(context.target(), grant.filter());
                    default -> matchesCreatureScope(context, grant.scope(), grant.filter());
                };
                if (scopeMatch) {
                    accumulator.addActivatedAbility(grant.ability());
                }
            }
        }
    }

    /**
     * Returns true if the target matches the given creature-centric scope.
     * Handles ENCHANTED_CREATURE, EQUIPPED_CREATURE, OWN_TAPPED_CREATURES, OWN_CREATURES, ALL_CREATURES.
     */
    private boolean matchesCreatureScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter) {
        if (scope == GrantScope.ENCHANTED_CREATURE || scope == GrantScope.EQUIPPED_CREATURE) {
            return context.source().getAttachedTo() != null
                    && context.source().getAttachedTo().equals(context.target().getId());
        }
        if (scope == GrantScope.OWN_TAPPED_CREATURES) {
            return context.targetOnSameBattlefield() && context.target().isTapped();
        }
        if (scope == GrantScope.OWN_CREATURES || scope == GrantScope.OPPONENT_CREATURES || scope == GrantScope.ALL_CREATURES) {
            boolean ownCheck = scope == GrantScope.ALL_CREATURES
                    || (scope == GrantScope.OWN_CREATURES && context.targetOnSameBattlefield())
                    || (scope == GrantScope.OPPONENT_CREATURES && !context.targetOnSameBattlefield());
            if (!ownCheck) return false;
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            return isEffectivelyCreature(context.gameData(), context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context.target(), filter);
        }
        return false;
    }

    private boolean isEffectivelyCreature(Permanent permanent, boolean hasAnimateArtifacts) {
        return isEffectivelyCreature(null, permanent, hasAnimateArtifacts);
    }

    private boolean isEffectivelyCreature(GameData gameData, Permanent permanent, boolean hasAnimateArtifacts) {
        if (permanent.getCard().getType() == CardType.CREATURE) return true;
        if (permanent.getCard().getAdditionalTypes().contains(CardType.CREATURE)) return true;
        if (permanent.isAnimatedUntilEndOfTurn()) return true;
        if (permanent.getAwakeningCounters() > 0) return true;
        if (hasAnimateArtifacts && gameQueryService.isArtifact(permanent)) return true;
        if (gameData != null) return gameQueryService.hasSelfBecomeCreatureEffect(gameData, permanent);
        return false;
    }

    @HandlesStaticEffect(value = PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToCreatureCardsInAllGraveyards(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        int count = countCardsInAllGraveyards(context.gameData(), new CardTypePredicate(CardType.CREATURE));
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    @HandlesStaticEffect(value = GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect.class, selfOnly = true)
    private void resolveGainActivatedAbilitiesOfCreatureCardsInAllGraveyards(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        GameData gameData = context.gameData();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.getType() == CardType.CREATURE || card.getAdditionalTypes().contains(CardType.CREATURE)) {
                    for (var ability : card.getActivatedAbilities()) {
                        accumulator.addActivatedAbility(ability);
                    }
                }
            }
        }
    }

    @HandlesStaticEffect(value = GainActivatedAbilitiesOfExiledCardsEffect.class, selfOnly = true)
    private void resolveGainActivatedAbilitiesOfExiledCards(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        List<Card> exiledCards = context.gameData().permanentExiledCards.get(context.source().getId());
        if (exiledCards == null) return;
        for (Card card : exiledCards) {
            for (var ability : card.getActivatedAbilities()) {
                accumulator.addActivatedAbility(ability);
            }
        }
    }

    @HandlesStaticEffect(value = BoostByOtherCreaturesWithSameNameEffect.class, selfOnly = true)
    private void resolveBoostByOtherCreaturesWithSameName(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostByOtherCreaturesWithSameNameEffect) effect;
        String sourceName = context.source().getCard().getName();
        GameData gameData = context.gameData();
        boolean hasAnimateArtifacts = hasAnimateArtifactEffect(gameData);

        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getId().equals(context.source().getId())) return;
            if (!isEffectivelyCreature(permanent, hasAnimateArtifacts)) return;
            if (!sourceName.equals(permanent.getCard().getName())) return;
            count[0]++;
        });

        accumulator.addPower(count[0] * boost.powerPerCreature());
        accumulator.addToughness(count[0] * boost.toughnessPerCreature());
    }

    @HandlesStaticEffect(value = BoostSelfPerEnchantmentOnBattlefieldEffect.class, selfOnly = true)
    private void resolveBoostSelfPerEnchantmentOnBattlefield(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerEnchantmentOnBattlefieldEffect) effect;
        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().getType() == CardType.ENCHANTMENT
                    || permanent.getCard().getAdditionalTypes().contains(CardType.ENCHANTMENT)) {
                count[0]++;
            }
        });
        accumulator.addPower(count[0] * boost.powerPerEnchantment());
        accumulator.addToughness(count[0] * boost.toughnessPerEnchantment());
    }

    @HandlesStaticEffect(value = BoostSelfPerEquipmentAttachedEffect.class, selfOnly = true)
    private void resolveBoostSelfPerEquipmentAttached(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerEquipmentAttachedEffect) effect;
        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    && permanent.getAttachedTo() != null
                    && permanent.getAttachedTo().equals(context.target().getId())) {
                count[0]++;
            }
        });
        accumulator.addPower(count[0] * boost.powerPerEquipment());
        accumulator.addToughness(count[0] * boost.toughnessPerEquipment());
    }

    @HandlesStaticEffect(value = BoostSelfByImprintedCreaturePTEffect.class, selfOnly = true)
    private void resolveBoostSelfByImprintedCreaturePT(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Card imprintedCard = context.source().getCard().getImprintedCard();
        if (imprintedCard == null) {
            return;
        }
        accumulator.addPower(imprintedCard.getPower());
        accumulator.addToughness(imprintedCard.getToughness());
    }

    @HandlesStaticEffect(value = BoostSelfPerOpponentPoisonCounterEffect.class, selfOnly = true)
    private void resolveBoostSelfPerOpponentPoisonCounter(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerOpponentPoisonCounterEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        int totalPoison = 0;
        for (UUID playerId : context.gameData().orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                totalPoison += context.gameData().playerPoisonCounters.getOrDefault(playerId, 0);
            }
        }
        accumulator.addPower(totalPoison * boost.powerPerCounter());
        accumulator.addToughness(totalPoison * boost.toughnessPerCounter());
    }

    @HandlesStaticEffect(value = EquippedConditionalEffect.class, selfOnly = true)
    private void resolveEquippedConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!isEquipped(context)) return;
        var equipped = (EquippedConditionalEffect) effect;
        CardEffect wrapped = equipped.wrapped();
        if (wrapped instanceof GrantKeywordEffect grant) {
            if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                accumulator.addKeyword(grant.keyword());
            }
        } else if (wrapped instanceof StaticBoostEffect boost) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
            accumulator.addKeywords(boost.grantedKeywords());
        } else if (wrapped instanceof ProtectionFromColorsEffect protection) {
            accumulator.addProtectionColors(protection.colors());
        }
    }

    private boolean isEquipped(StaticEffectContext context) {
        for (UUID playerId : context.gameData().orderedPlayerIds) {
            List<Permanent> bf = context.gameData().playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent permanent : bf) {
                if (permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                        && permanent.getAttachedTo() != null
                        && permanent.getAttachedTo().equals(context.target().getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @HandlesStaticEffect(value = PowerToughnessEqualToControlledSubtypeCountEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToControlledSubtypeCount(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var pt = (PowerToughnessEqualToControlledSubtypeCountEffect) effect;
        int count = countControlledPermanents(context, p -> p.getCard().getSubtypes().contains(pt.subtype()));
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    @HandlesStaticEffect(value = PowerToughnessEqualToControlledLandCountEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToControlledLandCount(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        int count = countControlledPermanents(context,
                p -> p.getCard().getType() == CardType.LAND || p.getCard().getAdditionalTypes().contains(CardType.LAND));
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    @HandlesStaticEffect(value = PowerToughnessEqualToControlledPermanentCountEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToControlledPermanentCount(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var pt = (PowerToughnessEqualToControlledPermanentCountEffect) effect;
        int count = countControlledPermanents(context,
                p -> gameQueryService.matchesPermanentPredicate(context.gameData(), p, pt.filter()));
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    @HandlesStaticEffect(value = PowerToughnessEqualToControlledCreatureCountEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToControlledCreatureCount(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
        int count = countControlledPermanents(context, p -> isEffectivelyCreature(p, hasAnimateArtifacts));
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    @HandlesStaticEffect(value = PowerToughnessEqualToCardsInHandEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToCardsInHand(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        List<Card> hand = context.gameData().playerHands.get(controllerId);
        int count = hand != null ? hand.size() : 0;
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    private int countControlledPermanents(StaticEffectContext context, Predicate<Permanent> filter) {
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return 0;

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (filter.test(permanent)) count++;
        }
        return count;
    }

    private UUID findControllerId(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(permanent)) {
                return playerId;
            }
        }
        return null;
    }

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST,
            CardSubtype.MOUNTAIN,
            CardSubtype.ISLAND,
            CardSubtype.PLAINS,
            CardSubtype.SWAMP,
            CardSubtype.AURA,
            CardSubtype.EQUIPMENT,
            CardSubtype.AJANI,
            CardSubtype.KOTH
    );

    private boolean matchesStaticFilter(Permanent target, PermanentPredicate filter) {
        if (filter == null) return true;
        if (filter instanceof PermanentColorInPredicate p) {
            if (target.isColorOverridden()) {
                return target.getGrantedColors().stream().anyMatch(p.colors()::contains);
            }
            CardColor effectiveColor = target.getEffectiveColor();
            return (effectiveColor != null && p.colors().contains(effectiveColor))
                    || target.getGrantedColors().stream().anyMatch(p.colors()::contains);
        }
        if (filter instanceof PermanentHasSubtypePredicate p)
            return target.getCard().getSubtypes().contains(p.subtype())
                    || (isCreatureSubtype(p.subtype()) && target.hasKeyword(Keyword.CHANGELING));
        if (filter instanceof PermanentHasAnySubtypePredicate p)
            return target.getCard().getSubtypes().stream().anyMatch(p.subtypes()::contains)
                    || (p.subtypes().stream().anyMatch(StaticEffectResolutionService::isCreatureSubtype)
                    && target.hasKeyword(Keyword.CHANGELING));
        if (filter instanceof PermanentHasKeywordPredicate p)
            return target.hasKeyword(p.keyword());
        if (filter instanceof PermanentIsCreaturePredicate)
            return target.getCard().getType() == CardType.CREATURE
                    || target.getCard().getAdditionalTypes().contains(CardType.CREATURE)
                    || target.isAnimatedUntilEndOfTurn()
                    || target.getAwakeningCounters() > 0;
        if (filter instanceof PermanentIsArtifactPredicate)
            return gameQueryService.isArtifact(target);
        if (filter instanceof PermanentIsLandPredicate)
            return target.getCard().getType() == CardType.LAND
                    || target.getCard().getAdditionalTypes().contains(CardType.LAND);
        if (filter instanceof PermanentIsPlaneswalkerPredicate)
            return target.getCard().getType() == CardType.PLANESWALKER
                    || target.getCard().getAdditionalTypes().contains(CardType.PLANESWALKER);
        if (filter instanceof PermanentNotPredicate p)
            return !matchesStaticFilter(target, p.predicate());
        if (filter instanceof PermanentAllOfPredicate p)
            return p.predicates().stream().allMatch(inner -> matchesStaticFilter(target, inner));
        if (filter instanceof PermanentAnyOfPredicate p)
            return p.predicates().stream().anyMatch(inner -> matchesStaticFilter(target, inner));
        if (filter instanceof PermanentTruePredicate) return true;
        throw new IllegalArgumentException("Unsupported static filter predicate: " + filter.getClass().getSimpleName());
    }

    private static boolean isCreatureSubtype(CardSubtype subtype) {
        return !NON_CREATURE_SUBTYPES.contains(subtype);
    }

    private int countCardsInAllGraveyards(GameData gameData, CardPredicate filter) {
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (gameQueryService.matchesCardPredicate(card, filter, null)) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean hasAnimateArtifactEffect(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                for (CardEffect e : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (e instanceof AnimateNoncreatureArtifactsEffect) return true;
                }
            }
        }
        return false;
    }
}

