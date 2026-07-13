package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemigodOfRevengeTest extends BaseCardTest {

    private void prepareToCast(int redMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, redMana);
    }

    private long demigodsOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Demigod of Revenge"))
                .count();
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Demigod of Revenge"));
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
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
}
