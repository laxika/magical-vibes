package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreaterSandwurm.class, GrizzlyBears.class, HillGiant.class})
class GreaterSandwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling {2} discards Greater Sandwurm and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new GreaterSandwurm()));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Greater Sandwurm");
        harness.assertInHand(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Greater Sandwurm can't be blocked by a creature with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blockerPerm.setSummoningSick(false);

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new GreaterSandwurm());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Greater Sandwurm can be blocked by a creature with power 3 or greater")
    void canBeBlockedByHighPowerCreature() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        blockerPerm.setSummoningSick(false);

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new GreaterSandwurm());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cycling discards as a cost before the draw resolves")
    void cyclingDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new GreaterSandwurm()));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Greater Sandwurm");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new GreaterSandwurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Greater Sandwurm");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A printed two-power blocker can block after its power increases to three")
    void boostedLowPowerCreatureCanBlock() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setPowerModifier(1);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GreaterSandwurm());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A printed three-power blocker cannot block after its power decreases to two")
    void weakenedHighPowerCreatureCannotBlock() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        blocker.setPowerModifier(-1);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GreaterSandwurm());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
