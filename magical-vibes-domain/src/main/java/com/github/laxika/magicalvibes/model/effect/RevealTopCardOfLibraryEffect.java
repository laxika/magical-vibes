package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Reveals the top {@code count} cards (one by default) of the library of each player {@code player} names (the
 * controller's own when it names nobody), the count evaluated for each of them. The cards stay on top (CR 701.20b) —
 * no draw, no reorder. The step records them ({@code StepKind.REVEAL}): "those cards" in the steps that follow is
 * {@code Subjects.cardsThisWay(StepKind.REVEAL)}. Pair with a {@code ConditionalEffect} reading the revealed card for
 * an "if it's a land card …" rider (the Deceivers and Paroxysm).
 *
 * <p>{@code lifeGainIfLand} lets the controller gain that much life when the revealed card is a
 * land card (Prophecy); {@code 0} means no life gain (Aven Windreader, the Deceivers). The
 * {@link LifeGainEffect} capability therefore reports {@code Fixed(0)} for every reveal that has no
 * rider, which is a fact and not a score — consumers that ask "is this a life-gain card" must treat
 * a zero amount as "no".
 *
 * @param player         whose top cards are revealed; only {@code targetPlayer()} declares a (benign) player target
 * @param count          how many cards each of those players reveals, evaluated with that player as the controller
 * @param lifeGainIfLand the life the controller gains for a revealed land card
 */
public record RevealTopCardOfLibraryEffect(PlayerSubject player, DynamicAmount count, int lifeGainIfLand)
        implements LifeGainEffect {

    public RevealTopCardOfLibraryEffect(PlayerSubject player, int lifeGainIfLand) {
        this(player, new Fixed(1), lifeGainIfLand);
    }

    /** "Each player reveals the top five cards", "a number of cards from the top of their library equal to …". */
    public static RevealTopCardOfLibraryEffect topCards(PlayerSubject player, DynamicAmount count) {
        return new RevealTopCardOfLibraryEffect(player, count, 0);
    }

    /** Convenience constructor for a reveal with no life-gain rider. */
    public RevealTopCardOfLibraryEffect(PlayerSubject player) {
        this(player, 0);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(lifeGainIfLand);
    }
}
