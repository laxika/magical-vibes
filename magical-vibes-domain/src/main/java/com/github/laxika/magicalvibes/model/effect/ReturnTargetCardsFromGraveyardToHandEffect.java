package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Return target cards matching the filter from a graveyard to a hand. The default spell path uses
 * the controller's graveyard; activated abilities can provide positional graveyard target groups.
 * Multi-target graveyard selection is handled by SpellCastingService at cast time.
 * Targets are stored in StackEntry.targetCardIds and resolved by GraveyardReturnResolutionService.
 *
 * <p>By default, the effect allows up to {@code maxTargets} targets. When {@code exactTargets} is
 * set, exactly {@code maxTargets} targets must be chosen ("Return two target creature cards ..."
 * — Death's Duet).</p>
 *
 * <p>When {@code xScaled} is set the target count is the spell's paid X instead of
 * {@code maxTargets}, and exactly that many targets must be chosen ("Return X target creature
 * cards from your graveyard to your hand" — Shattered Crypt).</p>
 *
 * @param filter            which graveyard cards may be chosen; {@code null} matches any card
 * @param maxTargets        the fixed cap or exact count of chosen cards; ignored when
 *                          {@code dynamicMaxTargets} or {@code xScaled} is set
 * @param dynamicMaxTargets a cast-time cap computed from the game state instead of a fixed number
 *                          ("up to X target cards … where X is …", Reap). Evaluated as the spell is
 *                          cast, after its player target is chosen, so the effect also declares a
 *                          player {@link TargetSpec} in that case
 * @param xScaled           when {@code true} the target count is exactly the spell's paid X
 * @param exactTargets      when {@code true} exactly {@code maxTargets} targets must be chosen
 * @param maxOnePerCardType card types for which at most one selected card is allowed
 * @param unlessAnyPlayerPaysX when {@code true}, any player may pay the spell's X at resolution
 *                             to prevent the return effect
 * @param recordsReturnedCount when {@code true}, the number of cards actually returned is stored
 *                             on the resolving stack entry's event value
 * @param opponentChoosesOneForHand when {@code true}, an opponent chooses one of the selected
 *                                  cards to return to hand and the other returns to the battlefield
 *                                  with haste and a delayed exile
 * @param targetGroups         positional graveyard-card target groups resolved by this effect;
 *                             empty for ordinary single-effect targeting
 * @param targetGroupsMustShareGraveyard when {@code true}, all configured target groups must be
 *                                       cards in the same graveyard
 * @param returnToOwnersHand   when {@code true}, each returned card goes to its owner's hand
 * @param declaresGraveyardTarget when {@code true}, the effect declares its graveyard cards as
 *                                targets for a triggered-ability target choice
 * @param bargainedBattlefieldMaxManaValue when non-null, a bargained spell may put one of its
 *                                       targeted cards with mana value up to this value onto the
 *                                       battlefield instead of returning it to hand
 */
public record ReturnTargetCardsFromGraveyardToHandEffect(
        CardPredicate filter,
        int maxTargets,
        DynamicAmount dynamicMaxTargets,
        boolean xScaled,
        boolean exactTargets,
        int minTargets,
        boolean requireSharedCreatureType,
        Set<CardType> maxOnePerCardType,
        boolean unlessAnyPlayerPaysX,
        boolean opponentChoosesOneForHand,
        List<Integer> targetGroups,
        boolean targetGroupsMustShareGraveyard,
        boolean returnToOwnersHand,
        Integer bargainedBattlefieldMaxManaValue
, boolean recordsReturnedCount, boolean declaresGraveyardTarget) implements TargetCardGroupEffect {
    public ReturnTargetCardsFromGraveyardToHandEffect(
        CardPredicate filter,
        int maxTargets,
        DynamicAmount dynamicMaxTargets,
        boolean xScaled,
        boolean exactTargets,
        int minTargets,
        boolean requireSharedCreatureType,
        Set<CardType> maxOnePerCardType,
        boolean unlessAnyPlayerPaysX,
        boolean opponentChoosesOneForHand,
        List<Integer> targetGroups,
        boolean targetGroupsMustShareGraveyard,
        boolean returnToOwnersHand,
        Integer bargainedBattlefieldMaxManaValue
, boolean recordsReturnedCount) {
        this(filter, maxTargets, dynamicMaxTargets, xScaled, exactTargets, minTargets, requireSharedCreatureType, maxOnePerCardType, unlessAnyPlayerPaysX, opponentChoosesOneForHand, targetGroups, targetGroupsMustShareGraveyard, returnToOwnersHand, bargainedBattlefieldMaxManaValue, recordsReturnedCount, false);
    }

        public ReturnTargetCardsFromGraveyardToHandEffect(
        CardPredicate filter,
        int maxTargets,
        DynamicAmount dynamicMaxTargets,
        boolean xScaled,
        boolean exactTargets,
        int minTargets,
        boolean requireSharedCreatureType,
        Set<CardType> maxOnePerCardType,
        boolean unlessAnyPlayerPaysX,
        boolean opponentChoosesOneForHand,
        List<Integer> targetGroups,
        boolean targetGroupsMustShareGraveyard,
        boolean returnToOwnersHand,
        Integer bargainedBattlefieldMaxManaValue
) {
            this(filter, maxTargets, dynamicMaxTargets, xScaled, exactTargets, minTargets, requireSharedCreatureType, maxOnePerCardType, unlessAnyPlayerPaysX, opponentChoosesOneForHand, targetGroups, targetGroupsMustShareGraveyard, returnToOwnersHand, bargainedBattlefieldMaxManaValue, false);
        }


    public ReturnTargetCardsFromGraveyardToHandEffect {
        maxOnePerCardType = maxOnePerCardType == null ? Set.of() : Set.copyOf(maxOnePerCardType);
        targetGroups = targetGroups == null ? List.of() : List.copyOf(targetGroups);
        if (bargainedBattlefieldMaxManaValue != null && bargainedBattlefieldMaxManaValue < 0) {
            throw new IllegalArgumentException("bargainedBattlefieldMaxManaValue cannot be negative");
        }
    }

    public ReturnTargetCardsFromGraveyardToHandEffect(
            CardPredicate filter, int maxTargets, DynamicAmount dynamicMaxTargets,
            boolean xScaled, boolean exactTargets, int minTargets,
            boolean requireSharedCreatureType, Set<CardType> maxOnePerCardType,
            boolean unlessAnyPlayerPaysX) {
        this(filter, maxTargets, dynamicMaxTargets, xScaled, exactTargets, minTargets,
                requireSharedCreatureType, maxOnePerCardType, unlessAnyPlayerPaysX,
                false, List.of(), false, false, null, false);
    }

    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, int maxTargets) {
        this(filter, maxTargets, null, false, false, 0, false, Set.of(), false, false,
                List.of(), false, false, null, false);
    }

    /** The dynamic-cap form: the cap is counted off the targeted player as the spell is cast. */
    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, DynamicAmount dynamicMaxTargets) {
        this(filter, 0, dynamicMaxTargets, false, false, 0, false, Set.of(), false, false,
                List.of(), false, false, null, false);
    }

    /** Exact-X form: choose exactly the spell's paid X matching cards (Shattered Crypt). */
    public ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate filter, int maxTargets, boolean xScaled) {
        this(filter, maxTargets, null, xScaled, false, 0, false, Set.of(), false, false,
                List.of(), false, false, null, false);
    }

    /** Fixed-exact form: choose exactly {@code targetCount} matching cards (Death's Duet). */
    public static ReturnTargetCardsFromGraveyardToHandEffect exactly(CardPredicate filter, int targetCount) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, targetCount, null, false, true, targetCount, false, Set.of(), false, false,
                List.of(), false, false, null, false);
    }

    /** Fixed-exact form with an X payment that prevents the return at resolution. */
    public static ReturnTargetCardsFromGraveyardToHandEffect exactlyUnlessAnyPlayerPaysX(
            CardPredicate filter, int targetCount) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, targetCount, null, false, true, targetCount, false, Set.of(), true, false,
                List.of(), false, false, null, false);
    }

    public static ReturnTargetCardsFromGraveyardToHandEffect exactlyOne(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(filter, 1, null, false, false, 1, false,
                Set.of(), false, false, List.of(), false, false, null, false);
    }

    public static ReturnTargetCardsFromGraveyardToHandEffect exactlyTwoSharingCreatureType(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(filter, 2, null, false, false, 2, true,
                Set.of(), false, false, List.of(), false, false, null, false);
    }

    /** Return up to one card matching each listed card type. */
    public static ReturnTargetCardsFromGraveyardToHandEffect upToOnePerCardType(
            CardPredicate filter, Set<CardType> cardTypes) {
        if (cardTypes == null || cardTypes.isEmpty()) {
            throw new IllegalArgumentException("cardTypes must not be empty");
        }
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, cardTypes.size(), null, false, false, 0, false, cardTypes, false, false,
                List.of(), false, false, null, false);
    }

    /** Return one selected card to hand and the other to the battlefield after an opponent chooses. */
    public static ReturnTargetCardsFromGraveyardToHandEffect opponentChoosesOneForHand(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, 2, null, false, false, 0, false, Set.of(), false, true,
                List.of(), false, false, null, false);
    }

    /** Enables the bargained replacement that puts one eligible targeted card onto the battlefield. */
    public ReturnTargetCardsFromGraveyardToHandEffect withBargainedBattlefieldReplacement(
            int maxManaValue) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, maxTargets, dynamicMaxTargets, xScaled, exactTargets, minTargets,
                requireSharedCreatureType, maxOnePerCardType, unlessAnyPlayerPaysX,
                opponentChoosesOneForHand, targetGroups, targetGroupsMustShareGraveyard,
                returnToOwnersHand, maxManaValue, recordsReturnedCount, declaresGraveyardTarget);
    }

    /** Returns an equivalent effect without the resolution-time payment clause. */
    public ReturnTargetCardsFromGraveyardToHandEffect withoutAnyPlayerPaysX() {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, maxTargets, dynamicMaxTargets, xScaled, exactTargets, minTargets,
                requireSharedCreatureType, maxOnePerCardType, false, opponentChoosesOneForHand, targetGroups,
                targetGroupsMustShareGraveyard, returnToOwnersHand, bargainedBattlefieldMaxManaValue, recordsReturnedCount, declaresGraveyardTarget);
    }

    public ReturnTargetCardsFromGraveyardToHandEffect withTargetGroups(int... groups) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, maxTargets, dynamicMaxTargets, xScaled, exactTargets, minTargets,
                requireSharedCreatureType, maxOnePerCardType, unlessAnyPlayerPaysX,
                opponentChoosesOneForHand, Arrays.stream(groups).boxed().toList(),
                targetGroupsMustShareGraveyard, returnToOwnersHand, bargainedBattlefieldMaxManaValue, recordsReturnedCount, declaresGraveyardTarget);
    }

    public ReturnTargetCardsFromGraveyardToHandEffect fromSameGraveyard() {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, maxTargets, dynamicMaxTargets, xScaled, exactTargets, minTargets,
                requireSharedCreatureType, maxOnePerCardType, unlessAnyPlayerPaysX,
                opponentChoosesOneForHand, targetGroups, true,
                returnToOwnersHand, bargainedBattlefieldMaxManaValue, recordsReturnedCount, declaresGraveyardTarget);
    }

    public ReturnTargetCardsFromGraveyardToHandEffect toOwnersHands() {
        return new ReturnTargetCardsFromGraveyardToHandEffect(
                filter, maxTargets, dynamicMaxTargets, xScaled, exactTargets, minTargets,
                requireSharedCreatureType, maxOnePerCardType, unlessAnyPlayerPaysX,
                opponentChoosesOneForHand, targetGroups,
                targetGroupsMustShareGraveyard, true, bargainedBattlefieldMaxManaValue, recordsReturnedCount, declaresGraveyardTarget);
    }

    public ReturnTargetCardsFromGraveyardToHandEffect withReturnedCount() {
        return new ReturnTargetCardsFromGraveyardToHandEffect(filter, maxTargets, dynamicMaxTargets, xScaled, exactTargets, minTargets, requireSharedCreatureType, maxOnePerCardType, unlessAnyPlayerPaysX, opponentChoosesOneForHand, targetGroups, targetGroupsMustShareGraveyard, returnToOwnersHand, bargainedBattlefieldMaxManaValue, true, declaresGraveyardTarget);
    }

    public static ReturnTargetCardsFromGraveyardToHandEffect forTriggeredAbility(CardPredicate filter, int maxTargets) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(filter, maxTargets, null, false, false, 0,
                false, Set.of(), false, false, List.of(), false, false, null, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        if (declaresGraveyardTarget) {
            return TargetSpec.benign(TargetPredicates.graveyardCards(
                    filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
        }
        return dynamicMaxTargets == null || upToXTargets()
                ? TargetSpec.NONE
                : TargetSpec.benign(TargetPredicates.player());
    }
    public static ReturnTargetCardsFromGraveyardToHandEffect upToX(CardPredicate filter) {
        return new ReturnTargetCardsFromGraveyardToHandEffect(filter, new com.github.laxika.magicalvibes.model.amount.XValue());
    }

    public boolean upToXTargets() {
        return dynamicMaxTargets instanceof com.github.laxika.magicalvibes.model.amount.XValue;
    }
}
