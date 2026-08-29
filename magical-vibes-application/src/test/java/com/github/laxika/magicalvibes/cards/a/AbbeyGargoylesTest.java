package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FireDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
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

@CardUsed({AbbeyGargoyles.class, FireDrake.class, GrizzlyBears.class, Incinerate.class, Terror.class,
        ZephyrFalcon.class})
class AbbeyGargoylesTest extends BaseCardTest {

    @Test
    @DisplayName("Red creature cannot block Abbey Gargoyles")
    void redCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new AbbeyGargoyles());
        attacker.setAttacking(true);

        // Fire Drake has flying, so protection from red rather than flying is what stops the block.
        addCreatureReady(player2, new FireDrake());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-red creature can block Abbey Gargoyles")
    void nonRedCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new AbbeyGargoyles());
        attacker.setAttacking(true);

        // Zephyr Falcon has flying, and protection from red does not apply to its blue color.
        Permanent blocker = addCreatureReady(player2, new ZephyrFalcon());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Abbey Gargoyles takes no combat damage from red creature")
    void takesNoDamageFromRed() {
        Permanent attacker = addCreatureReady(player1, new FireDrake());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AbbeyGargoyles());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Without protection, the combat damage would be marked even though it is not lethal.
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Abbey Gargoyles");
    }

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent gargoyles = addCreatureReady(player2, new AbbeyGargoyles());

        // Keep another creature available so the targeted spell has a legal target in the game.
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, gargoyles.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by black instant")
    void canBeTargetedByBlackInstant() {
        Permanent gargoyles = addCreatureReady(player1, new AbbeyGargoyles());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, gargoyles.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Terror");
    }
}
