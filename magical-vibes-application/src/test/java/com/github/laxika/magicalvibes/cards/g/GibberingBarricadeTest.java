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
        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(21);
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
        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(21);
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
