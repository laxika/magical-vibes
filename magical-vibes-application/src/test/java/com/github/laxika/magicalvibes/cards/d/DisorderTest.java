package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EagerCadet;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Disorder.class, EagerCadet.class, GloriousAnthem.class, GrizzlyBears.class, SerraAngel.class})
class DisorderTest extends BaseCardTest {

    private void castDisorder() {
        harness.setHand(player1, List.of(new Disorder()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Deals 2 damage to white creatures, leaving non-white creatures unharmed")
    void damagesWhiteCreaturesOnly() {
        harness.addToBattlefield(player2, new EagerCadet()); // 1/1 white
        harness.addToBattlefield(player2, new GrizzlyBears());   // 2/2 green

        castDisorder();

        // The 1/1 white creature dies; the green creature is untouched.
        harness.assertNotOnBattlefield(player2, "Eager Cadet");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 2 damage to each player controlling a white creature; others untouched")
    void damagesControllersOfWhiteCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears()); // green — controller safe
        harness.addToBattlefield(player2, new SerraAngel());   // 4/4 white — controller takes 2

        castDisorder();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        // 4/4 white survives 2 damage.
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Controller still takes damage even if their only white creature dies (simultaneous)")
    void controllerDamagedEvenWhenWhiteCreatureDies() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new EagerCadet()); // 1/1 white, dies to the 2 damage

        castDisorder();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Eager Cadet");
    }

    @Test
    @DisplayName("Ignores white noncreature permanents")
    void ignoresWhiteNoncreaturePermanents() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GloriousAnthem());

        castDisorder();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        castDisorder();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Disorder");
    }
}
