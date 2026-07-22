package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.networking.model.ActivatedAbilityView;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.ModalOptionView;
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
                base.alternateCostTapCount(), base.alternateCostReturnCount(), base.alternateCostManaCost(),
                base.graveyardActivatedAbilities(), base.handActivatedAbilities(), base.transformable(), base.kickerCost(),
                base.modalChoicesRequired(), base.modalChoicesMax(), base.modalOptional(), base.modalOptions());
    }

    /**
     * Creates a CardView with granted subtypes merged in and additional graveyard-activated abilities
     * appended (e.g. unearth granted by Sedris, the Traitor King). Granted graveyard abilities are only
     * merged for creature cards and are appended after the card's own so indices stay aligned with the
     * server-side graveyard ability list.
     */
    public CardView create(Card card, List<CardSubtype> grantedSubtypes, List<ActivatedAbility> grantedGraveyardAbilities) {
        CardView base = create(card, grantedSubtypes);
        if (grantedGraveyardAbilities.isEmpty() || !card.hasType(CardType.CREATURE)) return base;
        List<ActivatedAbilityView> mergedGraveyard = new ArrayList<>(base.graveyardActivatedAbilities());
        for (ActivatedAbility ability : grantedGraveyardAbilities) {
            mergedGraveyard.add(createAbilityView(ability));
        }
        return new CardView(
                base.id(), base.name(), base.type(), base.additionalTypes(), base.supertypes(),
                base.subtypes(), base.cardText(), base.manaCost(), base.power(), base.toughness(),
                base.keywords(), base.hasTapAbility(), base.setCode(), base.collectorNumber(),
                base.color(), base.colors(), base.needsTarget(), base.needsSpellTarget(),
                base.activatedAbilities(), base.loyalty(), base.hasConvoke(), base.hasPhyrexianMana(),
                base.phyrexianManaCount(), base.token(), base.watermark(), base.hasAlternateCastingCost(),
                base.alternateCostLifePayment(), base.alternateCostSacrificeCount(),
                base.alternateCostTapCount(), base.alternateCostReturnCount(), base.alternateCostManaCost(),
                mergedGraveyard, base.handActivatedAbilities(), base.transformable(), base.kickerCost(),
                base.modalChoicesRequired(), base.modalChoicesMax(), base.modalOptional(), base.modalOptions());
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

        List<ActivatedAbilityView> handAbilityViews = card.getHandActivatedAbilities().stream()
                .map(this::createAbilityView)
                .toList();

        ChooseOneEffect modalEffect = findModalEffect(card);
        List<ModalOptionView> modalOptions = modalEffect == null ? null
                : modalEffect.options().stream().map(this::createModalOptionView).toList();

        var altCastOpt = card.getCastingOption(AlternateHandCast.class);
        boolean hasAlternateCastingCost = altCastOpt.isPresent();
        int alternateCostLifePayment = altCastOpt.flatMap(a -> a.getCost(LifeCastingCost.class)).map(LifeCastingCost::amount).orElse(0);
        int alternateCostSacrificeCount = altCastOpt.flatMap(a -> a.getCost(SacrificePermanentsCost.class)).map(SacrificePermanentsCost::count).orElse(0);
        int alternateCostTapCount = altCastOpt.flatMap(a -> a.getCost(TapUntappedPermanentsCost.class)).map(TapUntappedPermanentsCost::count).orElse(0);
        int alternateCostReturnCount = altCastOpt.flatMap(a -> a.getCost(ReturnPermanentsCost.class)).map(ReturnPermanentsCost::count).orElse(0);
        String alternateCostManaCost = altCastOpt.flatMap(a -> a.getCost(ManaCastingCost.class)).map(ManaCastingCost::manaCost).orElse(null);

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
                EffectResolution.needsTarget(card),
                EffectResolution.needsSpellTarget(card),
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
                alternateCostTapCount,
                alternateCostReturnCount,
                alternateCostManaCost,
                graveyardAbilityViews,
                handAbilityViews,
                card.getBackFaceCard() != null,
                card.getEffects(EffectSlot.STATIC).stream()
                        .filter(e -> e instanceof KickerEffect)
                        .map(e -> {
                            KickerEffect ke = (KickerEffect) e;
                            if (ke.hasManaCost()) return ke.cost();
                            if (ke.hasSacrificeCost()) return "Sacrifice " + ke.sacrificeDescription();
                            return null;
                        })
                        .findFirst().orElse(null),
                modalEffect != null ? modalEffect.choicesRequired() : 0,
                modalEffect != null ? modalEffect.choicesMax() : 0,
                modalEffect != null && modalEffect.optional(),
                modalOptions);
    }

    /**
     * Graveyard CardView: if this card can be cast via Disturb and the back face needs a target
     * (e.g. Aura), report needsTarget so the flashback UI prompts before cast.
     */
    public CardView createForGraveyard(Card card, List<CardSubtype> grantedSubtypes,
                                       List<ActivatedAbility> grantedGraveyardAbilities) {
        CardView base = create(card, grantedSubtypes, grantedGraveyardAbilities);
        if (base.needsTarget() || !disturbBackFaceNeedsTarget(card)) {
            return base;
        }
        return new CardView(
                base.id(), base.name(), base.type(), base.additionalTypes(), base.supertypes(),
                base.subtypes(), base.cardText(), base.manaCost(), base.power(), base.toughness(),
                base.keywords(), base.hasTapAbility(), base.setCode(), base.collectorNumber(),
                base.color(), base.colors(), true, base.needsSpellTarget(),
                base.activatedAbilities(), base.loyalty(), base.hasConvoke(), base.hasPhyrexianMana(),
                base.phyrexianManaCount(), base.token(), base.watermark(), base.hasAlternateCastingCost(),
                base.alternateCostLifePayment(), base.alternateCostSacrificeCount(),
                base.alternateCostTapCount(), base.alternateCostReturnCount(), base.alternateCostManaCost(),
                base.graveyardActivatedAbilities(), base.handActivatedAbilities(), base.transformable(),
                base.kickerCost(), base.modalChoicesRequired(), base.modalChoicesMax(), base.modalOptional(), base.modalOptions());
    }

    private static boolean disturbBackFaceNeedsTarget(Card card) {
        if (card.getCastingOption(DisturbCast.class).isEmpty()) {
            return false;
        }
        Card back = card.getBackFaceCard();
        return back != null && EffectResolution.needsTarget(back);
    }

    /**
     * Finds the card's modal ("choose one/two") effect, whether it is a modal spell (SPELL slot)
     * or a modal ETB trigger whose mode is picked at cast time (ON_ENTER_BATTLEFIELD slot).
     */
    private ChooseOneEffect findModalEffect(Card card) {
        for (CardEffect e : card.getEffects(EffectSlot.SPELL)) {
            if (e instanceof ChooseOneEffect coe) return coe;
        }
        for (CardEffect e : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (e instanceof ChooseOneEffect coe) return coe;
        }
        return null;
    }

    private ModalOptionView createModalOptionView(ChooseOneEffect.ChooseOneOption option) {
        boolean needsSpellTarget = EffectResolution.needsSpellTarget(option.effects());
        boolean needsTarget = !needsSpellTarget
                && (option.targetFilter() != null || option.targetFilters() != null
                        || EffectResolution.needsTarget(option.effects(), List.of(), false, false));
        int targetCount = option.targetFilters() != null ? option.targetFilters().size()
                : (needsTarget || needsSpellTarget ? 1 : 0);
        return new ModalOptionView(option.label(), needsTarget, needsSpellTarget, targetCount);
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
