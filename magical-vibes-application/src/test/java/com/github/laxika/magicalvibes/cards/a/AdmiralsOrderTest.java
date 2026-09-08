package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdmiralsOrderTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target spell when cast for its mana cost")
    void countersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new AdmiralsOrder()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Admiral's Order");
    }

    @Test
    @DisplayName("Can be cast for {U} after attacking this turn")
    void castsForAlternateCostAfterAttacking() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AdmiralsOrder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.castWithAlternateCost(player1, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Admiral's Order");
    }

    @Test
    @DisplayName("The alternate cost requires having attacked this turn")
    void alternateCostRequiresRaid() {
        harness.setHand(player1, List.of(new AdmiralsOrder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
