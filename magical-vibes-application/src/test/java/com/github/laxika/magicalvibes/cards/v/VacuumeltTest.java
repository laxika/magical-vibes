package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vacuumelt.class, GrizzlyBears.class})
class VacuumeltTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature to its owner's hand")
    void returnsTargetCreatureToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castVacuumelt(target, List.of());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Replicate creates a copy that may target another creature")
    void replicateCopyMayTargetAnotherCreature() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castVacuumelt(originalTarget, List.of("{2}{U}"));

        harness.passBothPriorities();
        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        harness.setHand(player1, List.of(new Vacuumelt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    private void castVacuumelt(Permanent target, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new Vacuumelt()));
        harness.addMana(player1, ManaColor.BLUE, 1 + replicatePayments.size());
        harness.addMana(player1, ManaColor.COLORLESS, 2 + replicatePayments.size() * 2);
        harness.castInstantWithRepeatedCosts(player1, 0, target.getId(), replicatePayments);
    }
}
