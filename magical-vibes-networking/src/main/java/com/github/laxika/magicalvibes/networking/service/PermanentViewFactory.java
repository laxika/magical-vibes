package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.networking.model.ActivatedAbilityView;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermanentViewFactory {

    private final CardViewFactory cardViewFactory;

    public PermanentView create(Permanent p, int bonusPower, int bonusToughness, Set<Keyword> bonusKeywords, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities) {
        return create(p, bonusPower, bonusToughness, bonusKeywords, animatedCreature, grantedActivatedAbilities, Set.of(), List.of(), false, false);
    }

    public PermanentView create(Permanent p, int bonusPower, int bonusToughness, Set<Keyword> bonusKeywords, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities, Set<CardColor> staticGrantedColors, List<CardSubtype> staticGrantedSubtypes) {
        return create(p, bonusPower, bonusToughness, bonusKeywords, animatedCreature, grantedActivatedAbilities, staticGrantedColors, staticGrantedSubtypes, false, false);
    }

    public PermanentView create(Permanent p, int bonusPower, int bonusToughness, Set<Keyword> bonusKeywords, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities, Set<CardColor> staticGrantedColors, List<CardSubtype> staticGrantedSubtypes, boolean colorOverriding, boolean subtypeOverriding) {
        return create(p, bonusPower, bonusToughness, bonusKeywords, animatedCreature, grantedActivatedAbilities, staticGrantedColors, staticGrantedSubtypes, Set.of(), colorOverriding, subtypeOverriding);
    }

    public PermanentView create(Permanent p, int bonusPower, int bonusToughness, Set<Keyword> bonusKeywords, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities, Set<CardColor> staticGrantedColors, List<CardSubtype> staticGrantedSubtypes, Set<CardType> staticGrantedCardTypes, boolean colorOverriding, boolean subtypeOverriding) {
        Set<Keyword> allKeywords = new HashSet<>(p.getGrantedKeywords());
        allKeywords.addAll(bonusKeywords);
        CardView cardView = cardViewFactory.create(p.getCard());
        cardView = applyTextReplacements(cardView, p);
        cardView = applyGrantedSubtypes(cardView, p);
        cardView = applyStaticGrantedSubtypes(cardView, staticGrantedSubtypes, subtypeOverriding);
        cardView = applyAwakeningCounterSubtype(cardView, p);
        cardView = applyGrantedCardTypes(cardView, p);
        cardView = applyPermanentAnimation(cardView, p);
        cardView = applyStaticGrantedCardTypes(cardView, staticGrantedCardTypes);
        cardView = applyGrantedActivatedAbilities(cardView, grantedActivatedAbilities);
        cardView = applyStaticGrantedColors(cardView, p, staticGrantedColors, colorOverriding);
        return new PermanentView(
                p.getId(), cardView,
                p.isTapped(), p.isAttacking(), p.isBlocking(),
                new ArrayList<>(p.getBlockingTargets()), p.isSummoningSick() && !p.hasKeyword(Keyword.HASTE) && !allKeywords.contains(Keyword.HASTE),
                p.getPowerModifier() + bonusPower,
                p.getToughnessModifier() + bonusToughness,
                allKeywords,
                p.getEffectivePower() + bonusPower,
                p.getEffectiveToughness() + bonusToughness,
                p.getAttachedTo(),
                p.getChosenColor(),
                p.getChosenName(),
                p.getRegenerationShield(),
                p.isCantBeBlocked(),
                animatedCreature || p.isPermanentlyAnimated(),
                p.getLoyaltyCounters(),
                p.getChargeCounters()
        );
    }

    private CardView applyGrantedSubtypes(CardView cardView, Permanent p) {
        if (p.getGrantedSubtypes().isEmpty()) {
            return cardView;
        }
        List<CardSubtype> mergedSubtypes = new ArrayList<>(cardView.subtypes());
        for (CardSubtype subtype : p.getGrantedSubtypes()) {
            if (!mergedSubtypes.contains(subtype)) {
                mergedSubtypes.add(subtype);
            }
        }
        return new CardView(
                cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), mergedSubtypes,
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount()
        );
    }

    private CardView applyAwakeningCounterSubtype(CardView cardView, Permanent p) {
        if (p.getAwakeningCounters() <= 0 || p.getCard().getType() == CardType.CREATURE) {
            return cardView;
        }
        List<CardSubtype> mergedSubtypes = new ArrayList<>(cardView.subtypes());
        if (!mergedSubtypes.contains(CardSubtype.ELEMENTAL)) {
            mergedSubtypes.add(CardSubtype.ELEMENTAL);
        }
        return new CardView(
                cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), mergedSubtypes,
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), CardColor.GREEN, List.of(CardColor.GREEN), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount()
        );
    }

    private CardView applyPermanentAnimation(CardView cardView, Permanent p) {
        if (!p.isPermanentlyAnimated()) {
            return cardView;
        }
        Set<CardType> mergedTypes = new HashSet<>(cardView.additionalTypes());
        mergedTypes.add(CardType.CREATURE);
        return new CardView(
                cardView.name(), cardView.type(), mergedTypes, cardView.supertypes(), cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount()
        );
    }

    private CardView applyGrantedCardTypes(CardView cardView, Permanent p) {
        if (p.getGrantedCardTypes().isEmpty()) {
            return cardView;
        }
        Set<CardType> mergedTypes = new HashSet<>(cardView.additionalTypes());
        mergedTypes.addAll(p.getGrantedCardTypes());
        return new CardView(
                cardView.name(), cardView.type(), mergedTypes, cardView.supertypes(), cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount()
        );
    }

    private CardView applyStaticGrantedCardTypes(CardView cardView, Set<CardType> staticGrantedCardTypes) {
        if (staticGrantedCardTypes.isEmpty()) {
            return cardView;
        }
        Set<CardType> mergedTypes = new HashSet<>(cardView.additionalTypes());
        mergedTypes.addAll(staticGrantedCardTypes);
        return new CardView(
                cardView.name(), cardView.type(), mergedTypes, cardView.supertypes(), cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount()
        );
    }

    private CardView applyGrantedActivatedAbilities(CardView cardView, List<ActivatedAbility> grantedAbilities) {
        if (grantedAbilities.isEmpty()) {
            return cardView;
        }
        List<ActivatedAbilityView> mergedAbilities = new ArrayList<>(cardView.activatedAbilities());
        for (ActivatedAbility ability : grantedAbilities) {
            mergedAbilities.add(cardViewFactory.createAbilityView(ability));
        }
        boolean hasTapAbility = cardView.hasTapAbility()
                || grantedAbilities.stream().anyMatch(ActivatedAbility::isRequiresTap);
        return new CardView(
                cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), hasTapAbility, cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), mergedAbilities, cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount()
        );
    }

    private CardView applyTextReplacements(CardView cardView, Permanent p) {
        if (p.getTextReplacements().isEmpty() || cardView.cardText() == null) {
            return cardView;
        }
        String modifiedText = cardView.cardText();
        for (TextReplacement rep : p.getTextReplacements()) {
            modifiedText = modifiedText.replace(rep.fromWord(), rep.toWord());
        }
        return new CardView(
                cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), cardView.subtypes(),
                modifiedText, cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount()
        );
    }

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST,
            CardSubtype.MOUNTAIN,
            CardSubtype.ISLAND,
            CardSubtype.PLAINS,
            CardSubtype.SWAMP,
            CardSubtype.AURA,
            CardSubtype.EQUIPMENT,
            CardSubtype.AJANI
    );

    private CardView applyStaticGrantedSubtypes(CardView cardView, List<CardSubtype> staticGrantedSubtypes, boolean subtypeOverriding) {
        if (staticGrantedSubtypes.isEmpty()) {
            return cardView;
        }
        List<CardSubtype> mergedSubtypes;
        if (subtypeOverriding) {
            // Keep only non-creature subtypes from the original, then add the granted ones
            mergedSubtypes = new ArrayList<>();
            for (CardSubtype subtype : cardView.subtypes()) {
                if (NON_CREATURE_SUBTYPES.contains(subtype)) {
                    mergedSubtypes.add(subtype);
                }
            }
        } else {
            mergedSubtypes = new ArrayList<>(cardView.subtypes());
        }
        for (CardSubtype subtype : staticGrantedSubtypes) {
            if (!mergedSubtypes.contains(subtype)) {
                mergedSubtypes.add(subtype);
            }
        }
        return new CardView(
                cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), mergedSubtypes,
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount()
        );
    }

    private CardView applyStaticGrantedColors(CardView cardView, Permanent p, Set<CardColor> staticGrantedColors, boolean colorOverriding) {
        // Determine the effective color: static grants add to permanent's colors
        // If the permanent already has a color override (from non-static effects), use that
        if (staticGrantedColors.isEmpty() && !p.isColorOverridden()) {
            return cardView;
        }
        CardColor effectiveColor = cardView.color();
        List<CardColor> effectiveColors = new ArrayList<>(cardView.colors());
        if (p.isColorOverridden() && !p.getGrantedColors().isEmpty()) {
            effectiveColor = p.getGrantedColors().iterator().next();
            effectiveColors = new ArrayList<>(p.getGrantedColors());
        }
        if (!staticGrantedColors.isEmpty()) {
            // Static color grants take precedence for display if the card has no color
            // For multicolor, we display the first granted color as primary
            effectiveColor = staticGrantedColors.iterator().next();
            effectiveColors = new ArrayList<>(staticGrantedColors);
        }
        if (effectiveColor == cardView.color() && effectiveColors.equals(cardView.colors())) {
            return cardView;
        }
        return new CardView(
                cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), effectiveColor, List.copyOf(effectiveColors), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount()
        );
    }
}
