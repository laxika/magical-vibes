package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.h.HuangZhongShuGeneral;
import com.github.laxika.magicalvibes.cards.s.ShuEliteCompanions;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YuanShaoTheIndecisive.class, ShuEliteCompanions.class, HuangZhongShuGeneral.class})
class YuanShaoTheIndecisiveTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature you control can't be blocked by two creatures while Yuan Shao is out")
    void otherCreatureCannotBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new YuanShaoTheIndecisive());

        Permanent attacker = addCreatureReady(player1, new ShuEliteCompanions());
        attacker.setAttacking(true);

        addCreatureReady(player2, new ShuEliteCompanions());
        addCreatureReady(player2, new ShuEliteCompanions());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("A single blocker is legal while Yuan Shao is out")
    void canBeBlockedByOneCreature() {
        addCreatureReady(player1, new YuanShaoTheIndecisive());

        Permanent attacker = addCreatureReady(player1, new ShuEliteCompanions());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ShuEliteCompanions());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Yuan Shao itself can't be blocked by two creatures")
    void yuanShaoItselfCannotBeBlockedByTwoCreatures() {
        Permanent yuanShao = addCreatureReady(player1, new YuanShaoTheIndecisive());
        yuanShao.setAttacking(true);

        addCreatureReady(player2, new ShuEliteCompanions());
        addCreatureReady(player2, new ShuEliteCompanions());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Yuan Shao does not restrict creatures controlled by an opponent")
    void opponentCreatureCanBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new YuanShaoTheIndecisive());
        Permanent blockerOne = addCreatureReady(player1, new ShuEliteCompanions());
        Permanent blockerTwo = addCreatureReady(player1, new ShuEliteCompanions());

        Permanent attacker = addCreatureReady(player2, new ShuEliteCompanions());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Yuan Shao cannot be blocked by a creature without horsemanship")
    void cannotBeBlockedByCreatureWithoutHorsemanship() {
        addCreatureReady(player1, new YuanShaoTheIndecisive());
        addCreatureReady(player2, new HuangZhongShuGeneral());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(horsemanship)");
    }
}
