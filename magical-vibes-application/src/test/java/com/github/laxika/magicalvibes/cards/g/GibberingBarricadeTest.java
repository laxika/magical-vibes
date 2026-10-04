package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GibberingBarricade.class, GrizzlyBears.class})
class GibberingBarricadeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gains life and draws a card")
    void sacrificesAnotherCreatureGainsLifeAndDraws() {
        Permanent barricade = harness.addToBattlefieldAndReturn(player1, new GibberingBarricade());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Gibbering Barricade").getId()).isEqualTo(barricade.getId());
        harness.assertLife(player1, 21);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability can sacrifice Gibbering Barricade itself")
    void sacrificesItself() {
        harness.addToBattlefield(player1, new GibberingBarricade());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gibbering Barricade");
        harness.assertLife(player1, 21);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifice is paid before life gain and draw resolve")
    void paysSacrificeBeforeResolution() {
        harness.addToBattlefield(player1, new GibberingBarricade());
        harness.addToBattlefield(player2, new GibberingBarricade());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GibberingBarricade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Gibbering Barricade");
        harness.assertNotOnBattlefield(player1, "Gibbering Barricade");
        harness.assertOnBattlefield(player2, "Gibbering Barricade");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Gibbering Barricade");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Barricade can activate repeatedly")
    void activatesRepeatedlyWhileTapped() {
        Permanent barricade = harness.addToBattlefieldAndReturn(player1, new GibberingBarricade());
        barricade.setTapped(true);
        barricade.setSummoningSick(true);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GibberingBarricade());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GibberingBarricade(), new GibberingBarricade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Gibbering Barricade");
    }

    @Test
    @DisplayName("Activation needs the full three mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new GibberingBarricade());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Gibbering Barricade");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
}
