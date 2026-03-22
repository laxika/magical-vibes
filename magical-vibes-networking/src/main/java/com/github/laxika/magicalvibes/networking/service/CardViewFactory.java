package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.networking.model.ActivatedAbilityView;
import com.github.laxika.magicalvibes.networking.model.CardView;
import org.springframework.stereotype.Service;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.ArrayList;
import java.util.List;

@Service
public class CardViewFactory {

    /**
     * Creates a CardView with additional granted subtypes merged in.
     * Only merges granted subtypes for creature cards.
     */
    public CardView create(Card card, List<CardSubtype> grantedSubtypes) {
        CardView base = create(card);
        if (grantedSubtypes.isEmpty() || !card.hasType(CardType.CREATURE)) return base;
        List<CardSubtype> merged = new ArrayList<>(base.subtypes());
        for (CardSubtype st : grantedSubtypes) {
            if (!merged.contains(st)) merged.add(st);
        }
        return new CardView(
                base.id(), base.name(), base.type(), base.additionalTypes(), base.supertypes(),
                merged, base.cardText(), base.manaCost(), base.power(), base.toughness(),
                base.keywords(), base.hasTapAbility(), base.setCode(), base.collectorNumber(),
                base.color(), base.colors(), base.needsTarget(), base.needsSpellTarget(),
                base.activatedAbilities(), base.loyalty(), base.hasConvoke(), base.hasPhyrexianMana(),
                base.phyrexianManaCount(), base.token(), base.watermark(), base.hasAlternateCastingCost(),
                base.alternateCostLifePayment(), base.alternateCostSacrificeCount(),
                base.graveyardActivatedAbilities(), base.transformable(), base.kickerCost());
    }

    public CardView create(Card card) {
        boolean hasTapAbility = !card.getEffects(EffectSlot.ON_TAP).isEmpty();

        List<ActivatedAbilityView> abilityViews = card.getActivatedAbilities().stream()
                .map(this::createAbilityView)
                .toList();

        boolean hasPhyrexianMana = false;
        int phyrexianManaCount = 0;
        if (card.getManaCost() != null) {
            ManaCost cost = new ManaCost(card.getManaCost());
            hasPhyrexianMana = cost.hasPhyrexianMana();
            phyrexianManaCount = cost.getPhyrexianManaCount();
        }

        List<ActivatedAbilityView> graveyardAbilityViews = card.getGraveyardActivatedAbilities().stream()
                .map(this::createAbilityView)
                .toList();

        var altCastOpt = card.getCastingOption(AlternateHandCast.class);
        boolean hasAlternateCastingCost = altCastOpt.isPresent();
        int alternateCostLifePayment = altCastOpt.flatMap(a -> a.getCost(LifeCastingCost.class)).map(LifeCastingCost::amount).orElse(0);
        int alternateCostSacrificeCount = altCastOpt.flatMap(a -> a.getCost(SacrificePermanentsCost.class)).map(SacrificePermanentsCost::count).orElse(0);

        return new CardView(
                card.getId(),
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
                card.getColors(),
                card.isNeedsTarget(),
                card.isNeedsSpellTarget(),
                abilityViews,
                card.getLoyalty(),
                card.getKeywords().contains(Keyword.CONVOKE),
                hasPhyrexianMana,
                phyrexianManaCount,
                card.isToken(),
                card.getWatermark(),
                hasAlternateCastingCost,
                alternateCostLifePayment,
                alternateCostSacrificeCount,
                graveyardAbilityViews,
                card.getBackFaceCard() != null,
                card.getEffects(EffectSlot.STATIC).stream()
                        .filter(e -> e instanceof KickerEffect)
                        .map(e -> {
                            KickerEffect ke = (KickerEffect) e;
                            if (ke.hasManaCost()) return ke.cost();
                            if (ke.hasSacrificeCost()) return "Sacrifice " + ke.sacrificeDescription();
                            return null;
                        })
                        .findFirst().orElse(null));
    }

    public ActivatedAbilityView createAbilityView(ActivatedAbility ability) {
        boolean isManaAbility = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget()
                && ability.getLoyaltyCost() == null
                && ability.getEffects().stream()
                        .filter(e -> !(e instanceof CostEffect))
                        .anyMatch(e -> e instanceof ManaProducingEffect);
        return new ActivatedAbilityView(
                ability.getDescription(),
                ability.isRequiresTap(),
                ability.isNeedsTarget(),
                ability.isNeedsSpellTarget(),
                ability.getManaCost(),
                ability.getLoyaltyCost(),
                ability.getMinTargets(),
                ability.getMaxTargets(),
                isManaAbility,
                ability.isVariableLoyaltyCost());
    }
}
