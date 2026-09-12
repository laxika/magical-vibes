package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmoredGriffin.class, BearCub.class})
class ArmoredGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents Bear Cub from blocking Armored Griffin")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new ArmoredGriffin());
        addCreatureReady(player2, new BearCub());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A creature with flying can block Armored Griffin")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new ArmoredGriffin());
        Permanent blocker = addCreatureReady(player2, new ArmoredGriffin());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Armored Griffin untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent griffin = addCreatureReady(player1, new ArmoredGriffin());

        declareAttackers(List.of(0));

        assertThat(griffin.isTapped()).isFalse();
    }
}
