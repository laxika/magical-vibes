package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.cards.t.TropicalIsland;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantShark.class, Squire.class, TropicalIsland.class})
class GiantSharkTest extends BaseCardTest {

    @Test
    @DisplayName("Giant Shark is sacrificed when its controller controls no Islands")
    void sacrificedWithoutIsland() {
        addReadyShark(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Shark");
        harness.assertInGraveyard(player1, "Giant Shark");
    }

    @Test
    @CardUsed(BloodMoon.class)
    @DisplayName("Giant Shark is sacrificed when Blood Moon removes the Island subtype it relies on")
    void sacrificedWhenBloodMoonRemovesIslandSubtype() {
        harness.addToBattlefield(player1, new TropicalIsland());
        addReadyShark(player1);
        harness.addToBattlefield(player1, new BloodMoon());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Shark");
        harness.assertInGraveyard(player1, "Giant Shark");
    }

    @Test
    @DisplayName("Giant Shark survives while its controller controls an Island")
    void survivesWithIsland() {
        harness.addToBattlefield(player1, new TropicalIsland());
        addReadyShark(player1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Shark");
    }

    @Test
    @DisplayName("Giant Shark cannot attack without an Island controlled by the defending player")
    void cannotAttackWithoutDefendingIsland() {
        harness.addToBattlefield(player1, new TropicalIsland());
        Permanent shark = addReadyShark(player1);

        assertThatThrownBy(() -> declareAttackers(List.of(battlefieldIndex(player1, shark))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Giant Shark can attack when the defending player controls an Island")
    void canAttackWithDefendingIsland() {
        harness.addToBattlefield(player1, new TropicalIsland());
        harness.addToBattlefield(player2, new TropicalIsland());
        Permanent shark = addReadyShark(player1);

        declareAttackers(List.of(battlefieldIndex(player1, shark)));

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Giant Shark gets +2/+0 and trample when it blocks a damaged creature")
    void blockingDamagedCreatureGivesBonus() {
        harness.addToBattlefield(player2, new TropicalIsland());
        Permanent shark = addReadyShark(player2);
        Permanent attacker = addCreatureReady(player1, new Squire());
        attacker.setAttacking(true);
        gd.permanentsDealtDamageThisTurn.add(attacker.getId());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                battlefieldIndex(player2, shark), battlefieldIndex(player1, attacker))));
        harness.passBothPriorities();

        assertThat(shark.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, shark, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Giant Shark gets no bonus when it blocks an undamaged creature")
    void blockingUndamagedCreatureGivesNoBonus() {
        harness.addToBattlefield(player2, new TropicalIsland());
        Permanent shark = addReadyShark(player2);
        Permanent attacker = addCreatureReady(player1, new Squire());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                battlefieldIndex(player2, shark), battlefieldIndex(player1, attacker))));
        harness.passBothPriorities();

        assertThat(shark.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, shark, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Giant Shark gets its bonus when it becomes blocked by a damaged creature")
    void becomesBlockedByDamagedCreatureGivesBonus() {
        harness.addToBattlefield(player1, new TropicalIsland());
        harness.addToBattlefield(player2, new TropicalIsland());
        Permanent shark = addReadyShark(player1);
        shark.setAttacking(true);
        shark.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new Squire());
        gd.permanentsDealtDamageThisTurn.add(blocker.getId());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                battlefieldIndex(player2, blocker), battlefieldIndex(player1, shark))));
        harness.passBothPriorities();

        assertThat(shark.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, shark, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addReadyShark(Player player) {
        return addCreatureReady(player, new GiantShark());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
