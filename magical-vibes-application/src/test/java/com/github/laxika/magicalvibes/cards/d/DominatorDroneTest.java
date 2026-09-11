package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DominatorDrone.class, GrizzlyBears.class, Memnite.class})
class DominatorDroneTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each opponent lose 2 life when you control another colorless creature")
    void etbWithAnotherColorlessCreature() {
        harness.addToBattlefield(player1, new Memnite());
        castDominatorDrone();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB does not trigger without another colorless creature")
    void etbWithoutAnotherColorlessCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castDominatorDrone();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Ingest exiles the top card of the damaged player's library")
    void ingestExilesTopCard() {
        Permanent drone = addCreatureReady(player1, new DominatorDrone());
        drone.setAttacking(true);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
    }

    private void castDominatorDrone() {
        harness.setHand(player1, List.of(new DominatorDrone()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
    }
}
