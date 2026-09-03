package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Breezekeeper;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HulkingCyclops.class, Breezekeeper.class})
class HulkingCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Hulking Cyclops cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent cyclops = addCreatureReady(player2, new HulkingCyclops());

        Permanent attacker = addCreatureReady(player1, new Breezekeeper());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Can still be declared as an attacker")
    void canStillAttack() {
        addCreatureReady(player1, new HulkingCyclops());

        assertThatCode(() -> declareAttackers(List.of(0))).doesNotThrowAnyException();
    }
}
