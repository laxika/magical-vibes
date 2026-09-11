package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonstrousHound.class, CityOfTraitors.class, RagingGoblin.class})
class MonstrousHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrous Hound can attack when its controller has more lands")
    void canAttackWithMoreLands() {
        addCreatureReady(player1, new MonstrousHound());
        harness.addToBattlefield(player1, new CityOfTraitors());
        harness.addToBattlefield(player1, new CityOfTraitors());
        harness.addToBattlefield(player2, new CityOfTraitors());

        declareAttackers(player1, List.of(0));
    }

    @Test
    @DisplayName("Monstrous Hound cannot attack when its controller does not have more lands")
    void cannotAttackWithoutMoreLands() {
        addCreatureReady(player1, new MonstrousHound());
        harness.addToBattlefield(player1, new CityOfTraitors());
        harness.addToBattlefield(player2, new CityOfTraitors());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Monstrous Hound cannot attack when defending player has more lands")
    void cannotAttackWithFewerLands() {
        addCreatureReady(player1, new MonstrousHound());
        harness.addToBattlefield(player2, new CityOfTraitors());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Monstrous Hound can block when its controller has more lands")
    void canBlockWithMoreLands() {
        addCreatureReady(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new CityOfTraitors());
        addCreatureReady(player2, new MonstrousHound());
        harness.addToBattlefield(player2, new CityOfTraitors());
        harness.addToBattlefield(player2, new CityOfTraitors());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Monstrous Hound cannot block when its controller does not have more lands")
    void cannotBlockWithoutMoreLands() {
        addCreatureReady(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new CityOfTraitors());
        addCreatureReady(player2, new MonstrousHound());
        harness.addToBattlefield(player2, new CityOfTraitors());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Monstrous Hound cannot block when attacking player has more lands")
    void cannotBlockWithFewerLands() {
        addCreatureReady(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new CityOfTraitors());
        harness.addToBattlefield(player1, new CityOfTraitors());
        addCreatureReady(player2, new MonstrousHound());
        harness.addToBattlefield(player2, new CityOfTraitors());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
