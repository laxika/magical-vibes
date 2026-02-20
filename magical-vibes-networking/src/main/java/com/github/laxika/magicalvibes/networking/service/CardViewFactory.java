package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetLandAndDamageControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.HeadGamesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.networking.model.ActivatedAbilityView;
import com.github.laxika.magicalvibes.networking.model.CardView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CardViewFactory {

    public CardView create(Card card) {
        boolean hasTapAbility = !card.getEffects(EffectSlot.ON_TAP).isEmpty()
                || card.getActivatedAbilities().stream().anyMatch(ActivatedAbility::isRequiresTap);

        List<ActivatedAbilityView> abilityViews = card.getActivatedAbilities().stream()
                .map(this::createAbilityView)
                .toList();

        List<String> spellAllowedTargetTypes = computeSpellAllowedTargetTypes(card);
        List<String> spellAllowedTargetSubtypes = computeSpellAllowedTargetSubtypes(card);
        boolean requiresAttackingTarget = computeRequiresAttackingTarget(card);
        boolean targetsPlayer = computeSpellTargetsPlayer(card);

        return new CardView(
                card.getName(),
                card.getType(),
                card.getAdditionalTypes(),
                card.getSupertypes(),
                card.getSubtypes(),
                card.getCardText(),
                card.getManaCost(),
                card.getPower(),
                card.getToughness(),
                card.getKeywords(),
                hasTapAbility,
                card.getSetCode(),
                card.getCollectorNumber(),
                card.getColor(),
                card.isNeedsTarget(),
                card.isNeedsSpellTarget(),
                targetsPlayer,
                requiresAttackingTarget,
                spellAllowedTargetTypes,
                spellAllowedTargetSubtypes,
                abilityViews,
                card.getLoyalty(),
                card.getMinTargets(),
                card.getMaxTargets(),
                card.getKeywords().contains(Keyword.CONVOKE)
        );
    }

    public ActivatedAbilityView createAbilityView(ActivatedAbility ability) {
        boolean targetsPlayer = ability.getEffects().stream()
                .anyMatch(e -> e instanceof MillTargetPlayerEffect || e instanceof RevealTopCardOfLibraryEffect
                        || e instanceof DealDamageToAnyTargetEffect || e instanceof DealDamageToTargetPlayerEffect);

        List<String> allowedTargetTypes = new ArrayList<>();
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof TapTargetPermanentEffect tapEffect) {
                tapEffect.allowedTypes().forEach(t -> allowedTargetTypes.add(t.getDisplayName()));
            }
        }

        Set<String> allowedTargetColors = new LinkedHashSet<>();
        if (ability.getTargetFilter() instanceof PermanentPredicateTargetFilter predicateFilter) {
            collectAllowedColors(predicateFilter.predicate(), allowedTargetColors);
        }

        boolean targetsBlockingThis = ability.getEffects().stream()
                .anyMatch(e -> e instanceof DestroyCreatureBlockingThisEffect);

        return new ActivatedAbilityView(
                ability.getDescription(),
                ability.isRequiresTap(),
                ability.isNeedsTarget(),
                ability.isNeedsSpellTarget(),
                targetsPlayer,
                allowedTargetTypes,
                List.copyOf(allowedTargetColors),
                ability.getManaCost(),
                ability.getLoyaltyCost(),
                targetsBlockingThis
        );
    }

    private boolean computeRequiresAttackingTarget(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof PutTargetOnBottomOfLibraryEffect) {
                return true;
            }
        }
        return false;
    }

    private boolean computeSpellTargetsPlayer(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ChooseCardsFromTargetHandToTopOfLibraryEffect
                    || effect instanceof DoubleTargetPlayerLifeEffect
                    || effect instanceof ExtraTurnEffect
                    || effect instanceof HeadGamesEffect
                    || effect instanceof LookAtHandEffect
                    || effect instanceof MillTargetPlayerEffect
                    || effect instanceof ReturnArtifactsTargetPlayerOwnsToHandEffect
                    || effect instanceof SacrificeCreatureEffect
                    || effect instanceof ShuffleGraveyardIntoLibraryEffect
                    || effect instanceof DealOrderedDamageToAnyTargetsEffect) {
                return true;
            }
        }
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof TargetPlayerLosesLifeAndControllerGainsLifeEffect) {
                return true;
            }
        }
        return false;
    }

    private List<String> computeSpellAllowedTargetTypes(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof GainControlOfTargetAuraEffect) {
                return List.of(CardType.ENCHANTMENT.getDisplayName());
            }
            if (effect instanceof ReturnTargetCreatureToHandEffect) {
                return List.of(CardType.CREATURE.getDisplayName());
            }
            if (effect instanceof ReturnTargetPermanentToHandEffect) {
                return List.of(
                        CardType.CREATURE.getDisplayName(),
                        CardType.ENCHANTMENT.getDisplayName(),
                        CardType.ARTIFACT.getDisplayName(),
                        CardType.LAND.getDisplayName()
                );
            }
            if (effect instanceof DestroyTargetPermanentEffect destroy) {
                return destroy.targetTypes().stream()
                        .map(CardType::getDisplayName)
                        .toList();
            }
            if (effect instanceof DestroyTargetLandAndDamageControllerEffect) {
                return List.of(CardType.LAND.getDisplayName());
            }
        }
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof DestroyTargetPermanentEffect destroy) {
                return destroy.targetTypes().stream()
                        .map(CardType::getDisplayName)
                        .toList();
            }
        }
        return List.of();
    }

    private List<String> computeSpellAllowedTargetSubtypes(Card card) {
        TargetFilter filter = card.getTargetFilter();
        if (filter instanceof PermanentPredicateTargetFilter f) {
            Set<String> subtypeNames = new LinkedHashSet<>();
            collectAllowedSubtypes(f.predicate(), subtypeNames);
            return List.copyOf(subtypeNames);
        }
        return List.of();
    }

    private void collectAllowedColors(PermanentPredicate predicate, Set<String> out) {
        if (predicate instanceof PermanentColorInPredicate colorInPredicate) {
            colorInPredicate.colors().forEach(c -> out.add(c.name()));
            return;
        }
        if (predicate instanceof PermanentAnyOfPredicate anyOfPredicate) {
            anyOfPredicate.predicates().forEach(p -> collectAllowedColors(p, out));
            return;
        }
        if (predicate instanceof PermanentAllOfPredicate allOfPredicate) {
            allOfPredicate.predicates().forEach(p -> collectAllowedColors(p, out));
        }
    }

    private void collectAllowedSubtypes(PermanentPredicate predicate, Set<String> out) {
        if (predicate instanceof PermanentHasSubtypePredicate subtypePredicate) {
            out.add(subtypePredicate.subtype().getDisplayName());
            return;
        }
        if (predicate instanceof PermanentHasAnySubtypePredicate anySubtypePredicate) {
            anySubtypePredicate.subtypes().stream()
                    .map(CardSubtype::getDisplayName)
                    .forEach(out::add);
            return;
        }
        if (predicate instanceof PermanentAnyOfPredicate anyOfPredicate) {
            anyOfPredicate.predicates().forEach(p -> collectAllowedSubtypes(p, out));
            return;
        }
        if (predicate instanceof PermanentAllOfPredicate allOfPredicate) {
            allOfPredicate.predicates().forEach(p -> collectAllowedSubtypes(p, out));
        }
    }
}
