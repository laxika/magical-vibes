package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackWaltzNo3.class, GrizzlyBears.class, Shock.class})
class BlackWaltzNo3Test extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell deals 2 damage to each opponent")
    void noncreatureSpellDealsDamage() {
        harness.addToBattlefield(player1, new BlackWaltzNo3());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Black Waltz No. 3")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new BlackWaltzNo3());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Black Waltz No. 3")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new BlackWaltzNo3());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        gs.passPriority(gd, player1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each noncreature spell triggers independently")
    void repeatedSpellsEachTrigger() {
        harness.addToBattlefield(player1, new BlackWaltzNo3());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The trigger still deals damage after its source is destroyed in response")
    void triggerSurvivesSourceRemoval() {
        var source = harness.addToBattlefieldAndReturn(player1, new BlackWaltzNo3());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        gs.passPriority(gd, player1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
