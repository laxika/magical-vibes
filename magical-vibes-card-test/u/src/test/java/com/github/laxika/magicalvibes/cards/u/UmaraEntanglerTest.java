package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UmaraEntangler.class, Shock.class, GrizzlyBears.class, BoneSaw.class})
class UmaraEntanglerTest extends BaseCardTest {

    private Permanent addEntangler() {
        Permanent entangler = harness.addToBattlefieldAndReturn(player1, new UmaraEntangler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return entangler;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives Umara Entangler +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent entangler = addEntangler();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Prowess")
    void creatureSpellDoesNotPump() {
        Permanent entangler = addEntangler();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Prowess boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent entangler = addEntangler();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(3);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prowess resolves before the spell and multiple casts give cumulative boosts")
    void multipleCastsGiveIndependentBoosts() {
        Permanent entangler = addEntangler();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(2);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(3);
        harness.passBothPriorities();

        endTurn();
        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Prowess")
    void opponentSpellDoesNotPump() {
        Permanent entangler = addEntangler();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving a creature spell leaves the existing Entangler unboosted")
    void creatureSpellDoesNotPumpAfterResolution() {
        Permanent entangler = addEntangler();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a noncreature permanent triggers Prowess")
    void artifactSpellPumps() {
        Permanent entangler = addEntangler();
        harness.setHand(player1, List.of(new BoneSaw()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Bone Saw");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Bone Saw");
        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(2);
    }
}
