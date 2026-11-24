package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;
import java.util.Set;

/**
 * "[Permanents] enter tapped": a replacement effect on how a matching permanent enters (CR 614.1c), judged with the
 * permanent as it would exist on the battlefield (CR 614.12). Matches by {@code cardTypes} and/or {@code filter}; a
 * filter is judged with this ability's source as the predicates' source ("each creature without mana value of the
 * chosen quality", Ashling's Prerogative).
 *
 * <p>As a static ability it applies to a permanent entering under the control of a player {@code players} names, judged
 * against the source and its controller ({@code SubjectResolutionService.namesPlayer}): {@code eachPlayer()} ("creatures
 * enter tapped"), {@code eachOpponent()} ("creatures your opponents control enter tapped"), {@code enchantedPlayer()}
 * ("creatures enchanted player controls enter tapped", Radiant Restraints). Floated on a player by
 * {@link GrantStaticEffectToPlayerEffect} ("permanents enter tapped this turn", Due Respect, over each player; "lands
 * you control enter tapped this turn", Nahiri's Lithoforming) it applies to the permanents entering under that
 * player's control for its duration, {@code players} being unread; being a rule change, it covers permanents that enter
 * after it began (CR 611.2c). Read by {@code BattlefieldPlacementService}.
 *
 * @param cardTypes the card types that enter tapped (empty with a filter)
 * @param players   whose entering permanents it applies to; every player by default
 * @param filter    the permanents that enter tapped, or {@code null} to match by {@code cardTypes} alone
 * @param castOnly  only permanents that were cast (Uphill Battle's "creatures played by your opponents")
 */
public record EnterPermanentsOfTypesTappedEffect(
        Set<CardType> cardTypes,
        PlayerSubject players,
        PermanentPredicate filter,
        boolean castOnly
) implements CardEffect {

    public EnterPermanentsOfTypesTappedEffect {
        Objects.requireNonNull(cardTypes, "cardTypes");
        Objects.requireNonNull(players, "players");
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("An enters-tapped replacement takes no target player");
        }
    }

    public EnterPermanentsOfTypesTappedEffect(Set<CardType> cardTypes, boolean opponentsOnly,
                                             PermanentPredicate filter, boolean castOnly) {
        this(cardTypes, playersOf(opponentsOnly), filter, castOnly);
    }

    public EnterPermanentsOfTypesTappedEffect(Set<CardType> cardTypes, boolean opponentsOnly,
                                             PermanentPredicate filter) {
        this(cardTypes, playersOf(opponentsOnly), filter, false);
    }

    public EnterPermanentsOfTypesTappedEffect(Set<CardType> cardTypes) {
        this(cardTypes, Subjects.eachPlayer(), null, false);
    }

    public EnterPermanentsOfTypesTappedEffect(Set<CardType> cardTypes, boolean opponentsOnly) {
        this(cardTypes, playersOf(opponentsOnly), null, false);
    }

    public EnterPermanentsOfTypesTappedEffect(Set<CardType> cardTypes, PlayerSubject players) {
        this(cardTypes, players, null, false);
    }

    /** Permanents matching {@code filter} enter tapped (optionally opponents-only). */
    public static EnterPermanentsOfTypesTappedEffect matching(PermanentPredicate filter, boolean opponentsOnly) {
        return matching(filter, playersOf(opponentsOnly));
    }

    /** Permanents matching {@code filter} that enter under the control of a player {@code players} names enter tapped. */
    public static EnterPermanentsOfTypesTappedEffect matching(PermanentPredicate filter, PlayerSubject players) {
        return new EnterPermanentsOfTypesTappedEffect(Set.of(), players, filter, false);
    }

    /**
     * Whether it applies to every player's permanents and only by card type, so a batch of entering permanents can take
     * it once before the first enters ({@code BattlefieldPlacementService.snapshotEnterTappedTypes}).
     */
    public boolean everyPlayersByTypeAlone() {
        return filter == null && !castOnly && Subjects.eachPlayer().equals(players);
    }

    private static PlayerSubject playersOf(boolean opponentsOnly) {
        return opponentsOnly ? Subjects.eachOpponent() : Subjects.eachPlayer();
    }
}
