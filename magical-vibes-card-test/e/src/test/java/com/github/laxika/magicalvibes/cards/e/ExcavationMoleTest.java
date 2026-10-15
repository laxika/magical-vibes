package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BlisterspitGremlin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExcavationMole.class, Forest.class, BlisterspitGremlin.class})
class ExcavationMoleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards from its controller's library")
    void etbMillsThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("ETB mills its controller's library, not the opponent's")
    void etbMillsControllerOnly() {
        int opponentDeckSize = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardSize = gd.playerGraveyards.get(player2.getId()).size();

        castAndResolve();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSize);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(opponentGraveyardSize);
    }

    @Test
    void millsOnlyTopThreeCards() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
    }

    @Test
    void millsAllRemainingCardsFromShortLibrary() {
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(remaining));

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void emptyLibraryDoesNotCauseLossWhenMilling() {
        harness.setLibrary(player1, List.of());

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertOnBattlefield(player1, "Excavation Mole");
    }

    @Test
    void millingWaitsForEnterTriggerToResolve() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new ExcavationMole(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Excavation Mole");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ExcavationMole());
        Permanent blocker = addCreatureReady(player2, new BlisterspitGremlin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 1, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Blisterspit Gremlin");
        harness.assertOnBattlefield(player1, "Excavation Mole");
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new ExcavationMole(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
