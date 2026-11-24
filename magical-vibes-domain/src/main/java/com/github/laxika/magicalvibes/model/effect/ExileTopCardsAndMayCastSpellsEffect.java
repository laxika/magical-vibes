package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

/**
 * Exiles cards from a library, then lets the controller cast any number of the exiled spells
 * without paying their mana costs. The cast choices are made during resolution; uncast cards
 * normally remain exiled, or return to the owner's library bottom in random order when requested.
 * They may alternatively be put into their owners' hands after the cast choices are complete.
 *
 * <p>When {@code playLands} is true, exiled lands are offered too and are played during resolution
 * (Gix, Yawgmoth Praetor), still only on the controller's turn and only while they have a land play
 * left.
 */
public record ExileTopCardsAndMayCastSpellsEffect(
        int count,
        DynamicAmount dynamicCount,
        PlayerSubject player,
        boolean trackWithSource,
        boolean faceDown,
        DynamicAmount manaValueLimit,
        CardPredicate castFilter,
        int maxCastCount,
        boolean putUncastCardsOnBottomRandom,
        boolean putUncastCardsIntoHand,
        CardPredicate uncastCardsToHandFilter,
        Integer totalManaValueLimit,
        boolean normalCost,
        boolean playLands
) implements CardEffect {

    public ExileTopCardsAndMayCastSpellsEffect(int count, DynamicAmount dynamicCount, PlayerSubject player,
                                               boolean trackWithSource, boolean faceDown,
                                               DynamicAmount manaValueLimit, CardPredicate castFilter,
                                               int maxCastCount,
                                               boolean putUncastCardsOnBottomRandom, boolean putUncastCardsIntoHand,
                                               CardPredicate uncastCardsToHandFilter, Integer totalManaValueLimit) {
        this(count, dynamicCount, player, trackWithSource, faceDown, manaValueLimit, castFilter, maxCastCount,
                putUncastCardsOnBottomRandom, putUncastCardsIntoHand,
                uncastCardsToHandFilter, totalManaValueLimit, false, false);
    }

    /** Exiles cards and allows each to be played immediately while paying its normal costs. */
    public static ExileTopCardsAndMayCastSpellsEffect controllerMayPlayWithNormalCost(int count) {
        return new ExileTopCardsAndMayCastSpellsEffect(count, null, Subjects.you(), false, false,
                null, null, Integer.MAX_VALUE, false, false, null, null, true, false);
    }

    /** Exiles {@code count} cards from a targeted opponent, then lets the controller play any number
     * of the exiled lands and spells without paying their mana costs. */
    public static ExileTopCardsAndMayCastSpellsEffect targetedOpponentPlayLandsAndSpells(DynamicAmount count) {
        return new ExileTopCardsAndMayCastSpellsEffect(0, count, Subjects.targetPlayer(), false, false,
                null, null, Integer.MAX_VALUE, false, false, null, null, false, true);
    }

    /** "Exile [that many] cards from the top [of your library]. You may play up to {@code maxPlayCount} of those cards
     * without paying [its] mana cost": lands among them are played during resolution, on the controller's turn with a
     * land play left (CR 305.2, 305.3). */
    public static ExileTopCardsAndMayCastSpellsEffect controllerMayPlayUpTo(DynamicAmount count, int maxPlayCount) {
        return new ExileTopCardsAndMayCastSpellsEffect(0, count, Subjects.you(), false, false,
                null, null, maxPlayCount, false, false, null, null, false, true);
    }

    /** Exiles the top {@code count} cards of the controller's library. */
    public ExileTopCardsAndMayCastSpellsEffect(int count) {
        this(count, null, Subjects.you(), false, false, null, null, Integer.MAX_VALUE,
                false, false, null, null);
    }

    /** Exiles cards from a combat-damaged opponent's library and tracks them with the source. */
    public ExileTopCardsAndMayCastSpellsEffect(DynamicAmount dynamicCount, PlayerSubject player,
                                               boolean trackWithSource,
                                               DynamicAmount manaValueLimit) {
        this(0, dynamicCount, player, trackWithSource, false, manaValueLimit, null,
                Integer.MAX_VALUE, false, false, null, null);
    }

    /** Exiles cards and offers only cards matching {@code castFilter} for free casting. */
    public ExileTopCardsAndMayCastSpellsEffect(DynamicAmount dynamicCount, PlayerSubject player,
                                               boolean trackWithSource,
                                               DynamicAmount manaValueLimit,
                                               CardPredicate castFilter) {
        this(0, dynamicCount, player, trackWithSource, false, manaValueLimit, castFilter,
                Integer.MAX_VALUE, false, false, null, null);
    }

    /** Exiles the controller's top cards and caps the free-cast offer. */
    public ExileTopCardsAndMayCastSpellsEffect(int count, DynamicAmount manaValueLimit,
                                               CardPredicate castFilter, int maxCastCount,
                                               boolean putUncastCardsOnBottomRandom) {
        this(count, null, Subjects.you(), false, false, manaValueLimit, castFilter,
                maxCastCount, putUncastCardsOnBottomRandom, false, null, null);
    }

    /** Exiles the controller's top cards face down with this source and offers matching spells. */
    public static ExileTopCardsAndMayCastSpellsEffect controllerTrackedFaceDown(
            int count, CardPredicate castFilter, int maxCastCount) {
        return new ExileTopCardsAndMayCastSpellsEffect(
                count, null, Subjects.you(), true, true, null, castFilter,
                maxCastCount, false, false, null, null);
    }

    /** Exiles a fixed number from a targeted opponent and caps the number of free casts. */
    public static ExileTopCardsAndMayCastSpellsEffect targetedOpponent(int count, int maxCastCount) {
        return new ExileTopCardsAndMayCastSpellsEffect(
                count, null, Subjects.targetPlayer(), true, false, null, null,
                maxCastCount, false, false, null, null);
    }

    /** Exiles cards from the controller's library and randomly bottoms every card not cast. */
    public static ExileTopCardsAndMayCastSpellsEffect controllerWithRandomBottom(
            int count, DynamicAmount manaValueLimit, CardPredicate castFilter, int maxCastCount) {
        return new ExileTopCardsAndMayCastSpellsEffect(
                count, null, Subjects.you(), false, false, manaValueLimit, castFilter,
                maxCastCount, true, false, null, null);
    }

    /** Exiles the controller's top cards and puts every card not cast into its owner's hand. */
    public static ExileTopCardsAndMayCastSpellsEffect controllerWithRestToHand(int count) {
        return new ExileTopCardsAndMayCastSpellsEffect(
                count, null, Subjects.you(), false, false, null, null,
                Integer.MAX_VALUE, false, true, null, null);
    }

    /** Exiles the controller's top cards, offers one matching spell for free, then puts
     * uncast matching cards into hand and every other uncast card on the library bottom randomly. */
    public static ExileTopCardsAndMayCastSpellsEffect controllerWithRandomBottomAndMatchingRestToHand(
            DynamicAmount count, DynamicAmount manaValueLimit, CardPredicate castFilter,
            CardPredicate uncastCardsToHandFilter) {
        return new ExileTopCardsAndMayCastSpellsEffect(
                0, count, Subjects.you(), false, false, manaValueLimit, castFilter,
                1, true, true, uncastCardsToHandFilter, null);
    }

    /** Exiles the controller's top cards and limits the total mana value of free casts. */
    public static ExileTopCardsAndMayCastSpellsEffect controllerWithTotalManaValue(
            int count, int totalManaValueLimit) {
        return new ExileTopCardsAndMayCastSpellsEffect(
                count, null, Subjects.you(), false, false,
                new Fixed(totalManaValueLimit), null, Integer.MAX_VALUE,
                false, false, null, totalManaValueLimit);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.harmful(TargetPredicates.players(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT)))
                : TargetSpec.NONE;
    }
}
