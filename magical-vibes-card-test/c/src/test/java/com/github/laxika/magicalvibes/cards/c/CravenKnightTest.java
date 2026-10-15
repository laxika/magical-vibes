package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CravenKnight.class, GrizzlyBears.class})
class CravenKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Craven Knight cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new CravenKnight());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("A different creature can still be declared as a blocker")
    void differentCreatureCanBeDeclaredAsBlocker() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Craven Knight can attack and be blocked while another Craven Knight cannot block")
    void canAttackAndBeBlockedWithoutPreventingOtherCreaturesFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new CravenKnight());
        Permanent defendingKnight = addCreatureReady(player2, new CravenKnight());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(defendingKnight.isBlocking()).isFalse();
    }
}
