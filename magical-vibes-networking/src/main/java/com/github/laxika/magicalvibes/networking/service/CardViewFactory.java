package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.networking.model.ActivatedAbilityView;
import com.github.laxika.magicalvibes.networking.model.CardView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CardViewFactory {

    public CardView create(Card card) {
        boolean hasTapAbility = !card.getEffects(EffectSlot.ON_TAP).isEmpty()
                || card.getActivatedAbilities().stream().anyMatch(ActivatedAbility::isRequiresTap);

        List<ActivatedAbilityView> abilityViews = card.getActivatedAbilities().stream()
                .map(this::createAbilityView)
                .toList();

        return new CardView(
                card.getName(),
                card.getType(),
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
                card.getFlavorText(),
                card.getColor(),
                abilityViews
        );
    }

    private ActivatedAbilityView createAbilityView(ActivatedAbility ability) {
        boolean targetsPlayer = ability.getEffects().stream()
                .anyMatch(e -> e instanceof MillTargetPlayerEffect);

        List<String> allowedTargetTypes = new ArrayList<>();
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof TapTargetPermanentEffect tapEffect) {
                tapEffect.allowedTypes().forEach(t -> allowedTargetTypes.add(t.getDisplayName()));
            }
        }

        return new ActivatedAbilityView(
                ability.getDescription(),
                ability.isRequiresTap(),
                ability.isNeedsTarget(),
                targetsPlayer,
                allowedTargetTypes,
                ability.getManaCost()
        );
    }
}
