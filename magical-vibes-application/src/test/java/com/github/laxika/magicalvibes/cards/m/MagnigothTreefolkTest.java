package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagnigothTreefolk.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class,
        Swamp.class, Mountain.class})
class MagnigothTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("Gains landwalk for a basic land type among its controller's lands")
    void gainsLandwalkForControlledBasicLandType() {
        assertLandwalkPreventsBlocking(Forest::new);
    }

    @Test
    @DisplayName("Only the land types controlled by the Treefolk's controller count")
    void opponentLandTypesDoNotCountForDomain() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent treefolk = addAttacker(player1);

        prepareDeclareBlockers();

        declareBlocker(blocker, treefolk);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A nonland permanent with a basic land subtype does not count")
    void nonlandBasicSubtypeDoesNotCount() {
        Permanent forestCreature = addCreatureReady(player1, new GrizzlyBears());
        TestCards.mutableCard(forestCreature).setSubtypes(List.of(CardSubtype.FOREST));
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent treefolk = addAttacker(player1);

        prepareDeclareBlockers();

        declareBlocker(blocker, treefolk);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gains islandwalk for an Island among its controller's lands")
    void gainsIslandwalkForControlledBasicLandType() {
        assertLandwalkPreventsBlocking(Island::new);
    }

    @Test
    @DisplayName("Gains plainswalk for a Plains among its controller's lands")
    void gainsPlainswalkForControlledBasicLandType() {
        assertLandwalkPreventsBlocking(Plains::new);
    }

    @Test
    @DisplayName("Gains swampwalk for a Swamp among its controller's lands")
    void gainsSwampwalkForControlledBasicLandType() {
        assertLandwalkPreventsBlocking(Swamp::new);
    }

    @Test
    @DisplayName("Gains mountainwalk for a Mountain among its controller's lands")
    void gainsMountainwalkForControlledBasicLandType() {
        assertLandwalkPreventsBlocking(Mountain::new);
    }

    private void assertLandwalkPreventsBlocking(Supplier<Card> landFactory) {
        harness.addToBattlefield(player1, landFactory.get());
        harness.addToBattlefield(player2, landFactory.get());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent treefolk = addAttacker(player1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlocker(blocker, treefolk))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private Permanent addAttacker(Player player) {
        Permanent treefolk = addCreatureReady(player, new MagnigothTreefolk());
        treefolk.setAttacking(true);
        return treefolk;
    }

    private void declareBlocker(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
