package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.cards.v.VesselOfNascency;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrafMole.class, ThrabenInspector.class, VesselOfNascency.class})
class GrafMoleTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Clue makes Graf Mole's controller gain 3 life")
    void clueSacrificeGainsThreeLife() {
        harness.addToBattlefield(player1, new GrafMole());
        addClueToken(player1);
        harness.setLibrary(player1, List.of(new GrafMole()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, clueIndex, null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
        harness.assertNotOnBattlefield(player1, "Clue");
    }

    @Test
    @DisplayName("Sacrificing a non-Clue permanent does not make Graf Mole's controller gain life")
    void nonClueSacrificeDoesNotGainLife() {
        harness.addToBattlefield(player1, new GrafMole());

        harness.addToBattlefield(player1, new VesselOfNascency());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        int vesselIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Vessel of Nascency"));
        harness.activateAbility(player1, vesselIndex, null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        harness.assertNotOnBattlefield(player1, "Vessel of Nascency");
    }

    @Test
    @DisplayName("Graf Mole's life gain resolves before the Clue's draw ability")
    void lifeGainUsesTheStackAboveClueAbility() {
        harness.addToBattlefield(player1, new GrafMole());
        addClueToken(player1);
        harness.setLibrary(player1, List.of(new GrafMole()));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Clue"));
        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent sacrificing a Clue does not trigger your Graf Mole")
    void opponentClueSacrificeDoesNotGainLife() {
        harness.addToBattlefield(player1, new GrafMole());
        addClueToken(player2);
        harness.setLibrary(player2, List.of(new GrafMole()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        int clueIndex = gd.playerBattlefields.get(player2.getId()).indexOf(findPermanent(player2, "Clue"));
        harness.activateAbility(player2, clueIndex, null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Clue");
    }

    @Test
    @DisplayName("Each Graf Mole triggers for every Clue sacrificed")
    void multipleMolesTriggerForRepeatedSacrifices() {
        harness.addToBattlefield(player1, new GrafMole());
        harness.addToBattlefield(player1, new GrafMole());
        addClueToken(player1);
        addClueToken(player1);
        harness.setLibrary(player1, List.of(new GrafMole(), new GrafMole()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int sacrifice = 0; sacrifice < 2; sacrifice++) {
            int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Clue"));
            harness.activateAbility(player1, clueIndex, null, null);
            while (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            }
            harness.assertLife(player1, 20 + 6 * (sacrifice + 1));
        }
    }

    private void addClueToken(Player player) {
        harness.enterBattlefieldAndReturn(player, new ThrabenInspector());
        harness.passBothPriorities();
    }
}
