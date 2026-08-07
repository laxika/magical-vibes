package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LiegeOfTheHollowsTest extends BaseCardTest {

    /** Wraths the board so Liege of the Hollows dies and its ON_DEATH trigger goes on the stack. */
    private void killLiege() {
        harness.addToBattlefield(player1, new LiegeOfTheHollows());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private long squirrelCount(Player player) {
        return countPermanents(player, "Squirrel");
    }

    @Test
    @DisplayName("Each player pays any amount of mana and creates that many Squirrels")
    void eachPlayerPaysManaForSquirrels() {
        killLiege();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.passBothPriorities(); // death trigger resolves, prompting each player in turn

        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(squirrelCount(player1)).isEqualTo(3);
        assertThat(squirrelCount(player2)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Paying zero creates no Squirrels and leaves the mana in the pool")
    void payingZeroCreatesNoSquirrels() {
        killLiege();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(squirrelCount(player1)).isZero();
        assertThat(squirrelCount(player2)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("A player with no available mana is skipped without a prompt")
    void playerWithNoManaIsSkipped() {
        killLiege();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(squirrelCount(player1)).isEqualTo(2);
        assertThat(squirrelCount(player2)).isZero();
    }

    @Test
    @DisplayName("With no mana available at all, the trigger resolves with no prompts and no Squirrels")
    void noManaAnywhereCreatesNoSquirrels() {
        killLiege();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(squirrelCount(player1)).isZero();
        assertThat(squirrelCount(player2)).isZero();
    }
}
