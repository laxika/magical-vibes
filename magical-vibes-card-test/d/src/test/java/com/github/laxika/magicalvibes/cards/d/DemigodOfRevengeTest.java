package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemigodOfRevenge.class, GrizzlyBears.class, Cancel.class})
class DemigodOfRevengeTest extends BaseCardTest {

    private void prepareToCast(int redMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, redMana);
    }

    private long demigodsOnBattlefield() {
        return countPermanents(player1, "Demigod of Revenge");
    }

    @Test
    @DisplayName("Casting returns another Demigod of Revenge from graveyard to the battlefield")
    void castReturnsGraveyardDemigod() {
        harness.setGraveyard(player1, List.of(new DemigodOfRevenge()));
        harness.setHand(player1, List.of(new DemigodOfRevenge()));
        prepareToCast(5);

        harness.castCreature(player1, 0);

        // ON_SELF_CAST trigger sits above the spell; resolve it (returns graveyard Demigod).
        harness.passBothPriorities();
        // Resolve the creature spell itself.
        harness.passBothPriorities();

        // Both copies are now on the battlefield.
        assertThat(demigodsOnBattlefield()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Demigod of Revenge");
    }

    @Test
    @DisplayName("Only cards named Demigod of Revenge are returned")
    void onlyReturnsNamedCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new DemigodOfRevenge()));
        harness.setHand(player1, List.of(new DemigodOfRevenge()));
        prepareToCast(5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(demigodsOnBattlefield()).isEqualTo(2);
        // Grizzly Bears stays in the graveyard.
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting with no Demigod in graveyard just resolves the spell")
    void castWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new DemigodOfRevenge()));
        prepareToCast(5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Only the cast copy is on the battlefield.
        assertThat(demigodsOnBattlefield()).isEqualTo(1);
    }

    @Test
    void triggerReturnsAllOwnCopiesBeforeSpellResolvesButNotOpponentsCopies() {
        harness.setGraveyard(player1, List.of(new DemigodOfRevenge(), new DemigodOfRevenge()));
        harness.setGraveyard(player2, List.of(new DemigodOfRevenge()));
        harness.setHand(player1, List.of(new DemigodOfRevenge()));
        prepareToCast(5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(demigodsOnBattlefield()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Demigod of Revenge");
        harness.assertInGraveyard(player2, "Demigod of Revenge");
        harness.assertNotOnBattlefield(player2, "Demigod of Revenge");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(demigodsOnBattlefield()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counteringBeforeTriggerResolvesReturnsTheCounteredSpellToo() {
        DemigodOfRevenge spell = new DemigodOfRevenge();
        harness.setGraveyard(player1, List.of(new DemigodOfRevenge()));
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new Cancel()));
        prepareToCast(5);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(demigodsOnBattlefield()).isZero();
        harness.assertInGraveyard(player1, "Demigod of Revenge");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(demigodsOnBattlefield()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Demigod of Revenge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counteringAfterTriggerResolvesLeavesTheCounteredSpellInGraveyard() {
        DemigodOfRevenge spell = new DemigodOfRevenge();
        harness.setGraveyard(player1, List.of(new DemigodOfRevenge()));
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new Cancel()));
        prepareToCast(5);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(demigodsOnBattlefield()).isEqualTo(1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(demigodsOnBattlefield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Demigod of Revenge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastDoesNotReturnGraveyardCopies() {
        harness.setGraveyard(player1, List.of(new DemigodOfRevenge()));

        harness.addToBattlefield(player1, new DemigodOfRevenge());

        assertThat(demigodsOnBattlefield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Demigod of Revenge");
        assertThat(gd.stack).isEmpty();
    }
}
