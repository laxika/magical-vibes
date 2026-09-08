package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfirmSuspicionsTest extends BaseCardTest {

    @Test
    @DisplayName("Counters target spell and investigates three times")
    void countersAndInvestigatesThreeTimes() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ConfirmSuspicions()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player2, "Clue")).hasSize(3);
    }

    @Test
    @DisplayName("Investigates even when the target spell cannot be countered")
    void investigatesWhenTargetCannotBeCountered() {
        CarnageTyrant tyrant = new CarnageTyrant();
        harness.setHand(player1, List.of(tyrant));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ConfirmSuspicions()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castInstant(player2, 0, tyrant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Carnage Tyrant");
        assertThat(findPermanents(player2, "Clue")).hasSize(3);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConfirmSuspicions()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
