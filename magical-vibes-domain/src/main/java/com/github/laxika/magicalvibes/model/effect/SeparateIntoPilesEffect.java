package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Separate all creatures target player controls into two piles ... the pile of that player's choice": as the effect
 * resolves its controller puts each of the objects into one of two piles (CR 700.3a; a pile may be empty,
 * CR 700.3d), then a player chooses one of the piles. A CHOOSE step that pauses at its end on the choices:
 * {@code Subjects.thisWay(StepKind.CHOOSE)} (or {@code cardsThisWay(StepKind.CHOOSE)}) is the chosen pile, and the
 * other pile is the separated set {@code except(…)} it. Grouping objects into piles moves none of them (CR 700.3c).
 *
 * <p>{@link #permanents}: all permanents a {@code controlledBy(players, filter)} set names. The permanents each of
 * those players controls are separated on their own and that player chooses one of their piles ("for each defending
 * player, separate all creatures that player controls into two piles and that player chooses one", Stand or Fall);
 * the chosen piles are the step's result together. A player with none is skipped. With a {@code targetPlayer()} set,
 * this effect declares that player target (Do or Die, Liliana of the Veil).
 *
 * <p>{@link #cards}: the cards a graveyard subject names, chosen between by {@code chooser} ("exile the pile of an
 * opponent's choice", Death or Glory, after {@code ChoosePlayerEffect.opponent()}).
 *
 * @param permanents which permanents are separated, a {@code controlledBy} set; {@code null} for cards
 * @param cards      which graveyard cards are separated; {@code null} for permanents
 * @param chooser    who chooses a pile of cards; {@code null} for permanents, whose controller chooses
 * @param label      what is separated, for the prompts and the log ("creatures", "creature cards")
 */
public record SeparateIntoPilesEffect(PermanentSubject permanents, CardSubject cards, PlayerSubject chooser,
                                      String label) implements CardEffect {

    public SeparateIntoPilesEffect {
        Objects.requireNonNull(label, "label");
        if ((permanents == null) == (cards == null)) {
            throw new IllegalArgumentException("Piles are made of permanents or of cards");
        }
        if (permanents != null && (!(permanents instanceof PermanentSubject.ControlledBy) || chooser != null)) {
            throw new IllegalArgumentException("The permanents a set of players controls are separated, each "
                    + "player choosing their own pile: " + permanents);
        }
        if (cards != null && (!(cards instanceof CardSubject.InGraveyards) || cards.declaresTarget()
                || chooser == null || chooser.declaresTarget())) {
            throw new IllegalArgumentException("Graveyard cards nothing targets are separated and a player who is "
                    + "no target chooses: " + cards + ", " + chooser);
        }
    }

    /** "Separate all [filter] [players] control into two piles"; each of those players chooses one of theirs. */
    public static SeparateIntoPilesEffect permanents(PermanentSubject permanents, String label) {
        return new SeparateIntoPilesEffect(Objects.requireNonNull(permanents, "permanents"), null, null, label);
    }

    /** "Separate all [filter] cards in [graveyards] into two piles"; {@code chooser} chooses one. */
    public static SeparateIntoPilesEffect cards(CardSubject cards, PlayerSubject chooser, String label) {
        return new SeparateIntoPilesEffect(null, Objects.requireNonNull(cards, "cards"),
                Objects.requireNonNull(chooser, "chooser"), label);
    }

    /** A {@code targetPlayer()} set declares that player target. */
    @Override
    public TargetSpec targetSpec() {
        return permanents != null && permanents.declaresPlayerTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
