package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UktabiFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysTargetArtifact() {
        harness.addToBattlefield(player1, new UktabiFaerie());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent target = findPermanent(player2, "Ornithopter");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Uktabi Faerie is sacrificed as a cost of the ability")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new UktabiFaerie());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent target = findPermanent(player2, "Ornithopter");
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Uktabi Faerie");
        harness.assertInGraveyard(player1, "Uktabi Faerie");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player1, new UktabiFaerie());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent target = findPermanent(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }
}
