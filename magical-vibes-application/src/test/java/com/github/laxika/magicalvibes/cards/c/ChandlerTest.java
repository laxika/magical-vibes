package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChandlerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact creature")
    void destroysArtifactCreature() {
        addCreatureReady(player1, new Chandler());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent target = findPermanent(player2, "Iron Myr");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Iron Myr");
        harness.assertInGraveyard(player2, "Iron Myr");
    }

    @Test
    @DisplayName("Ability taps Chandler as a cost")
    void tapsAsCost() {
        addCreatureReady(player1, new Chandler());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent target = findPermanent(player2, "Iron Myr");
        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(findPermanent(player1, "Chandler").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonArtifactCreature() {
        addCreatureReady(player1, new Chandler());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent target = findPermanent(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
    }

    @Test
    @DisplayName("Cannot activate without enough red mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new Chandler());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent target = findPermanent(player2, "Iron Myr");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
