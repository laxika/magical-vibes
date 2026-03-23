package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
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
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerControlsColorConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEquipByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BlockedByMinCreaturesConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerControlledCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerMatchingLandNameEffect;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfBySlimeCountersOnLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerAttachmentEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerEquipmentAttachedEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerEnchantmentOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfByImprintedCreaturePTEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOtherControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerControlsPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOpponentPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOpponentPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerGraveyardCardThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SelfHasKeywordConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TopCardOfLibraryColorConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessStaticEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.EquippedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentPoisonedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControllerLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledPermanentCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledLandCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInControllerGraveyardEffect;
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
    private final StaticEffectHandlerRegistry staticEffectHandlerRegistry;

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
                if (permanent.isAttached()
                        && permanent.getAttachedTo().equals(target.getId())
                        && matchesStaticFilter(permanent, grant.filter())) {
                    accumulator.addPower(permanent.getCard().getManaValue());
                }
            });
        }
    }

    @HandlesStaticEffect(EnchantedCreatureSubtypeConditionalEffect.class)
    private void resolveEnchantedCreatureSubtypeConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (EnchantedCreatureSubtypeConditionalEffect) effect;
        if (!context.source().isAttached()
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }
        boolean hasSubtype = permanentHasSubtype(context.target(), conditional.subtype());
        CardEffect activeEffect = hasSubtype ? conditional.ifMatch() : conditional.ifNotMatch();
        StaticEffectHandler handler = staticEffectHandlerRegistry.getHandler(activeEffect);
        if (handler != null) {
            handler.apply(context, activeEffect, accumulator);
        }
    }

    /**
     * Checks whether a permanent has a given subtype without triggering {@code computeStaticBonus}
     * (which would cause infinite recursion when called from within a static effect handler).
     * Checks base subtypes, transient subtypes, granted subtypes, and the intrinsic Changeling keyword.
     */
    private static boolean permanentHasSubtype(Permanent permanent, CardSubtype subtype) {
        return permanent.getCard().getSubtypes().contains(subtype)
                || permanent.getTransientSubtypes().contains(subtype)
                || permanent.getGrantedSubtypes().contains(subtype)
                || permanent.hasKeyword(Keyword.CHANGELING);
    }

    @HandlesStaticEffect(BoostCreaturePerCardsInAllGraveyardsEffect.class)
    private void resolveBoostCreaturePerCardsInAllGraveyards(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturePerCardsInAllGraveyardsEffect) effect;
        if (!matchesCreatureScope(context, boost.scope(), null)) {
            return;
        }

        int count = countCardsInAllGraveyards(context.gameData(), boost.filter());
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    @HandlesStaticEffect(BoostCreaturePerCardsInControllerGraveyardEffect.class)
    private void resolveBoostCreaturePerCardsInControllerGraveyard(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturePerCardsInControllerGraveyardEffect) effect;
        if (!matchesCreatureScope(context, boost.scope(), null)) {
            return;
        }

        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        List<Card> graveyard = context.gameData().playerGraveyards.get(controllerId);
        int count = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (gameQueryService.matchesCardPredicate(card, boost.filter(), null)) {
                    count++;
                }
            }
        }
        accumulator.addPower(count * boost.powerPerCard());
        accumulator.addToughness(count * boost.toughnessPerCard());
    }

    @HandlesStaticEffect(BoostCreaturePerMatchingLandNameEffect.class)
    private void resolveBoostCreaturePerMatchingLandName(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturePerMatchingLandNameEffect) effect;
        if (!matchesCreatureScope(context, boost.scope(), null)) {
            return;
        }

        Card imprintedCard = context.source().getCard().getImprintedCard();
        if (imprintedCard == null) {
            return;
        }

        String imprintedName = imprintedCard.getName();
        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().hasType(CardType.LAND)) {
                if (imprintedName.equals(permanent.getCard().getName())) {
                    count[0]++;
                }
            }
        });

        accumulator.addPower(count[0] * boost.powerPerMatch());
        accumulator.addToughness(count[0] * boost.toughnessPerMatch());
    }

    @HandlesStaticEffect(BoostCreaturePerControlledSubtypeEffect.class)
    private void resolveBoostCreaturePerControlledSubtype(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturePerControlledSubtypeEffect) effect;
        if (!matchesCreatureScope(context, boost.scope(), null)) {
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

    @HandlesStaticEffect(BoostCreaturePerControlledCardTypeEffect.class)
    private void resolveBoostCreaturePerControlledCardType(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturePerControlledCardTypeEffect) effect;
        if (!matchesCreatureScope(context, boost.scope(), null)) {
            return;
        }

        int count = countControlledPermanents(context, p -> p.getCard().hasType(boost.cardType()));

        accumulator.addPower(count * boost.powerPerMatch());
        accumulator.addToughness(count * boost.toughnessPerMatch());
    }

    @HandlesStaticEffect(ProtectionFromColorsEffect.class)
    private void resolveProtectionFromColors(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var protection = (ProtectionFromColorsEffect) effect;
        if (context.source().isAttached()
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addProtectionColors(protection.colors());
        }
    }

    @HandlesStaticEffect(GrantKeywordEffect.class)
    private void resolveGrantKeyword(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantKeywordEffect) effect;
        boolean scopeMatch = switch (grant.scope()) {
            case OWN_PERMANENTS -> context.targetOnSameBattlefield()
                    && matchesStaticFilter(context.target(), grant.filter());
            default -> matchesCreatureScope(context, grant.scope(), grant.filter());
        };
        if (scopeMatch) {
            accumulator.addKeywords(grant.keywords());
        }
    }

    @HandlesStaticEffect(RemoveKeywordEffect.class)
    private void resolveRemoveKeyword(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var remove = (RemoveKeywordEffect) effect;
        if (matchesCreatureScope(context, remove.scope(), remove.filter())) {
            accumulator.removeKeyword(remove.keyword());
        }
    }

    @HandlesStaticEffect(SetBasePowerToughnessStaticEffect.class)
    private void resolveSetBasePowerToughnessStatic(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var setPT = (SetBasePowerToughnessStaticEffect) effect;
        if (matchesCreatureScope(context, setPT.scope(), null)) {
            accumulator.setBasePTOverride(setPT.power(), setPT.toughness());
        }
    }

    @HandlesStaticEffect(LosesAllAbilitiesEffect.class)
    private void resolveLosesAllAbilities(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var loses = (LosesAllAbilitiesEffect) effect;
        if (matchesCreatureScope(context, loses.scope(), null)) {
            accumulator.setLosesAllAbilities(true);
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

    @HandlesStaticEffect(GrantChosenSubtypeToOwnCreaturesEffect.class)
    private void resolveGrantChosenSubtypeToOwnCreatures(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype == null) return;
        if (matchesCreatureScope(context, GrantScope.OWN_CREATURES, null)) {
            accumulator.addGrantedSubtype(chosenSubtype);
        }
    }

    @HandlesStaticEffect(EnchantedPermanentBecomesTypeEffect.class)
    private void resolveEnchantedPermanentBecomesType(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var becomesType = (EnchantedPermanentBecomesTypeEffect) effect;
        if (context.source().isAttached()
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addGrantedSubtype(becomesType.subtype());
            accumulator.setSubtypeOverriding(true);
            if (becomesType.isBasicLandSubtype()) {
                accumulator.setLandSubtypeOverriding(true);
            }
        }
    }

    @HandlesStaticEffect(EnchantedPermanentBecomesChosenTypeEffect.class)
    private void resolveEnchantedPermanentBecomesChosenType(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype == null) return;
        if (context.source().isAttached()
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addGrantedSubtype(chosenSubtype);
            accumulator.setSubtypeOverriding(true);
            accumulator.setLandSubtypeOverriding(true);
        }
    }

    @HandlesStaticEffect(GrantCardTypeEffect.class)
    private void resolveGrantCardType(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantCardTypeEffect) effect;
        if (matchesCreatureScope(context, grant.scope(), null)) {
            accumulator.addGrantedCardType(grant.cardType());
        }
    }

    @HandlesStaticEffect(GrantSupertypeToEnchantedPermanentEffect.class)
    private void resolveGrantSupertypeToEnchantedPermanent(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantSupertypeToEnchantedPermanentEffect) effect;
        if (context.source().isAttached()
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addGrantedSupertype(grant.supertype());
        }
    }

    @HandlesStaticEffect(GrantEffectEffect.class)
    private void resolveGrantEffectEffect(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantEffectEffect) effect;
        if (matchesCreatureScope(context, grant.scope(), grant.filter())) {
            accumulator.addGrantedEffect(grant.effect());
        }
    }

    @HandlesStaticEffect(value = GrantEffectEffect.class, selfOnly = true)
    private void resolveGrantEffectEffectSelf(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantEffectEffect) effect;
        if ((grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.ALL_OWN_CREATURES)
                && matchesStaticFilter(context.target(), grant.filter())) {
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
            colorMatch = target.getTransientColors().contains(chosenColor);
        } else {
            CardColor effectiveColor = target.getEffectiveColor();
            colorMatch = (effectiveColor != null && effectiveColor == chosenColor)
                    || target.getTransientColors().contains(chosenColor);
        }
        if (colorMatch) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
        }
    }

    @HandlesStaticEffect(StaticBoostEffect.class)
    private void resolveStaticBoost(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (StaticBoostEffect) effect;
        if (matchesCreatureScope(context, boost.scope(), boost.filter())) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
            accumulator.addKeywords(boost.grantedKeywords());
        }
    }

    @HandlesStaticEffect(value = StaticBoostEffect.class, selfOnly = true)
    private void resolveStaticBoostSelf(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (StaticBoostEffect) effect;
        if ((boost.scope() == GrantScope.SELF || boost.scope() == GrantScope.ALL_OWN_CREATURES)
                && matchesStaticFilter(context.target(), boost.filter())) {
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
            accumulator.addActivatedAbility(grant.ability().withGrantSource(context.source().getId()));
        }
    }

    @HandlesStaticEffect(BoostBySharedCreatureTypeEffect.class)
    private void resolveBoostBySharedCreatureType(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Permanent target = context.target();
        GameData gameData = context.gameData();

        List<CardSubtype> targetTypes = new ArrayList<>(target.getCard().getSubtypes());
        targetTypes.addAll(target.getTransientSubtypes());
        boolean targetIsChangeling = target.hasKeyword(Keyword.CHANGELING);

        if (targetTypes.isEmpty() && !targetIsChangeling) return;

        boolean hasAnimateArtifacts = hasAnimateArtifactEffect(gameData);
        final int[] count = {0};

        gameData.forEachPermanent((playerId, other) -> {
            if (other == target) return;
            if (!isEffectivelyCreature(other, hasAnimateArtifacts)) return;

            List<CardSubtype> otherTypes = new ArrayList<>(other.getCard().getSubtypes());
            otherTypes.addAll(other.getTransientSubtypes());
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
                    accumulator.addKeywords(grant.keywords());
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
                    accumulator.addKeywords(grant.keywords());
                }
            }
        } else if (wrapped instanceof StaticBoostEffect boost && boost.scope() != GrantScope.SELF) {
            int artifactCount = countControlledPermanents(context, gameQueryService::isArtifact);
            if (artifactCount >= 3) {
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
                    accumulator.addActivatedAbility(grant.ability().withGrantSource(context.source().getId()));
                }
            }
        }
    }

    /**
     * Returns true if the target matches the given creature-centric scope.
     * Handles ENCHANTED_CREATURE, EQUIPPED_CREATURE, OWN_TAPPED_CREATURES, OWN_CREATURES, ALL_OWN_CREATURES, ALL_CREATURES.
     */
    private boolean matchesCreatureScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter) {
        if (scope == GrantScope.ENCHANTED_CREATURE || scope == GrantScope.EQUIPPED_CREATURE) {
            return context.source().isAttached()
                    && context.source().getAttachedTo().equals(context.target().getId())
                    && matchesStaticFilter(context.target(), filter);
        }
        if (scope == GrantScope.ENCHANTED_PLAYER_CREATURES) {
            if (!context.source().isAttached()) return false;
            UUID attachedPlayerId = context.source().getAttachedTo();
            List<Permanent> attachedPlayerBf = context.gameData().playerBattlefields.get(attachedPlayerId);
            if (attachedPlayerBf == null || !attachedPlayerBf.contains(context.target())) return false;
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            return isEffectivelyCreature(context.gameData(), context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context.target(), filter);
        }
        if (scope == GrantScope.OWN_TAPPED_CREATURES) {
            return context.targetOnSameBattlefield() && context.target().isTapped();
        }
        if (scope == GrantScope.OWN_CREATURES || scope == GrantScope.ALL_OWN_CREATURES
                || scope == GrantScope.OPPONENT_CREATURES || scope == GrantScope.ALL_CREATURES) {
            boolean ownCheck = scope == GrantScope.ALL_CREATURES
                    || (scope == GrantScope.OWN_CREATURES && context.targetOnSameBattlefield())
                    || (scope == GrantScope.ALL_OWN_CREATURES && context.targetOnSameBattlefield())
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
        if (permanent.getCard().hasType(CardType.CREATURE)) return true;
        if (permanent.isAnimatedUntilEndOfTurn()) return true;
        if (permanent.isAnimatedUntilNextTurn()) return true;
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

    @HandlesStaticEffect(value = PowerToughnessEqualToCardsInAllGraveyardsEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToCardsInAllGraveyards(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var ptEffect = (PowerToughnessEqualToCardsInAllGraveyardsEffect) effect;
        int count = countCardsInAllGraveyards(context.gameData(), ptEffect.filter());
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    @HandlesStaticEffect(value = PowerToughnessEqualToCardsInControllerGraveyardEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToCardsInControllerGraveyard(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var ptEffect = (PowerToughnessEqualToCardsInControllerGraveyardEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        List<Card> graveyard = context.gameData().playerGraveyards.get(controllerId);
        int count = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (gameQueryService.matchesCardPredicate(card, ptEffect.filter(), null)) {
                    count++;
                }
            }
        }
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
            if (permanent.getCard().hasType(CardType.ENCHANTMENT)) {
                count[0]++;
            }
        });
        accumulator.addPower(count[0] * boost.powerPerEnchantment());
        accumulator.addToughness(count[0] * boost.toughnessPerEnchantment());
    }

    @HandlesStaticEffect(value = BoostSelfPerControlledPermanentEffect.class, selfOnly = true)
    private void resolveBoostSelfPerControlledPermanent(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerControlledPermanentEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        int count = 0;
        for (Permanent permanent : battlefield) {
            // Pass null for gameData to avoid recursive computeStaticBonus calls —
            // type-checking predicates (isArtifact, isCreature) would otherwise trigger
            // computeStaticBonus on each permanent, causing infinite recursion when the
            // source itself is being evaluated. Natural type is sufficient here.
            if (gameQueryService.matchesPermanentPredicate(null, permanent, boost.filter())) {
                count++;
            }
        }
        accumulator.addPower(count * boost.powerPerPermanent());
        accumulator.addToughness(count * boost.toughnessPerPermanent());
    }

    @HandlesStaticEffect(value = BoostSelfPerCardsInControllerGraveyardEffect.class, selfOnly = true)
    private void resolveBoostSelfPerCardsInControllerGraveyard(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerCardsInControllerGraveyardEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        List<Card> graveyard = context.gameData().playerGraveyards.get(controllerId);
        int count = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (gameQueryService.matchesCardPredicate(card, boost.filter(), null)) {
                    count++;
                }
            }
        }
        accumulator.addPower(count * boost.powerPerCard());
        accumulator.addToughness(count * boost.toughnessPerCard());
    }

    @HandlesStaticEffect(value = BoostSelfPerOpponentPermanentEffect.class, selfOnly = true)
    private void resolveBoostSelfPerOpponentPermanent(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerOpponentPermanentEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;

        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (!playerId.equals(controllerId)
                    && gameQueryService.matchesPermanentPredicate(context.gameData(), permanent, boost.filter())) {
                count[0]++;
            }
        });
        accumulator.addPower(count[0] * boost.powerPerPermanent());
        accumulator.addToughness(count[0] * boost.toughnessPerPermanent());
    }

    @HandlesStaticEffect(value = AnyPlayerControlsPermanentConditionalEffect.class, selfOnly = true)
    private void resolveAnyPlayerControlsPermanentConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (AnyPlayerControlsPermanentConditionalEffect) effect;
        final boolean[] found = {false};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (!found[0] && gameQueryService.matchesPermanentPredicate(context.gameData(), permanent, conditional.filter())) {
                found[0] = true;
            }
        });
        if (found[0]) {
            CardEffect wrapped = conditional.wrapped();
            if (wrapped instanceof StaticBoostEffect boost) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            } else if (wrapped instanceof GrantKeywordEffect grant) {
                if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                    accumulator.addKeywords(grant.keywords());
                }
            } else if (wrapped instanceof ProtectionFromColorsEffect protection) {
                accumulator.addProtectionColors(protection.colors());
            }
        }
    }

    @HandlesStaticEffect(value = BoostSelfPerEquipmentAttachedEffect.class, selfOnly = true)
    private void resolveBoostSelfPerEquipmentAttached(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerEquipmentAttachedEffect) effect;
        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    && permanent.isAttached()
                    && permanent.getAttachedTo().equals(context.target().getId())) {
                count[0]++;
            }
        });
        accumulator.addPower(count[0] * boost.powerPerEquipment());
        accumulator.addToughness(count[0] * boost.toughnessPerEquipment());
    }

    @HandlesStaticEffect(value = BoostSelfPerAttachmentEffect.class, selfOnly = true)
    private void resolveBoostSelfPerAttachment(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerAttachmentEffect) effect;
        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttached() && permanent.getAttachedTo().equals(context.target().getId())) {
                boolean isAura = permanent.getCard().getSubtypes().contains(CardSubtype.AURA);
                boolean isEquipment = permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT);
                if ((boost.countAuras() && isAura) || (boost.countEquipment() && isEquipment)) {
                    count[0]++;
                }
            }
        });
        accumulator.addPower(count[0] * boost.power());
        accumulator.addToughness(count[0] * boost.toughness());
    }

    @HandlesStaticEffect(value = BoostSelfByImprintedCreaturePTEffect.class, selfOnly = true)
    private void resolveBoostSelfByImprintedCreaturePT(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Card imprintedCard = context.source().getCard().getImprintedCard();
        if (imprintedCard == null || imprintedCard.getPower() == null || imprintedCard.getToughness() == null) {
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

    @HandlesStaticEffect(value = BoostSelfBySlimeCountersOnLinkedPermanentEffect.class, selfOnly = true)
    private void resolveBoostSelfBySlimeCountersOnLinkedPermanent(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfBySlimeCountersOnLinkedPermanentEffect) effect;
        Permanent linked = gameQueryService.findPermanentById(context.gameData(), boost.linkedPermanentId());
        int slimeCount = (linked != null) ? linked.getSlimeCounters() : 0;
        accumulator.addPower(slimeCount);
        accumulator.addToughness(slimeCount);
    }

    @HandlesStaticEffect(value = BoostSelfPerControlledSubtypeEffect.class, selfOnly = true)
    private void resolveBoostSelfPerControlledSubtype(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerControlledSubtypeEffect) effect;
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

        accumulator.addPower(count * boost.powerPerPermanent());
        accumulator.addToughness(count * boost.toughnessPerPermanent());
    }

    @HandlesStaticEffect(value = BoostSelfPerOtherControlledSubtypeEffect.class, selfOnly = true)
    private void resolveBoostSelfPerOtherControlledSubtype(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerOtherControlledSubtypeEffect) effect;
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
            if (permanent.getId().equals(context.source().getId())) continue;
            if (permanent.getCard().getSubtypes().contains(boost.subtype())) {
                count++;
            }
        }

        accumulator.addPower(count * boost.powerPerPermanent());
        accumulator.addToughness(count * boost.toughnessPerPermanent());
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
        for (UUID playerId : context.gameData().orderedPlayerIds) {
            List<Permanent> bf = context.gameData().playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent permanent : bf) {
                if (permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(context.target().getId())) {
                    return true;
                }
            }
        }
        return false;
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
                p -> p.getCard().hasType(CardType.LAND));
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

    @HandlesStaticEffect(value = PowerToughnessEqualToControllerLifeTotalEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToControllerLifeTotal(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        int lifeTotal = context.gameData().playerLifeTotals.getOrDefault(controllerId, 0);
        accumulator.addPower(lifeTotal);
        accumulator.addToughness(lifeTotal);
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

    @HandlesStaticEffect(value = ControlsSubtypeConditionalEffect.class, selfOnly = true)
    private void resolveControlsSubtypeConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ControlsSubtypeConditionalEffect) effect;
        int subtypeCount = countControlledPermanents(context, p -> p.getCard().getSubtypes().contains(conditional.subtype()));
        if (subtypeCount > 0) {
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

    @HandlesStaticEffect(value = OpponentControlsSubtypeConditionalEffect.class, selfOnly = true)
    private void resolveOpponentControlsSubtypeConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (OpponentControlsSubtypeConditionalEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        boolean opponentHasSubtype = false;
        for (UUID playerId : context.gameData().orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                List<Permanent> battlefield = context.gameData().playerBattlefields.get(playerId);
                if (battlefield != null) {
                    for (Permanent p : battlefield) {
                        if (p.getCard().getSubtypes().contains(conditional.subtype())) {
                            opponentHasSubtype = true;
                            break;
                        }
                    }
                }
                if (opponentHasSubtype) break;
            }
        }
        if (opponentHasSubtype) {
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

    @HandlesStaticEffect(value = AnyPlayerControlsColorConditionalEffect.class, selfOnly = true)
    private void resolveAnyPlayerControlsColorConditional(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (AnyPlayerControlsColorConditionalEffect) effect;
        boolean anyPlayerHasColor = false;
        for (UUID playerId : context.gameData().orderedPlayerIds) {
            List<Permanent> battlefield = context.gameData().playerBattlefields.get(playerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getCard().getColor() == conditional.color()) {
                        anyPlayerHasColor = true;
                        break;
                    }
                }
            }
            if (anyPlayerHasColor) break;
        }
        if (anyPlayerHasColor) {
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
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return false;
        List<Card> deck = context.gameData().playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) return false;
        return deck.getFirst().getColors().contains(color);
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
                return target.getTransientColors().stream().anyMatch(p.colors()::contains);
            }
            CardColor effectiveColor = target.getEffectiveColor();
            return (effectiveColor != null && p.colors().contains(effectiveColor))
                    || target.getTransientColors().stream().anyMatch(p.colors()::contains);
        }
        if (filter instanceof PermanentHasSubtypePredicate p)
            return target.getCard().getSubtypes().contains(p.subtype())
                    || target.getTransientSubtypes().contains(p.subtype())
                    || target.getGrantedSubtypes().contains(p.subtype())
                    || (isCreatureSubtype(p.subtype()) && target.hasKeyword(Keyword.CHANGELING));
        if (filter instanceof PermanentHasAnySubtypePredicate p)
            return target.getCard().getSubtypes().stream().anyMatch(p.subtypes()::contains)
                    || target.getTransientSubtypes().stream().anyMatch(p.subtypes()::contains)
                    || target.getGrantedSubtypes().stream().anyMatch(p.subtypes()::contains)
                    || (p.subtypes().stream().anyMatch(StaticEffectResolutionService::isCreatureSubtype)
                    && target.hasKeyword(Keyword.CHANGELING));
        if (filter instanceof PermanentHasKeywordPredicate p)
            return target.hasKeyword(p.keyword());
        if (filter instanceof PermanentIsCreaturePredicate)
            return target.getCard().hasType(CardType.CREATURE)
                    || target.isAnimatedUntilEndOfTurn()
                    || target.isAnimatedUntilNextTurn()
                    || target.getAwakeningCounters() > 0;
        if (filter instanceof PermanentIsArtifactPredicate)
            return gameQueryService.isArtifact(target);
        if (filter instanceof PermanentIsLandPredicate)
            return target.getCard().hasType(CardType.LAND);
        if (filter instanceof PermanentIsPlaneswalkerPredicate)
            return target.getCard().hasType(CardType.PLANESWALKER);
        if (filter instanceof PermanentIsTokenPredicate)
            return target.getCard().isToken();
        if (filter instanceof PermanentIsHistoricPredicate)
            return gameQueryService.isArtifact(target)
                    || target.getCard().getSupertypes().contains(CardSupertype.LEGENDARY)
                    || target.getCard().getSubtypes().contains(CardSubtype.SAGA)
                    || target.getTransientSubtypes().contains(CardSubtype.SAGA);
        if (filter instanceof PermanentNotPredicate p)
            return !matchesStaticFilter(target, p.predicate());
        if (filter instanceof PermanentAllOfPredicate p)
            return p.predicates().stream().allMatch(inner -> matchesStaticFilter(target, inner));
        if (filter instanceof PermanentAnyOfPredicate p)
            return p.predicates().stream().anyMatch(inner -> matchesStaticFilter(target, inner));
        if (filter instanceof PermanentHasSupertypePredicate p)
            return target.getCard().getSupertypes().contains(p.supertype());
        if (filter instanceof PermanentIsAttackingPredicate)
            return target.isAttacking();
        if (filter instanceof PermanentTruePredicate) return true;
        if (filter instanceof PermanentPowerAtMostPredicate p)
            return target.getEffectivePower() <= p.maxPower();
        if (filter instanceof PermanentToughnessAtMostPredicate p)
            return target.getEffectiveToughness() <= p.maxToughness();
        if (filter instanceof PermanentPowerAtLeastPredicate p)
            return target.getEffectivePower() >= p.minPower();
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

