package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateCastingCost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.networking.model.ActivatedAbilityView;
import com.github.laxika.magicalvibes.networking.model.CardView;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardViewFactory {

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

        AlternateCastingCost altCost = card.getAlternateCastingCost();
        boolean hasAlternateCastingCost = altCost != null;
        int alternateCostLifePayment = altCost != null ? altCost.lifeCost() : 0;
        int alternateCostSacrificeCount = altCost != null ? altCost.sacrificeCount() : 0;

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
                alternateCostSacrificeCount);
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
