package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CastingOption;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.RevealCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseXValueCost;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.BeholdAndExileCost;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeCost;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.networking.model.ActivatedAbilityView;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.ModalOptionView;
import org.springframework.stereotype.Service;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CardViewFactory {

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = Set.of(
            CardSubtype.FOREST, CardSubtype.MOUNTAIN, CardSubtype.ISLAND, CardSubtype.PLAINS,
            CardSubtype.SWAMP, CardSubtype.AURA, CardSubtype.EQUIPMENT, CardSubtype.AJANI,
            CardSubtype.KOTH, CardSubtype.BOLAS);

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
        return base.toBuilder().subtypes(merged).build();
    }

    /**
     * Creates a CardView with granted subtypes merged in and additional graveyard-activated abilities
     * appended (e.g. unearth granted by Sedris, the Traitor King or Mishra, Tamer of Mak Fawa).
     * They are appended after the card's own so indices stay aligned with the server-side graveyard
     * ability list.
     */
    public CardView create(Card card, List<CardSubtype> grantedSubtypes, List<ActivatedAbility> grantedGraveyardAbilities) {
        return create(card, grantedSubtypes, grantedGraveyardAbilities, List.of());
    }

    /**
     * Creates a CardView with granted subtypes and additional zone-specific activated abilities.
     * Granted hand abilities are appended after the card's own abilities so their indices match
     * the effective server-side hand ability list.
     */
    public CardView create(Card card, List<CardSubtype> grantedSubtypes,
                           List<ActivatedAbility> grantedGraveyardAbilities,
                           List<ActivatedAbility> grantedHandAbilities) {
        CardView base = create(card, grantedSubtypes);
        CardView result = base;
        if (!grantedGraveyardAbilities.isEmpty()) {
            List<ActivatedAbilityView> mergedGraveyard = new ArrayList<>(base.graveyardActivatedAbilities());
            for (ActivatedAbility ability : grantedGraveyardAbilities) {
                mergedGraveyard.add(createAbilityView(ability));
            }
            result = result.toBuilder().graveyardActivatedAbilities(mergedGraveyard).build();
        }
        if (!grantedHandAbilities.isEmpty()) {
            List<ActivatedAbilityView> mergedHand = new ArrayList<>(result.handActivatedAbilities());
            for (ActivatedAbility ability : grantedHandAbilities) {
                mergedHand.add(createAbilityView(ability));
            }
            result = result.toBuilder().handActivatedAbilities(mergedHand).build();
        }
        return result;
    }

    public CardView create(Card card) {
        boolean hasTapAbility = !card.getEffects(EffectSlot.ON_TAP).isEmpty();

        List<ActivatedAbilityView> abilityViews = card.getActivatedAbilities().stream()
                .filter(ability -> !ability.isExileOnly())
                .map(this::createAbilityView)
                .toList();

        List<ActivatedAbilityView> exileAbilityViews = card.getActivatedAbilities().stream()
                .filter(ActivatedAbility::isExileOnly)
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

        ChooseXValueCost xValueCost = card.getEffects(EffectSlot.SPELL).stream()
                .filter(ChooseXValueCost.class::isInstance)
                .map(ChooseXValueCost.class::cast)
                .findFirst()
                .orElse(null);
        boolean chooseCreatureTypeCost = card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(ChooseCreatureTypeCost.class::isInstance);
        List<String> creatureTypeChoices = chooseCreatureTypeCost
                ? java.util.Arrays.stream(CardSubtype.values())
                .filter(subtype -> !NON_CREATURE_SUBTYPES.contains(subtype))
                .map(CardSubtype::getDisplayName)
                .toList()
                : List.of();

        // Prepare cards keep their front face on the battlefield and print the prepare spell inset,
        // so the spell is projected as a nested view rather than as a face the client flips to.
        // The prepare spell itself has no back face, so this recurses exactly one level.
        CardView prepareSpellView = card.getKeywords().contains(Keyword.PREPARED) && card.getBackFaceCard() != null
                ? create(card.getBackFaceCard())
                : null;

        var altCastOpt = card.getCastingOption(AlternateHandCast.class).map(a -> (CastingOption) a)
                .or(() -> card.getCastingOption(BestowCast.class).map(b -> (CastingOption) b));
        boolean hasAlternateCastingCost = altCastOpt.isPresent();
        int alternateCostLifePayment = altCastOpt.flatMap(a -> a.getCost(LifeCastingCost.class)).map(LifeCastingCost::amount).orElse(0);
        int alternateCostSacrificeCount = altCastOpt.flatMap(a -> a.getCost(SacrificePermanentsCost.class)).map(SacrificePermanentsCost::count).orElse(0);
        int alternateCostTapCount = altCastOpt.flatMap(a -> a.getCost(TapUntappedPermanentsCost.class)).map(TapUntappedPermanentsCost::count).orElse(0);
        int alternateCostReturnCount = altCastOpt.flatMap(a -> a.getCost(ReturnPermanentsCost.class)).map(ReturnPermanentsCost::count).orElse(0);
        String alternateCostManaCost = altCastOpt.flatMap(a -> a.getCost(ManaCastingCost.class)).map(ManaCastingCost::manaCost).orElse(null);
        var exileHandCost = altCastOpt.flatMap(a -> a.getCost(ExileCardsFromHandCastingCost.class));
        List<DiscardCardCastingCost> discardHandCosts = altCastOpt
                .map(a -> a.getCosts(DiscardCardCastingCost.class)).orElse(List.of());
        var revealHandCost = altCastOpt.flatMap(a -> a.getCost(RevealCardsFromHandCastingCost.class));
        int alternateCostExileHandCount = exileHandCost.map(ExileCardsFromHandCastingCost::count)
                .orElse(!discardHandCosts.isEmpty() ? discardHandCosts.size()
                        : revealHandCost.isPresent() ? 1 : 0);
        String alternateCostExileHandLabel = exileHandCost.map(ExileCardsFromHandCastingCost::label)
                .orElseGet(() -> !discardHandCosts.isEmpty()
                        ? String.join(" and ", discardHandCosts.stream()
                                .map(cost -> cost.label() != null ? cost.label() : "a card").toList())
                        : revealHandCost.map(RevealCardsFromHandCastingCost::label).orElse(null));
        boolean alternateCostDiscardsHandCard = !discardHandCosts.isEmpty();
        boolean alternateCostRevealsHandCard = revealHandCost.isPresent();
        boolean graveyardCastRequiresDiscard = card.getCastingOption(GraveyardCast.class)
                .flatMap(castingOption -> castingOption.getCost(DiscardCardCastingCost.class))
                .isPresent();

        BuybackEffect buybackEffect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof BuybackEffect)
                .map(e -> (BuybackEffect) e)
                .findFirst().orElse(null);
        String buybackCost = buybackEffect == null ? null
                : buybackEffect.hasManaCost() ? buybackEffect.cost()
                : buybackEffect.hasSacrificeCost() ? "Sacrifice " + buybackEffect.sacrificeDescription()
                : buybackEffect.hasDiscardCost() ? "Discard " + buybackEffect.discardCount() + " cards"
                : null;

        KickerEffect kickerEffect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof KickerEffect)
                .map(e -> (KickerEffect) e)
                .findFirst().orElse(null);
        String kickerCost = kickerEffect == null ? null
                : kickerEffect.hasManaCost() ? kickerEffect.cost()
                : kickerEffect.hasSacrificeCost() ? "Sacrifice " + kickerEffect.sacrificeDescription()
                : kickerEffect.hasTapCost() ? "Tap " + kickerEffect.tapDescription()
                : kickerEffect.hasReturnCost() ? "Return " + kickerEffect.returnDescription()
                : null;
        boolean kickerRequiresTap = kickerEffect != null && kickerEffect.hasTapCost();
        boolean kickerRequiresReturn = kickerEffect != null && kickerEffect.hasReturnCost();

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
                card.getColorIdentity(),
                EffectResolution.needsTarget(card),
                EffectResolution.needsSpellTarget(card),
                xValueCost != null,
                xValueCost != null ? xValueCost.minValue() : 0,
                xValueCost != null ? xValueCost.maxValue() : 0,
                abilityViews,
                card.getLoyalty(),
                card.getKeywords().contains(Keyword.CONVOKE)
                        || card.getKeywords().contains(Keyword.IMPROVISE),
                card.getKeywords().contains(Keyword.HARMONIZE),
                hasPhyrexianMana,
                phyrexianManaCount,
                card.isToken(),
                card.getWatermark(),
                hasAlternateCastingCost,
                card.getCastingOption(BestowCast.class).isPresent(),
                alternateCostLifePayment,
                alternateCostSacrificeCount,
                alternateCostTapCount,
                alternateCostReturnCount,
                alternateCostManaCost,
                alternateCostExileHandCount,
                alternateCostExileHandLabel,
                alternateCostDiscardsHandCard,
                alternateCostRevealsHandCard,
                graveyardCastRequiresDiscard,
                graveyardAbilityViews,
                handAbilityViews,
                exileAbilityViews,
                card.getBackFaceCard() != null,
                kickerCost,
                kickerRequiresTap,
                kickerRequiresReturn,
                buybackCost,
                buybackEffect != null && buybackEffect.hasSacrificeCost(),
                buybackEffect != null ? buybackEffect.sacrificeCount() : 0,
                buybackEffect != null ? buybackEffect.discardCount() : 0,
                modalEffect != null ? modalEffect.choicesRequired() : 0,
                modalEffect != null ? modalEffect.choicesMax() : 0,
                modalEffect != null && modalEffect.optional(),
                modalEffect != null && modalEffect.modesMayRepeat(),
                modalOptions,
                0,
                chooseCreatureTypeCost,
                creatureTypeChoices,
                prepareSpellView);
    }

    /**
     * Graveyard CardView: if this card can be cast via Disturb and the back face needs a target
     * (e.g. Aura), report needsTarget so the flashback UI prompts before cast.
     */
    public CardView createForGraveyard(Card card, List<CardSubtype> grantedSubtypes,
                                       List<ActivatedAbility> grantedGraveyardAbilities) {
        return createForGraveyard(card, grantedSubtypes, grantedGraveyardAbilities, false);
    }

    public CardView createForGraveyard(Card card, List<CardSubtype> grantedSubtypes,
                                       List<ActivatedAbility> grantedGraveyardAbilities,
                                       boolean graveyardAbilitiesSuppressed) {
        CardView base = create(card, grantedSubtypes, grantedGraveyardAbilities);
        if (graveyardAbilitiesSuppressed) {
            return base.toBuilder()
                    .keywords(Set.of())
                    .hasTapAbility(false)
                    .hasConvoke(false)
                    .hasHarmonize(false)
                    .activatedAbilities(List.of())
                    .graveyardActivatedAbilities(List.of())
                    .handActivatedAbilities(List.of())
                    .exileActivatedAbilities(List.of())
                    .graveyardCastRequiresDiscard(false)
                    .build();
        }
        if (base.needsTarget() || !disturbBackFaceNeedsTarget(card)) {
            return base;
        }
        return base.toBuilder().needsTarget(true).build();
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
        return new ModalOptionView(option.label(), needsTarget, needsSpellTarget, targetCount, option.manaCost());
    }

    public ActivatedAbilityView createAbilityView(ActivatedAbility ability) {
        boolean isManaAbility = !ability.isNeedsTarget() && !ability.isNeedsSpellTarget()
                && ability.getLoyaltyCost() == null
                && ability.getEffects().stream()
                        .filter(e -> !(e instanceof CostEffect))
                        .anyMatch(e -> e instanceof ManaProducingEffect);
        ChooseOneEffect modalEffect = ability.modalEffectAtActivation();
        List<ModalOptionView> modalOptions = modalEffect == null ? null
                : modalEffect.options().stream().map(this::createModalOptionView).toList();
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
                ability.isVariableLoyaltyCost(),
                ability.getEffects().stream()
                        .filter(RemoveXCountersFromSourceCost.class::isInstance)
                        .map(RemoveXCountersFromSourceCost.class::cast)
                        .map(cost -> cost.counterType().name())
                        .findFirst()
                        .orElse(null),
                ability.isRequiresXValue(),
                ability.isXValueFromControlledCreatureCounters(),
                ability.getXValueFromCardsInHandColor(),
                modalEffect != null ? modalEffect.choicesRequired() : 0,
                modalEffect != null ? modalEffect.choicesMax() : 0,
                modalOptions);
    }
}
