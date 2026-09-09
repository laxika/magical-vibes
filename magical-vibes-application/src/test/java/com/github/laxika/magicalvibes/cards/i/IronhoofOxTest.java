package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed({IronhoofOx.class, BearCub.class})
class IronhoofOxTest extends BaseCardTest {

    @Test
    @DisplayName("Ironhoof Ox can be blocked by one creature")
    void canBeBlockedByOneCreature() {
        Permanent attacker = addCreatureReady(player1, new IronhoofOx());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new BearCub());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Ironhoof Ox cannot be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new IronhoofOx());
        attacker.setAttacking(true);

        Permanent blockerOne = addCreatureReady(player2, new BearCub());

        Permanent blockerTwo = addCreatureReady(player2, new BearCub());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
        assertThat(blockerOne.isBlocking()).isFalse();
        assertThat(blockerTwo.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Each Ironhoof Ox can be blocked by one creature")
    void eachAttackerCanBeBlockedByOneCreature() {
        Permanent firstAttacker = addCreatureReady(player1, new IronhoofOx());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new IronhoofOx());
        secondAttacker.setAttacking(true);

        Permanent firstBlocker = addCreatureReady(player2, new BearCub());
        Permanent secondBlocker = addCreatureReady(player2, new BearCub());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
