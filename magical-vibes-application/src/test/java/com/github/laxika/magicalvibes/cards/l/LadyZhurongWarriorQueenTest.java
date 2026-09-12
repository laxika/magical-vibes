package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LadyZhurongWarriorQueen.class, ShuFootSoldiers.class})
class LadyZhurongWarriorQueenTest extends BaseCardTest {

    @Test
    @DisplayName("A creature without horsemanship cannot block Lady Zhurong")
    void creatureWithoutHorsemanshipCannotBlock() {
        addCreatureReady(player1, new LadyZhurongWarriorQueen());
        addCreatureReady(player2, new ShuFootSoldiers());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(horsemanship)");
    }

    @Test
    @DisplayName("A creature with horsemanship can block Lady Zhurong")
    void creatureWithHorsemanshipCanBlock() {
        addCreatureReady(player1, new LadyZhurongWarriorQueen());
        Permanent blocker = addCreatureReady(player2, new LadyZhurongWarriorQueen());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
