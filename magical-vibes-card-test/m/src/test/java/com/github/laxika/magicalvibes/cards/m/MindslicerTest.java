package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mindslicer.class, GrizzlyBears.class, CrawWurm.class})
class MindslicerTest extends BaseCardTest {

    @Test
    @DisplayName("When Mindslicer dies, its death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        setupCombatWhereMindslicerDies();
        resolveCombat();

        harness.assertInGraveyard(player1, "Mindslicer");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mindslicer");
    }

    @Test
    @DisplayName("Resolving the death trigger makes each player discard their entire hand")
    void eachPlayerDiscardsEntireHand() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        setupCombatWhereMindslicerDies();
        resolveCombat();
        harness.passBothPriorities(); // Resolve death trigger

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears")).hasSize(3);
    }

    @Test
    @DisplayName("Death trigger empties a non-empty hand while logging the empty one")
    void handlesEmptyHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        setupCombatWhereMindslicerDies();
        resolveCombat();
        harness.passBothPriorities(); // Resolve death trigger

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gameLogContains("no cards to discard")).isTrue();
    }

    @Test
    @DisplayName("Hands remain intact until the death trigger resolves, including cards added in response")
    void discardsHandsAsTheyExistAtResolution() {
        GrizzlyBears originalCard = new GrizzlyBears();
        GrizzlyBears addedCard = new GrizzlyBears();
        GrizzlyBears opponentCard = new GrizzlyBears();
        harness.setHand(player1, List.of(originalCard));
        harness.setHand(player2, List.of());

        setupCombatWithCrawWurm();
        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(originalCard, addedCard));
        harness.setHand(player2, List.of(opponentCard));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(originalCard, addedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The death trigger resolves with both hands empty after simultaneous combat deaths")
    void resolvesWithBothHandsEmpty() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        setupCombatWithCrawWurm();
        resolveCombat();

        harness.assertInGraveyard(player1, "Mindslicer");
        harness.assertInGraveyard(player2, "Craw Wurm");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void setupCombatWithCrawWurm() {
        addCreatureReady(player1, new Mindslicer());
        addCreatureReady(player2, new CrawWurm());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    private void setupCombatWhereMindslicerDies() {
        addCreatureReady(player1, new Mindslicer());
        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(5);
        bigBear.setToughness(5);
        addCreatureReady(player2, bigBear);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
