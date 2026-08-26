package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WarTorchGoblin.class, GrizzlyBears.class})
class WarTorchGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("{R}, sacrifice: deals 2 damage to a blocking creature")
    void sacrificesAndDamagesBlockingCreature() {
        addCreatureReady(player1, new WarTorchGoblin());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "War-Torch Goblin");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("The ability cannot target a creature that is not blocking")
    void cannotTargetNonblockingCreature() {
        addCreatureReady(player1, new WarTorchGoblin());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
