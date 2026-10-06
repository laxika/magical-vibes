package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LampadOfDeathsVigil.class, GrizzlyBears.class})
class LampadOfDeathsVigilTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature makes each opponent lose 1 life and gains 1 life")
    void sacrificesCreatureAndDrainsEachOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LampadOfDeathsVigil());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Lampad of Death's Vigil");
    }

    @Test
    @DisplayName("Can sacrifice Lampad of Death's Vigil itself")
    void canSacrificeItself() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LampadOfDeathsVigil());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Lampad of Death's Vigil");
    }

    @Test
    @DisplayName("A tapped summoning-sick Lampad can sacrifice itself, with life changes only on resolution")
    void tappedSummoningSickLampadPaysSacrificeBeforeResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent lampad = harness.addToBattlefieldAndReturn(player1, new LampadOfDeathsVigil());
        lampad.tap();
        lampad.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Lampad of Death's Vigil");
        harness.assertInGraveyard(player1, "Lampad of Death's Vigil");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutMana() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LampadOfDeathsVigil());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Lampad of Death's Vigil");
        harness.assertNotInGraveyard(player1, "Lampad of Death's Vigil");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The nonactive controller gains life and the opposing player loses life")
    void nonactiveControllerCanActivate() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new LampadOfDeathsVigil());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
        harness.assertInGraveyard(player2, "Lampad of Death's Vigil");
    }
}
