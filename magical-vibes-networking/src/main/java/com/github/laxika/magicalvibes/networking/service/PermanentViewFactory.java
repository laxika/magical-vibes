package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
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
        return create(p, bonusPower, bonusToughness, bonusKeywords, animatedCreature, grantedActivatedAbilities, staticGrantedColors, staticGrantedSubtypes, Set.of(), colorOverriding, subtypeOverriding, false);
    }

    public PermanentView create(Permanent p, int bonusPower, int bonusToughness, Set<Keyword> bonusKeywords, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities, Set<CardColor> staticGrantedColors, List<CardSubtype> staticGrantedSubtypes, Set<CardType> staticGrantedCardTypes, boolean colorOverriding, boolean subtypeOverriding) {
        return create(p, bonusPower, bonusToughness, bonusKeywords, animatedCreature, grantedActivatedAbilities, staticGrantedColors, staticGrantedSubtypes, staticGrantedCardTypes, colorOverriding, subtypeOverriding, false);
    }

    public PermanentView create(Permanent p, int bonusPower, int bonusToughness, Set<Keyword> bonusKeywords, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities, Set<CardColor> staticGrantedColors, List<CardSubtype> staticGrantedSubtypes, Set<CardType> staticGrantedCardTypes, boolean colorOverriding, boolean subtypeOverriding, boolean landSubtypeOverriding) {
        return create(p, bonusPower, bonusToughness, bonusKeywords, animatedCreature, grantedActivatedAbilities, staticGrantedColors, staticGrantedSubtypes, staticGrantedCardTypes, colorOverriding, subtypeOverriding, landSubtypeOverriding, Set.of());
    }

    public PermanentView create(Permanent p, int bonusPower, int bonusToughness, Set<Keyword> bonusKeywords, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities, Set<CardColor> staticGrantedColors, List<CardSubtype> staticGrantedSubtypes, Set<CardType> staticGrantedCardTypes, boolean colorOverriding, boolean subtypeOverriding, boolean landSubtypeOverriding, Set<Keyword> staticRemovedKeywords) {
        return create(p, bonusPower, bonusToughness, bonusKeywords, animatedCreature, grantedActivatedAbilities, staticGrantedColors, staticGrantedSubtypes, staticGrantedCardTypes, colorOverriding, subtypeOverriding, landSubtypeOverriding, staticRemovedKeywords, false);
    }

    public PermanentView create(Permanent p, int bonusPower, int bonusToughness, Set<Keyword> bonusKeywords, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities, Set<CardColor> staticGrantedColors, List<CardSubtype> staticGrantedSubtypes, Set<CardType> staticGrantedCardTypes, boolean colorOverriding, boolean subtypeOverriding, boolean landSubtypeOverriding, Set<Keyword> staticRemovedKeywords, boolean losesAllAbilities) {
        return create(p, bonusPower, bonusToughness, bonusKeywords, animatedCreature, grantedActivatedAbilities, staticGrantedColors, staticGrantedSubtypes, staticGrantedCardTypes, colorOverriding, subtypeOverriding, landSubtypeOverriding, staticRemovedKeywords, losesAllAbilities, Set.of());
    }

    public PermanentView create(Permanent p, int bonusPower, int bonusToughness, Set<Keyword> bonusKeywords, boolean animatedCreature, List<ActivatedAbility> grantedActivatedAbilities, Set<CardColor> staticGrantedColors, List<CardSubtype> staticGrantedSubtypes, Set<CardType> staticGrantedCardTypes, boolean colorOverriding, boolean subtypeOverriding, boolean landSubtypeOverriding, Set<Keyword> staticRemovedKeywords, boolean losesAllAbilities, Set<CardSupertype> staticGrantedSupertypes) {
        Set<Keyword> allKeywords = new HashSet<>(p.getGrantedKeywords());
        allKeywords.addAll(p.getUntilNextTurnKeywords());
        allKeywords.addAll(bonusKeywords);
        Set<Keyword> allRemovedKeywords = new HashSet<>(p.getRemovedKeywords());
        allRemovedKeywords.addAll(staticRemovedKeywords);
        // When creature loses all abilities, add card's own keywords to removed set
        if (losesAllAbilities) {
            allRemovedKeywords.addAll(p.getCard().getKeywords());
        }
        allKeywords.removeAll(allRemovedKeywords);
        CardView cardView = cardViewFactory.create(p.getCard());
        cardView = applyTextReplacements(cardView, p);
        cardView = applyGrantedSubtypes(cardView, p);
        cardView = applyStaticGrantedSubtypes(cardView, staticGrantedSubtypes, subtypeOverriding, landSubtypeOverriding);
        cardView = applyAwakeningCounterSubtype(cardView, p);
        cardView = applyGrantedCardTypes(cardView, p);
        cardView = applyPermanentAnimation(cardView, p);
        cardView = applyStaticGrantedCardTypes(cardView, staticGrantedCardTypes);
        cardView = applyStaticGrantedSupertypes(cardView, staticGrantedSupertypes);
        cardView = applyGrantedActivatedAbilities(cardView, grantedActivatedAbilities);
        cardView = applyStaticGrantedColors(cardView, p, staticGrantedColors, colorOverriding);
        // When creature loses all abilities, strip its own activated abilities from the view
        if (losesAllAbilities) {
            cardView = stripCardActivatedAbilities(cardView);
        }
        return new PermanentView(
                p.getId(), cardView,
                p.isTapped(), p.isAttacking(), p.isBlocking(),
                new ArrayList<>(p.getBlockingTargets()), p.isSummoningSick() && !p.hasKeyword(Keyword.HASTE) && !allKeywords.contains(Keyword.HASTE),
                p.getPowerModifier() + bonusPower,
                p.getToughnessModifier() + bonusToughness,
                allKeywords,
                allRemovedKeywords,
                p.getEffectivePower() + bonusPower,
                p.getEffectiveToughness() + bonusToughness,
                p.getAttachedTo(),
                p.getChosenColor(),
                p.getChosenName(),
                p.getRegenerationShield(),
                p.isCantBeBlocked(),
                animatedCreature || p.isAnimatedUntilNextTurn() || p.isPermanentlyAnimated(),
                p.getLoyaltyCounters(),
                p.getChargeCounters(),
                p.getHatchlingCounters(),
                p.getPhylacteryCounters(),
                p.getSlimeCounters(),
                p.getStudyCounters(),
                p.getWishCounters(),
                p.getLoreCounters(),
                p.getAimCounters(),
                p.getAttackTarget(),
                p.getMarkedDamage(),
                p.isTransformed()
        );
    }

    private CardView applyGrantedSubtypes(CardView cardView, Permanent p) {
        if (p.getTransientSubtypes().isEmpty() && p.getGrantedSubtypes().isEmpty()
                && p.getUntilNextTurnSubtypes().isEmpty()) {
            return cardView;
        }
        List<CardSubtype> mergedSubtypes = new ArrayList<>(cardView.subtypes());
        for (CardSubtype subtype : p.getTransientSubtypes()) {
            if (!mergedSubtypes.contains(subtype)) {
                mergedSubtypes.add(subtype);
            }
        }
        for (CardSubtype subtype : p.getGrantedSubtypes()) {
            if (!mergedSubtypes.contains(subtype)) {
                mergedSubtypes.add(subtype);
            }
        }
        for (CardSubtype subtype : p.getUntilNextTurnSubtypes()) {
            if (!mergedSubtypes.contains(subtype)) {
                mergedSubtypes.add(subtype);
            }
        }
        return new CardView(
                cardView.id(), cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), mergedSubtypes,
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount(),
                cardView.token(),
                cardView.watermark(),
                cardView.hasAlternateCastingCost(),
                cardView.alternateCostLifePayment(),
                cardView.alternateCostSacrificeCount(),
                cardView.alternateCostTapCount(),
                cardView.alternateCostManaCost(),
                cardView.graveyardActivatedAbilities(),
                cardView.transformable(),
                cardView.kickerCost()
        );
    }

    private CardView applyAwakeningCounterSubtype(CardView cardView, Permanent p) {
        if (p.getAwakeningCounters() <= 0 || p.getCard().hasType(CardType.CREATURE)) {
            return cardView;
        }
        List<CardSubtype> mergedSubtypes = new ArrayList<>(cardView.subtypes());
        if (!mergedSubtypes.contains(CardSubtype.ELEMENTAL)) {
            mergedSubtypes.add(CardSubtype.ELEMENTAL);
        }
        return new CardView(
                cardView.id(), cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), mergedSubtypes,
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), CardColor.GREEN, List.of(CardColor.GREEN), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount(),
                cardView.token(),
                cardView.watermark(),
                cardView.hasAlternateCastingCost(),
                cardView.alternateCostLifePayment(),
                cardView.alternateCostSacrificeCount(),
                cardView.alternateCostTapCount(),
                cardView.alternateCostManaCost(),
                cardView.graveyardActivatedAbilities(),
                cardView.transformable(),
                cardView.kickerCost()
        );
    }

    private CardView applyPermanentAnimation(CardView cardView, Permanent p) {
        if (!p.isPermanentlyAnimated()) {
            return cardView;
        }
        Set<CardType> mergedTypes = new HashSet<>(cardView.additionalTypes());
        mergedTypes.add(CardType.CREATURE);
        return new CardView(
                cardView.id(), cardView.name(), cardView.type(), mergedTypes, cardView.supertypes(), cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount(),
                cardView.token(),
                cardView.watermark(),
                cardView.hasAlternateCastingCost(),
                cardView.alternateCostLifePayment(),
                cardView.alternateCostSacrificeCount(),
                cardView.alternateCostTapCount(),
                cardView.alternateCostManaCost(),
                cardView.graveyardActivatedAbilities(),
                cardView.transformable(),
                cardView.kickerCost()
        );
    }

    private CardView applyGrantedCardTypes(CardView cardView, Permanent p) {
        if (p.getGrantedCardTypes().isEmpty()) {
            return cardView;
        }
        Set<CardType> mergedTypes = new HashSet<>(cardView.additionalTypes());
        mergedTypes.addAll(p.getGrantedCardTypes());
        return new CardView(
                cardView.id(), cardView.name(), cardView.type(), mergedTypes, cardView.supertypes(), cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount(),
                cardView.token(),
                cardView.watermark(),
                cardView.hasAlternateCastingCost(),
                cardView.alternateCostLifePayment(),
                cardView.alternateCostSacrificeCount(),
                cardView.alternateCostTapCount(),
                cardView.alternateCostManaCost(),
                cardView.graveyardActivatedAbilities(),
                cardView.transformable(),
                cardView.kickerCost()
        );
    }

    private CardView applyStaticGrantedCardTypes(CardView cardView, Set<CardType> staticGrantedCardTypes) {
        if (staticGrantedCardTypes.isEmpty()) {
            return cardView;
        }
        Set<CardType> mergedTypes = new HashSet<>(cardView.additionalTypes());
        mergedTypes.addAll(staticGrantedCardTypes);
        return new CardView(
                cardView.id(), cardView.name(), cardView.type(), mergedTypes, cardView.supertypes(), cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount(),
                cardView.token(),
                cardView.watermark(),
                cardView.hasAlternateCastingCost(),
                cardView.alternateCostLifePayment(),
                cardView.alternateCostSacrificeCount(),
                cardView.alternateCostTapCount(),
                cardView.alternateCostManaCost(),
                cardView.graveyardActivatedAbilities(),
                cardView.transformable(),
                cardView.kickerCost()
        );
    }

    private CardView applyStaticGrantedSupertypes(CardView cardView, Set<CardSupertype> staticGrantedSupertypes) {
        if (staticGrantedSupertypes.isEmpty()) {
            return cardView;
        }
        Set<CardSupertype> mergedSupertypes = EnumSet.copyOf(cardView.supertypes().isEmpty() ? EnumSet.noneOf(CardSupertype.class) : cardView.supertypes());
        mergedSupertypes.addAll(staticGrantedSupertypes);
        if (mergedSupertypes.equals(cardView.supertypes())) {
            return cardView;
        }
        return new CardView(
                cardView.id(), cardView.name(), cardView.type(), cardView.additionalTypes(), mergedSupertypes, cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount(),
                cardView.token(),
                cardView.watermark(),
                cardView.hasAlternateCastingCost(),
                cardView.alternateCostLifePayment(),
                cardView.alternateCostSacrificeCount(),
                cardView.alternateCostTapCount(),
                cardView.alternateCostManaCost(),
                cardView.graveyardActivatedAbilities(),
                cardView.transformable(),
                cardView.kickerCost()
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
        return new CardView(
                cardView.id(), cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), mergedAbilities, cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount(),
                cardView.token(),
                cardView.watermark(),
                cardView.hasAlternateCastingCost(),
                cardView.alternateCostLifePayment(),
                cardView.alternateCostSacrificeCount(),
                cardView.alternateCostTapCount(),
                cardView.alternateCostManaCost(),
                cardView.graveyardActivatedAbilities(),
                cardView.transformable(),
                cardView.kickerCost()
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
                cardView.id(), cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), cardView.subtypes(),
                modifiedText, cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount(),
                cardView.token(),
                cardView.watermark(),
                cardView.hasAlternateCastingCost(),
                cardView.alternateCostLifePayment(),
                cardView.alternateCostSacrificeCount(),
                cardView.alternateCostTapCount(),
                cardView.alternateCostManaCost(),
                cardView.graveyardActivatedAbilities(),
                cardView.transformable(),
                cardView.kickerCost()
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

    private static final Set<CardSubtype> BASIC_LAND_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST,
            CardSubtype.MOUNTAIN,
            CardSubtype.ISLAND,
            CardSubtype.PLAINS,
            CardSubtype.SWAMP
    );

    private CardView applyStaticGrantedSubtypes(CardView cardView, List<CardSubtype> staticGrantedSubtypes, boolean subtypeOverriding, boolean landSubtypeOverriding) {
        if (staticGrantedSubtypes.isEmpty()) {
            return cardView;
        }
        List<CardSubtype> mergedSubtypes;
        if (landSubtypeOverriding) {
            // Land type override (e.g. Evil Presence): remove all basic land subtypes, then add the granted ones
            mergedSubtypes = new ArrayList<>();
            for (CardSubtype subtype : cardView.subtypes()) {
                if (!BASIC_LAND_SUBTYPES.contains(subtype)) {
                    mergedSubtypes.add(subtype);
                }
            }
        } else if (subtypeOverriding) {
            // Creature subtype override: keep only non-creature subtypes from the original, then add the granted ones
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
                cardView.id(), cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), mergedSubtypes,
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount(),
                cardView.token(),
                cardView.watermark(),
                cardView.hasAlternateCastingCost(),
                cardView.alternateCostLifePayment(),
                cardView.alternateCostSacrificeCount(),
                cardView.alternateCostTapCount(),
                cardView.alternateCostManaCost(),
                cardView.graveyardActivatedAbilities(),
                cardView.transformable(),
                cardView.kickerCost()
        );
    }

    private CardView stripCardActivatedAbilities(CardView cardView) {
        if (cardView.activatedAbilities().isEmpty()) {
            return cardView;
        }
        return new CardView(
                cardView.id(), cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), cardView.color(), cardView.colors(), cardView.needsTarget(),
                cardView.needsSpellTarget(), List.of(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount(),
                cardView.token(),
                cardView.watermark(),
                cardView.hasAlternateCastingCost(),
                cardView.alternateCostLifePayment(),
                cardView.alternateCostSacrificeCount(),
                cardView.alternateCostTapCount(),
                cardView.alternateCostManaCost(),
                cardView.graveyardActivatedAbilities(),
                cardView.transformable(),
                cardView.kickerCost()
        );
    }

    private CardView applyStaticGrantedColors(CardView cardView, Permanent p, Set<CardColor> staticGrantedColors, boolean colorOverriding) {
        // Determine the effective color: static grants add to permanent's colors
        // If the permanent already has a color override (from non-static effects), use that
        if (staticGrantedColors.isEmpty() && !p.isColorOverridden() && p.getGrantedColors().isEmpty()) {
            return cardView;
        }
        CardColor effectiveColor = cardView.color();
        List<CardColor> effectiveColors = new ArrayList<>(cardView.colors());
        // Persistent granted colors are additive ("in addition to its other colors")
        if (!p.getGrantedColors().isEmpty()) {
            for (CardColor color : p.getGrantedColors()) {
                if (!effectiveColors.contains(color)) {
                    effectiveColors.add(color);
                }
            }
            if (effectiveColor == null) {
                effectiveColor = p.getGrantedColors().iterator().next();
            }
        }
        if (p.isColorOverridden() && !p.getTransientColors().isEmpty()) {
            effectiveColor = p.getTransientColors().iterator().next();
            effectiveColors = new ArrayList<>(p.getTransientColors());
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
                cardView.id(), cardView.name(), cardView.type(), cardView.additionalTypes(), cardView.supertypes(), cardView.subtypes(),
                cardView.cardText(), cardView.manaCost(), cardView.power(), cardView.toughness(),
                cardView.keywords(), cardView.hasTapAbility(), cardView.setCode(),
                cardView.collectorNumber(), effectiveColor, List.copyOf(effectiveColors), cardView.needsTarget(),
                cardView.needsSpellTarget(), cardView.activatedAbilities(), cardView.loyalty(),
                cardView.hasConvoke(), cardView.hasPhyrexianMana(), cardView.phyrexianManaCount(),
                cardView.token(),
                cardView.watermark(),
                cardView.hasAlternateCastingCost(),
                cardView.alternateCostLifePayment(),
                cardView.alternateCostSacrificeCount(),
                cardView.alternateCostTapCount(),
                cardView.alternateCostManaCost(),
                cardView.graveyardActivatedAbilities(),
                cardView.transformable(),
                cardView.kickerCost()
        );
    }
}
