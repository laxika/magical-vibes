package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlacksnagBuzzard.class, GrizzlyBears.class, Shock.class})
class BlacksnagBuzzardTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without a +1/+1 counter when no creature died this turn")
    void entersWithoutCounterWhenNoCreatureDied() {
        castBuzzard();

        assertThat(findPermanent(player1, "Blacksnag Buzzard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with a +1/+1 counter when a creature died this turn")
    void entersWithCounterAfterCreatureDeath() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock(), new BlacksnagBuzzard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Blacksnag Buzzard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter is present on entry without resolving a triggered ability")
    void counterIsPresentImmediatelyOnEntry() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock(), new BlacksnagBuzzard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Blacksnag Buzzard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Plot costs one generic and one black mana and does not use the stack")
    void plotExilesFromHandWithoutCasting() {
        BlacksnagBuzzard buzzard = plotBuzzard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(buzzard);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Blacksnag Buzzard");
    }

    @Test
    @DisplayName("A plotted Buzzard cannot be cast on the same turn")
    void cannotCastOnTurnItWasPlotted() {
        BlacksnagBuzzard buzzard = plotBuzzard();

        assertThatThrownBy(() -> harness.castFromExile(player1, buzzard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Blacksnag Buzzard");
    }

    @Test
    @DisplayName("A plotted Buzzard can be cast without mana on a later turn")
    void castFromPlotOnLaterTurn() {
        BlacksnagBuzzard buzzard = plotBuzzard();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.castFromExile(player1, buzzard.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Blacksnag Buzzard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private BlacksnagBuzzard plotBuzzard() {
        BlacksnagBuzzard buzzard = new BlacksnagBuzzard();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(buzzard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        return buzzard;
    }

    private void castBuzzard() {
        harness.setHand(player1, List.of(new BlacksnagBuzzard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
