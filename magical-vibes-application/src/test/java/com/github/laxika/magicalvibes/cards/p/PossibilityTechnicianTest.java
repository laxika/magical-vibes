package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PossibilityTechnician.class, AlphaKavu.class, GrizzlyBears.class, Murder.class})
@DisplayName("Possibility Technician")
class PossibilityTechnicianTest extends BaseCardTest {

    @Test
    void exilesTheTopCardWhenItOrAnotherKavuEnters() {
        Card first = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first));
        harness.enterBattlefieldAndReturn(player1, new PossibilityTechnician());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);

        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(second));
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);

        harness.enterBattlefieldAndReturn(player1, new AlphaKavu());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
    }

    @Test
    void permissionRequiresControllingAKavuButReactivatesWhenOneReturns() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent technician = harness.enterBattlefieldAndReturn(player1, new PossibilityTechnician());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, technician.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");

        harness.addToBattlefield(player1, new AlphaKavu());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
