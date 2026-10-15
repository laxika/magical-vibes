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

        addCreatureReady(player1, new ShuEliteCompanions());

        addCreatureReady(player2, new ShuEliteCompanions());
        addCreatureReady(player2, new ShuEliteCompanions());

        declareAttackersAndPrepareBlockers(List.of(1));

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

        addCreatureReady(player1, new ShuEliteCompanions());

        Permanent blocker = addCreatureReady(player2, new ShuEliteCompanions());

        declareAttackersAndPrepareBlockers(List.of(1));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Yuan Shao itself can't be blocked by two creatures")
    void yuanShaoItselfCannotBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new YuanShaoTheIndecisive());

        addCreatureReady(player2, new ShuEliteCompanions());
        addCreatureReady(player2, new ShuEliteCompanions());

        declareAttackersAndPrepareBlockers(List.of(0));

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

        addCreatureReady(player2, new ShuEliteCompanions());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

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

    @Test
    @DisplayName("Yuan Shao can be blocked by one creature with horsemanship")
    void canBeBlockedByOneCreatureWithHorsemanship() {
        addCreatureReady(player1, new YuanShaoTheIndecisive());
        Permanent blocker = addCreatureReady(player2, new ShuEliteCompanions());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The team blocking restriction ends when Yuan Shao leaves the battlefield")
    void restrictionEndsWhenYuanShaoLeavesBattlefield() {
        Permanent yuanShao = addCreatureReady(player1, new YuanShaoTheIndecisive());
        addCreatureReady(player1, new ShuEliteCompanions());
        Permanent blockerOne = addCreatureReady(player2, new ShuEliteCompanions());
        Permanent blockerTwo = addCreatureReady(player2, new ShuEliteCompanions());

        declareAttackersAndPrepareBlockers(List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(yuanShao);
        gd.playerGraveyards.get(player1.getId()).add(yuanShao.getCard());

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }
}
