package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuBuMasterAtArms.class, AlertShuInfantry.class, ShuCavalry.class})
class LuBuMasterAtArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Lu Bu to attack the turn it enters the battlefield")
    void hasteAllowsAttackingImmediately() {
        harness.addToBattlefield(player1, new LuBuMasterAtArms());

        declareAttackers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A creature without horsemanship cannot block Lu Bu")
    void creatureWithoutHorsemanshipCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new AlertShuInfantry());
        Permanent luBu = addCreatureReady(player1, new LuBuMasterAtArms());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(luBu)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("A creature with horsemanship can block Lu Bu")
    void creatureWithHorsemanshipCanBlock() {
        Permanent blocker = addCreatureReady(player2, new ShuCavalry());
        Permanent luBu = addCreatureReady(player1, new LuBuMasterAtArms());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(luBu))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Lu Bu can block a creature without horsemanship the turn he enters")
    void canBlockCreatureWithoutHorsemanshipImmediately() {
        addCreatureReady(player1, new AlertShuInfantry());
        Permanent luBu = harness.addToBattlefieldAndReturn(player2, new LuBuMasterAtArms());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(luBu.isBlocking()).isTrue();
        harness.assertLife(player2, 20);
    }
}
