package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DisorderTest extends BaseCardTest {

    private void castDisorder() {
        harness.setHand(player1, List.of(new Disorder()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 2 damage to white creatures, leaving non-white creatures unharmed")
    void damagesWhiteCreaturesOnly() {
        harness.addToBattlefield(player2, new EliteVanguard()); // 2/1 white
        harness.addToBattlefield(player2, new GrizzlyBears());   // 2/2 green

        castDisorder();

        GameData gd = harness.getGameData();
        // The 2/1 white creature dies; the green creature is untouched.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Deals 2 damage to each player controlling a white creature; others untouched")
    void damagesControllersOfWhiteCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears()); // green — controller safe
        harness.addToBattlefield(player2, new SerraAngel());   // 4/4 white — controller takes 2

        castDisorder();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // 4/4 white survives 2 damage.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Controller still takes damage even if their only white creature dies (simultaneous)")
    void controllerDamagedEvenWhenWhiteCreatureDies() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new EliteVanguard()); // 2/1 white, dies to the 2 damage

        castDisorder();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"));
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        castDisorder();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Disorder"));
    }
}
