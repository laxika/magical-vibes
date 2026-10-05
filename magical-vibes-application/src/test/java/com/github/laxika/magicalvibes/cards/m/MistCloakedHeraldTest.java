package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistCloakedHerald.class, RaptorCompanion.class})
class MistCloakedHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("Mist-Cloaked Herald cannot be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new RaptorCompanion());
        Permanent herald = addCreatureReady(player1, new MistCloakedHerald());
        herald.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Herald does not make another attacker unblockable")
    void otherAttackerCanBeBlocked() {
        Permanent herald = addCreatureReady(player1, new MistCloakedHerald());
        Permanent companion = addCreatureReady(player1, new RaptorCompanion());
        Permanent blocker = addCreatureReady(player2, new RaptorCompanion());
        herald.setAttacking(true);
        companion.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Herald can block an opposing creature")
    void canBlock() {
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        Permanent herald = addCreatureReady(player2, new MistCloakedHerald());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(herald.isBlocking()).isTrue();
    }
}
