package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * A player chooses a permanent and returns it to its owner's hand. By default the player chooses one of the permanents
 * they control matching {@code filter}: the acting player is the one carried on the entry's {@code targetId} — "that
 * player" (the caster) for spell-cast triggers like Mana Breach — or, when no target is set, the resolving controller
 * (Kefnet the Mindful's "you may return a land you control", wrapped in a {@code MayEffect} to make the return
 * optional; Havengul Skaab's attack trigger). With {@link #candidates()} the resolving controller chooses one of the
 * permanents that subject names instead ({@link #choosingOneOf}: "return one of them", Kaito, Dancing Shadow; "a
 * nonland permanent you own", Phenomenon Investigators). If there is nothing to choose, nothing happens; otherwise the
 * player chooses which one via the shared {@code BounceCreature} choice context. {@code noun} names the permanent kind
 * in the choice prompt ("land", "creature").
 *
 * <p>The return is a RETURN step: it begins with nothing returned and ends when the choice is answered, with the
 * returned permanent and its owner recorded, so "If you do" / "When you do" is a following
 * {@code ConditionalEffect(HappenedThisWay.returned(), ...)} and "if a Desert was returned this way"
 * {@code HappenedThisWay.toA(StepKind.RETURN, filter)}.</p>
 *
 * <p>Exclude the source itself ("return <em>another</em> creature you control") with
 * {@code PermanentNotPredicate(new PermanentIsSourceCardPredicate())} inside the filter.</p>
 *
 * @param controllerChooses when true, the resolving ability's controller chooses a permanent
 *                          controlled by the acting player (Sigil of Sleep)
 * @param player     the acting player when it isn't the entry's: {@code you()} even when the stack entry has an
 *                   unrelated target (Sparkcaster), a player an earlier step chose ("choose a player. They return a
 *                   creature they control", Multiple Choice); {@code null} for the entry's
 * @param candidates when non-null, the permanents the resolving controller chooses among (the filter is then unused)
 */
public record ReturnPermanentControlledByPlayerToHandEffect(PermanentPredicate filter, String noun,
                                                            boolean controllerChooses,
                                                            PlayerSubject player,
                                                            PermanentSubject candidates)
        implements CardEffect {

    public ReturnPermanentControlledByPlayerToHandEffect {
        if (candidates != null && candidates.declaresTarget()) {
            throw new IllegalArgumentException("The permanents to choose among are not targeted: " + candidates);
        }
        if (player != null && player.declaresTarget()) {
            throw new IllegalArgumentException("The acting player is no target of this step: " + player);
        }
    }

    public ReturnPermanentControlledByPlayerToHandEffect(PermanentPredicate filter, String noun) {
        this(filter, noun, false, null, null);
    }

    public ReturnPermanentControlledByPlayerToHandEffect(PermanentPredicate filter, String noun,
                                                         boolean controllerChooses) {
        this(filter, noun, controllerChooses, null, null);
    }

    public static ReturnPermanentControlledByPlayerToHandEffect controller(PermanentPredicate filter,
                                                                            String noun) {
        return byPlayer(Subjects.you(), filter, noun);
    }

    /** The player {@code player} names chooses a permanent they control and returns it. */
    public static ReturnPermanentControlledByPlayerToHandEffect byPlayer(PlayerSubject player,
                                                                          PermanentPredicate filter, String noun) {
        return new ReturnPermanentControlledByPlayerToHandEffect(filter, noun, false, player, null);
    }

    /** The resolving controller chooses one of the permanents {@code candidates} names and returns it. */
    public static ReturnPermanentControlledByPlayerToHandEffect choosingOneOf(PermanentSubject candidates,
                                                                               String noun) {
        return new ReturnPermanentControlledByPlayerToHandEffect(null, noun, false, Subjects.you(), candidates);
    }
}
