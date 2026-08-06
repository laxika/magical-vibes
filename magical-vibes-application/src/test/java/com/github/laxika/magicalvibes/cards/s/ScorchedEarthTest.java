package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScorchedEarthTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 discards two land cards and destroys two target lands")
    void destroysXLandsForTwoDiscardedLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new ScorchedEarth(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 3); // X=2: {2}{R}

        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID islandId = harness.getPermanentId(player2, "Island");

        harness.castSorceryWithDiscards(player1, 0, 2, List.of(forestId, islandId), List.of(1, 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player2, "Island");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Mountain")).hasSize(2);
    }

    @Test
    @DisplayName("X=0 discards nothing and destroys nothing")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ScorchedEarth(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1); // X=0: {0}{R}

        harness.castSorceryWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot discard a nonland card to pay the additional cost")
    void cannotDiscardNonlandCard() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ScorchedEarth(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2); // X=1

        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 1, List.of(forestId), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land cards");
    }

    @Test
    @DisplayName("Must discard exactly X land cards")
    void mustDiscardExactlyX() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new ScorchedEarth(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 3); // X=2

        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID islandId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 2, List.of(forestId, islandId), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard 2");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScorchedEarth(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2); // X=1

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 1, List.of(bearsId), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lands");
    }

    @Test
    @DisplayName("Cannot target more lands than X")
    void cannotTargetMoreThanX() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new ScorchedEarth(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2); // X=1

        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID islandId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 1, List.of(forestId, islandId), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Can destroy your own lands")
    void canTargetOwnLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new ScorchedEarth(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2); // X=1

        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.castSorceryWithDiscards(player1, 0, 1, List.of(forestId), List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
    }
}
