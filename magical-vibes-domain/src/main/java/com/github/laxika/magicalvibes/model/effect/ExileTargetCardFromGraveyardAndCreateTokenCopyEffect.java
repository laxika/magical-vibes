package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.Set;

/**
 * Exiles the targeted card from a graveyard, then creates a token that is a copy of that card.
 * Optional additional subtypes are granted to the token "in addition to its other types".
 * Optionally grants haste and/or schedules the token for exile at the beginning of the next end step.
 * Optional Eternalize-style overrides ({@code colorOverride}, {@code powerOverride},
 * {@code toughnessOverride}) transform the copy (e.g. The Scarab God: 4/4 black Zombie).
 *
 * @param filter              predicate restricting valid graveyard targets; {@code null} means any card
 * @param ownGraveyardOnly    when {@code true}, only the controller's graveyard can be targeted
 * @param additionalSubtypes  subtypes added to the token copy (e.g. Spirit for Séance, Zombie for Scarab God)
 * @param grantHaste          when {@code true}, the token gains haste
 * @param exileAtEndStep      when {@code true}, the token is exiled at the beginning of the next end step
 * @param colorOverride       if non-null, the token's color is set to exactly this color
 * @param powerOverride       if non-null, the token's base power is set to this
 * @param toughnessOverride   if non-null, the token's base toughness is set to this
 * @param additionalKeywords  keywords the token copy has in addition to the copied ones (e.g. flying for Soul Separator)
 * @param createZombieTokenWithExiledCardStats when {@code true}, a second token — a black Zombie creature token whose
 *                            power and toughness equal the exiled card's power and toughness — is created afterwards
 *                            (Soul Separator)
 * @param targetPutIntoGraveyardFromAnywhereThisTurn when {@code true}, the target must have been put into a graveyard from
 *                            any zone this turn
 * @param exileOtherControlledTokensOfSubtype when non-null, exile all other matching tokens controlled by the ability's
 *                            controller after the copy is created
 * @param replaceSubtypes   when {@code true}, the token's creature types are replaced by
 *                          {@code additionalSubtypes} instead of added to the copied types
 */
public record ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
        CardPredicate filter,
        boolean ownGraveyardOnly,
        List<CardSubtype> additionalSubtypes,
        boolean grantHaste,
        boolean exileAtEndStep,
        CardColor colorOverride,
        Integer powerOverride,
        Integer toughnessOverride,
        Set<Keyword> additionalKeywords,
        boolean createZombieTokenWithExiledCardStats,
        boolean targetPutIntoGraveyardFromAnywhereThisTurn,
        CardSubtype exileOtherControlledTokensOfSubtype,
        boolean replaceSubtypes
) implements CardEffect {

    /** Compact form without Eternalize-style P/T/color overrides (Séance). */
    public ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
            CardPredicate filter,
            boolean ownGraveyardOnly,
            List<CardSubtype> additionalSubtypes,
            boolean grantHaste,
            boolean exileAtEndStep) {
        this(filter, ownGraveyardOnly, additionalSubtypes, grantHaste, exileAtEndStep,
                null, null, null, Set.of(), false, false, null, false);
    }

    /** Eternalize-style transform without extra keywords or the companion Zombie token (The Scarab God). */
    public ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
            CardPredicate filter,
            boolean ownGraveyardOnly,
            List<CardSubtype> additionalSubtypes,
            boolean grantHaste,
            boolean exileAtEndStep,
            CardColor colorOverride,
            Integer powerOverride,
            Integer toughnessOverride) {
        this(filter, ownGraveyardOnly, additionalSubtypes, grantHaste, exileAtEndStep,
                colorOverride, powerOverride, toughnessOverride, Set.of(), false, false, null, false);
    }

    /** Full form retaining the original copy-creation options. */
    public ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
            CardPredicate filter,
            boolean ownGraveyardOnly,
            List<CardSubtype> additionalSubtypes,
            boolean grantHaste,
            boolean exileAtEndStep,
            CardColor colorOverride,
            Integer powerOverride,
            Integer toughnessOverride,
            Set<Keyword> additionalKeywords,
            boolean createZombieTokenWithExiledCardStats) {
        this(filter, ownGraveyardOnly, additionalSubtypes, grantHaste, exileAtEndStep,
                colorOverride, powerOverride, toughnessOverride, additionalKeywords,
                createZombieTokenWithExiledCardStats, false, null, false);
    }

    /** Full form retaining the original copy-creation options and replacing creature types. */
    public ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
            CardPredicate filter,
            boolean ownGraveyardOnly,
            List<CardSubtype> additionalSubtypes,
            boolean grantHaste,
            boolean exileAtEndStep,
            CardColor colorOverride,
            Integer powerOverride,
            Integer toughnessOverride,
            Set<Keyword> additionalKeywords,
            boolean createZombieTokenWithExiledCardStats,
            boolean targetPutIntoGraveyardFromAnywhereThisTurn,
            CardSubtype exileOtherControlledTokensOfSubtype) {
        this(filter, ownGraveyardOnly, additionalSubtypes, grantHaste, exileAtEndStep,
                colorOverride, powerOverride, toughnessOverride, additionalKeywords,
                createZombieTokenWithExiledCardStats, targetPutIntoGraveyardFromAnywhereThisTurn,
                exileOtherControlledTokensOfSubtype, false);
    }

    @Override
    public TargetSpec targetSpec() {
        GraveyardSearchScope scope = ownGraveyardOnly
                ? GraveyardSearchScope.CONTROLLERS_GRAVEYARD
                : GraveyardSearchScope.ALL_GRAVEYARDS;
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(scope)
                : TargetPredicates.graveyardCards(filter, scope));
    }
}
