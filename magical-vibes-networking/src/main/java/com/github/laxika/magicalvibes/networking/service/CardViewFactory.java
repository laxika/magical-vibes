package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
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
import com.github.laxika.magicalvibes.model.filter.CreatureColorTargetFilter;
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

        List<String> spellAllowedTargetTypes = computeSpellAllowedTargetTypes(card);
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
                        || e instanceof DealDamageToAnyTargetEffect);

        List<String> allowedTargetTypes = new ArrayList<>();
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof TapTargetPermanentEffect tapEffect) {
                tapEffect.allowedTypes().forEach(t -> allowedTargetTypes.add(t.getDisplayName()));
            }
        }

        List<String> allowedTargetColors = new ArrayList<>();
        if (ability.getTargetFilter() instanceof CreatureColorTargetFilter colorFilter) {
            colorFilter.colors().forEach(c -> allowedTargetColors.add(c.name()));
        }

        return new ActivatedAbilityView(
                ability.getDescription(),
                ability.isRequiresTap(),
                ability.isNeedsTarget(),
                ability.isNeedsSpellTarget(),
                targetsPlayer,
                allowedTargetTypes,
                allowedTargetColors,
                ability.getManaCost(),
                ability.getLoyaltyCost()
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
                        CardType.BASIC_LAND.getDisplayName()
                );
            }
        }
        return List.of();
    }
}
