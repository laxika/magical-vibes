package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlmsBeast.class, DiscipleOfTheOldWays.class})
class AlmsBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Every blocker of Alms Beast gains lifelink")
    void multipleBlockersGainLifelink() {
        Permanent beast = addCreatureReady(player1, new AlmsBeast());
        Permanent first = addCreatureReady(player2, new DiscipleOfTheOldWays());
        Permanent second = addCreatureReady(player2, new DiscipleOfTheOldWays());

        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isFalse();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, beast, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Two Alms Beasts in combat grant lifelink to each other")
    void opposingBeastsGrantEachOtherLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new AlmsBeast());
        Permanent blocker = addCreatureReady(player2, new AlmsBeast());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.LIFELINK)).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("A creature blocking Alms Beast has lifelink; the Beast itself does not")
    void blockerGainsLifelinkAndItsControllerGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent beast = addCreatureReady(player1, new AlmsBeast());
        Permanent blocker = addCreatureReady(player2, new DiscipleOfTheOldWays());
        Permanent bystander = addCreatureReady(player2, new DiscipleOfTheOldWays());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(beast)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(beast))));

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bystander, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, beast, Keyword.LIFELINK)).isFalse();

        harness.passBothPriorities();

        // The blocker's 2 combat damage is lifelinked; the Beast's 6 is not.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // Only the blocker died to the Beast's 6 damage; the bystander is untouched.
        assertThat(countPermanents(player2, "Disciple of the Old Ways")).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature blocked by Alms Beast has lifelink")
    void blockedCreatureGainsLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent beast = addCreatureReady(player1, new AlmsBeast());
        Permanent attacker = addCreatureReady(player2, new DiscipleOfTheOldWays());

        declareAttackersAndPrepareBlockers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(beast),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Lifelink is gone once combat ends")
    void lifelinkOnlyLastsWhileInCombat() {
        Permanent beast = addCreatureReady(player1, new AlmsBeast());
        Permanent blocker = addCreatureReady(player2, new DiscipleOfTheOldWays());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(beast)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(beast))));

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.LIFELINK)).isTrue();

        blocker.clearCombatState();
        beast.clearCombatState();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.LIFELINK)).isFalse();
    }
}
