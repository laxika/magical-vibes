package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BasilicaScreecher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Jump;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmogElemental.class, AirElemental.class, GrizzlyBears.class, BasilicaScreecher.class, Jump.class})
class SmogElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's flying creatures get -1/-1")
    void debuffsOpponentFliers() {
        harness.addToBattlefield(player1, new SmogElemental());
        Permanent air = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, air)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, air)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's non-flying creatures are unaffected")
    void ignoresOpponentGroundCreatures() {
        harness.addToBattlefield(player1, new SmogElemental());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Own flying creatures are unaffected, including Smog Elemental itself")
    void ignoresOwnFliers() {
        harness.addToBattlefield(player1, new SmogElemental());
        harness.addToBattlefield(player1, new AirElemental());

        Permanent air = findPermanent(player1, "Air Elemental");
        Permanent smog = findPermanent(player1, "Smog Elemental");

        assertThat(gqs.getEffectivePower(gd, air)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, air)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, smog)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, smog)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two Smog Elementals stack to -2/-2")
    void twoCopiesStack() {
        harness.addToBattlefield(player1, new SmogElemental());
        harness.addToBattlefield(player1, new SmogElemental());
        Permanent air = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, air)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, air)).isEqualTo(2);
    }

    @Test
    @DisplayName("Penalty applies on resolve and is removed when Smog Elemental leaves")
    void penaltyAppliesOnResolveAndEndsOnLeave() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new SmogElemental()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        Permanent air = findPermanent(player2, "Air Elemental");
        assertThat(gqs.getEffectivePower(gd, air)).isEqualTo(4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, air)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Smog Elemental"));

        assertThat(gqs.getEffectivePower(gd, air)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, air)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Smog Elementals put a two-toughness opposing flyer into the graveyard")
    void reducedToughnessKillsOpponentFlyer() {
        harness.addToBattlefield(player1, new SmogElemental());
        harness.addToBattlefield(player2, new BasilicaScreecher());
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player2, "Basilica Screecher");

        harness.setHand(player1, List.of(new SmogElemental()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Basilica Screecher");
        harness.assertInGraveyard(player2, "Basilica Screecher");
    }

    @Test
    @DisplayName("The penalty follows flying gained and lost during play")
    void penaltyTracksTemporaryFlying() {
        harness.addToBattlefield(player1, new SmogElemental());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Jump()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
