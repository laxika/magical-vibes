package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({WarTorchGoblin.class})
class WarTorchGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("{R}, sacrifice: deals 2 damage to a blocking creature")
    void sacrificesAndDamagesBlockingCreature() {
        addCreatureReady(player1, new WarTorchGoblin());
        Permanent attacker = addCreatureReady(player1, new WarTorchGoblin());
        Permanent blocker = addCreatureReady(player2, new WarTorchGoblin());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "War-Torch Goblin");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("The ability cannot target a creature that is not blocking")
    void cannotTargetNonblockingCreature() {
        addCreatureReady(player1, new WarTorchGoblin());
        Permanent target = addCreatureReady(player2, new WarTorchGoblin());

        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activation requires red mana")
    void requiresRedMana() {
        addCreatureReady(player1, new WarTorchGoblin());
        addCreatureReady(player1, new WarTorchGoblin());
        Permanent blocker = addCreatureReady(player2, new WarTorchGoblin());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Goblin can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent source = addCreatureReady(player1, new WarTorchGoblin());
        source.tap();
        source.setSummoningSick(true);
        addCreatureReady(player1, new WarTorchGoblin());
        Permanent blocker = addCreatureReady(player2, new WarTorchGoblin());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.assertInGraveyard(player1, "War-Torch Goblin");
        assertThat(blocker.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "War-Torch Goblin");
    }

    @Test
    @DisplayName("The ability can target its controller's blocking creature")
    void canTargetOwnBlockingCreature() {
        addCreatureReady(player1, new WarTorchGoblin());
        Permanent blocker = addCreatureReady(player1, new WarTorchGoblin());
        addCreatureReady(player2, new WarTorchGoblin());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("A blocking Goblin may target itself, but sacrifice makes the target illegal")
    void selfTargetBecomesIllegalAfterPayingSacrificeCost() {
        addCreatureReady(player1, new WarTorchGoblin());
        Permanent blocker = addCreatureReady(player2, new WarTorchGoblin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, null, blocker.getId());

        harness.assertNotOnBattlefield(player2, "War-Torch Goblin");
        harness.assertInGraveyard(player2, "War-Torch Goblin");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
