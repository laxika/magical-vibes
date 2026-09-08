package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Maro;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmplifireTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals until a creature and sets Amplifire's base power and toughness to twice its P/T")
    void revealsUntilCreatureAndSetsBasePowerToughness() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, bears));

        advanceToUpkeepAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, amplifire)).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(shock, bears);
    }

    @Test
    @DisplayName("Returns the whole revealed library to the bottom when no creature is found")
    void noCreatureLeavesBasePowerToughnessUnchanged() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));

        advanceToUpkeepAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, amplifire)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Uses a creature card's characteristic-defining power and toughness")
    void usesCharacteristicDefiningPowerToughness() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Maro()));

        advanceToUpkeepAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, amplifire)).isEqualTo(6);
    }

    @Test
    @DisplayName("The base power and toughness setting expires at the beginning of your next turn")
    void basePowerToughnessExpiresAtNextTurn() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeepAndResolve(player1);
        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(4);

        gd.playerDecks.get(player1.getId()).clear();
        endTurn(player1);
        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(4);

        endTurn(player2);
        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, amplifire)).isEqualTo(1);
    }

    private void advanceToUpkeepAndResolve(Player player) {
        advanceToUpkeep(player);
        harness.passBothPriorities();
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        for (int step = 0; step < 10 && activePlayer.getId().equals(gd.activePlayerId); step++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
