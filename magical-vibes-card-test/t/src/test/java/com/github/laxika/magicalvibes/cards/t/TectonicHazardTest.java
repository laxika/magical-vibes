package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TectonicHazard.class, FugitiveWizard.class, GrizzlyBears.class})
class TectonicHazardTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each opponent and each creature they control")
    void damagesOpponentsAndTheirCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Kills an opponent's 1/1 creature but not your own")
    void onlyKillsOpponentCreatures() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());

        cast();

        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Damages the opponent even when no creatures are on the battlefield")
    void damagesOpponentOnEmptyBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Tectonic Hazard");
    }

    @Test
    @DisplayName("Damages every opposing creature while sparing every friendly creature")
    void damagesAllOpposingCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownWizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent firstOpponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast();

        assertThat(ownBear.getMarkedDamage()).isZero();
        assertThat(ownWizard.getMarkedDamage()).isZero();
        assertThat(firstOpponentBear.getMarkedDamage()).isEqualTo(1);
        assertThat(secondOpponentBear.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    private void cast() {
        harness.setHand(player1, List.of(new TectonicHazard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
