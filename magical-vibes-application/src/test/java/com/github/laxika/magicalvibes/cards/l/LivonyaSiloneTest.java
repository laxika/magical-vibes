package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivonyaSilone.class, Karakas.class, DurkwoodBoars.class})
class LivonyaSiloneTest extends BaseCardTest {

    @Test
    @DisplayName("Livonya Silone can't be blocked when defending player controls a legendary land")
    void cannotBeBlockedWhenDefenderControlsLegendaryLand() {
        harness.addToBattlefield(player2, new Karakas());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());
        Permanent livonya = addCreatureReady(player1, new LivonyaSilone());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(livonya)));

        assertThatThrownBy(() -> declareBlock(blocker, livonya))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Livonya Silone can be blocked when defending player controls a nonlegendary land")
    void canBeBlockedWhenDefenderControlsNonlegendaryLand() {
        Karakas nonlegendaryLand = new Karakas();
        nonlegendaryLand.setSupertypes(EnumSet.noneOf(CardSupertype.class));
        harness.addToBattlefield(player2, nonlegendaryLand);
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());
        Permanent livonya = addCreatureReady(player1, new LivonyaSilone());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(livonya)));
        declareBlock(blocker, livonya);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Livonya Silone can be blocked when defending player controls a legendary nonland")
    void canBeBlockedWhenDefenderControlsLegendaryNonland() {
        DurkwoodBoars legendaryCreature = new DurkwoodBoars();
        legendaryCreature.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        harness.addToBattlefield(player2, legendaryCreature);
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());
        Permanent livonya = addCreatureReady(player1, new LivonyaSilone());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(livonya)));
        declareBlock(blocker, livonya);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("First strike lets Livonya Silone survive combat with an equal blocker")
    void firstStrikeLetsLivonyaSurviveEqualCombat() {
        Permanent livonya = addCreatureReady(player1, new LivonyaSilone());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(livonya)));
        declareBlock(blocker, livonya);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Livonya Silone");
        harness.assertNotOnBattlefield(player2, "Durkwood Boars");
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
