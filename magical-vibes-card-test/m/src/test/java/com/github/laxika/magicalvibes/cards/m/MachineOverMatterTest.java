package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.p.PhalanxVanguard;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SpotterThopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MachineOverMatter.class, EnergyRefractor.class, PhalanxVanguard.class,
        Island.class, SpotterThopter.class})
class MachineOverMatterTest extends BaseCardTest {

    @Test
    void returnsTargetNonlandPermanentToItsOwnersHand() {
        harness.addToBattlefield(player2, new PhalanxVanguard());
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Phalanx Vanguard");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Phalanx Vanguard");
        harness.assertInHand(player2, "Phalanx Vanguard");
    }

    @Test
    void costsOneLessWithAnArtifactCreature() {
        harness.addToBattlefield(player1, new SpotterThopter());
        harness.addToBattlefield(player2, new PhalanxVanguard());
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Phalanx Vanguard");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void noncreatureArtifactDoesNotReduceCost() {
        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.addToBattlefield(player2, new PhalanxVanguard());
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Phalanx Vanguard");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Island");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void opponentsArtifactCreatureDoesNotReduceCost() {
        harness.addToBattlefield(player2, new SpotterThopter());
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Spotter Thopter");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void canReturnTheArtifactCreatureThatReducesItsCost() {
        harness.addToBattlefield(player1, new SpotterThopter());
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Spotter Thopter");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Spotter Thopter");
        harness.assertInHand(player1, "Spotter Thopter");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void multipleArtifactCreaturesDoNotReduceTheBlueManaRequirement() {
        harness.addToBattlefield(player1, new SpotterThopter());
        harness.addToBattlefield(player1, new SpotterThopter());
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Spotter Thopter");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void separateArtifactAndCreatureDoNotSatisfyTheReduction() {
        harness.addToBattlefield(player1, new EnergyRefractor());
        harness.addToBattlefield(player1, new PhalanxVanguard());
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Phalanx Vanguard");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void canReturnANoncreatureArtifact() {
        harness.addToBattlefield(player2, new EnergyRefractor());
        harness.setHand(player1, List.of(new MachineOverMatter()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Energy Refractor");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Energy Refractor");
        harness.assertInHand(player2, "Energy Refractor");
    }
}
