package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import lombok.Builder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable card projection sent to the client.
 * <p>
 * Layered effects are applied by deriving one view from another (see {@code PermanentViewFactory}),
 * so every derivation MUST go through {@link #toBuilder()} rather than re-listing the components in
 * a {@code new CardView(...)} call. Hand-copied constructor calls silently drop any component added
 * later — the same drift hazard {@code agent-docs/ARCHITECTURE.md} calls out for cast dispatch.
 *
 * @param colorIdentity display-only, and unlike {@link #colors} never touched by a layered colour
 *                      effect. It exists so the client can tint a land's frame, since a land is
 *                      colourless under CR 202.2 and so ships an empty {@link #colors}.
 * @param prepareSpell the spell printed inset on a "prepare" layout card's front face (SOS
 *                     "Prepared"), projected from the card's back face; null for every other card.
 *                     Not a transform: the front face stays visible and this is shown alongside it.
 */
@Builder(toBuilder = true)
public record CardView(
        UUID id,
        String name,
        CardType type,
        Set<CardType> additionalTypes,
        Set<CardSupertype> supertypes,
        List<CardSubtype> subtypes,
        String cardText,
        String manaCost,
        Integer power,
        Integer toughness,
        Set<Keyword> keywords,
        boolean hasTapAbility,
        String setCode,
        String collectorNumber,
        CardColor color,
        List<CardColor> colors,
        List<CardColor> colorIdentity,
        boolean needsTarget,
        boolean needsSpellTarget,
        boolean requiresXValue,
        int xValueMin,
        int xValueMax,
        List<ActivatedAbilityView> activatedAbilities,
        Integer loyalty,
        boolean hasConvoke,
        boolean hasPhyrexianMana,
        int phyrexianManaCount,
        boolean token,
        String watermark,
        boolean hasAlternateCastingCost,
        boolean alternateCostRequiresTarget,
        int alternateCostLifePayment,
        int alternateCostSacrificeCount,
        int alternateCostTapCount,
        int alternateCostReturnCount,
        String alternateCostManaCost,
        int alternateCostExileHandCount,
        String alternateCostExileHandLabel,
        boolean alternateCostDiscardsHandCard,
        boolean alternateCostRevealsHandCard,
        boolean graveyardCastRequiresDiscard,
        List<ActivatedAbilityView> graveyardActivatedAbilities,
        List<ActivatedAbilityView> handActivatedAbilities,
        boolean transformable,
        String kickerCost,
        boolean kickerRequiresTap,
        boolean kickerRequiresReturn,
        String buybackCost,
        boolean buybackRequiresSacrifice,
        int buybackDiscardCount,
        int modalChoicesRequired,
        int modalChoicesMax,
        boolean modalOptional,
        List<ModalOptionView> modalOptions,
        int exileCastCounterCost,
        boolean additionalChooseCreatureType,
        List<String> additionalCreatureTypeChoices,
        CardView prepareSpell
) {
}
