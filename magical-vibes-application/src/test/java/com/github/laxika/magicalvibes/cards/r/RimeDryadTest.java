package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimeDryad.class, BalduvianBears.class, Forest.class,
        SnowCoveredForest.class, SnowCoveredSwamp.class})
class RimeDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked when defending player controls a snow Forest")
    void cantBeBlockedWhenDefenderControlsSnowForest() {
        addSnowForest(player2);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent dryad = readyAttacker(player1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(dryad)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Can be blocked when defending player controls only a non-snow Forest")
    void canBeBlockedWithNonSnowForest() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent dryad = readyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(dryad))));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can be blocked when the defender's snow land is not a Forest")
    void canBeBlockedWithSnowSwamp() {
        harness.addToBattlefield(player2, new SnowCoveredSwamp());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent dryad = readyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(dryad))));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can be blocked when only the attacking player controls a snow Forest")
    void canBeBlockedWhenOnlyAttackerControlsSnowForest() {
        addSnowForest(player1);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent dryad = readyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(dryad))));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A snow nonland with the Forest subtype does not enable snow forestwalk")
    void snowNonlandWithForestSubtypeDoesNotEnableForestwalk() {
        Permanent snowForestCreature = new Permanent(new BalduvianBears());
        Card card = TestCards.mutableCard(snowForestCreature);
        card.setSubtypes(List.of(CardSubtype.FOREST));
        card.setSupertypes(EnumSet.of(CardSupertype.SNOW));
        gd.playerBattlefields.get(player2.getId()).add(snowForestCreature);

        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent dryad = readyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(dryad))));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked Rime Dryad deals 1 damage")
    void dealsOneWhenUnblocked() {
        readyAttacker(player1);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent readyAttacker(Player player) {
        Permanent perm = addCreatureReady(player, new RimeDryad());
        perm.setAttacking(true);
        return perm;
    }

    private void addSnowForest(Player player) {
        harness.addToBattlefield(player, new SnowCoveredForest());
    }
}
