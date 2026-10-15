package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneyardScourge.class, ShivanDragon.class, GrizzlyBears.class, PlanarCleansing.class})
class BoneyardScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("A Dragon's death offers to return Boneyard Scourge from the graveyard")
    void dragonDeathOffersReturn() {
        BoneyardScourge scourge = new BoneyardScourge();
        harness.setGraveyard(player1, List.of(scourge));
        harness.addToBattlefield(player1, new ShivanDragon());
        castPlanarCleansing();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Paying for Boneyard Scourge's trigger returns it to the battlefield")
    void payingReturnsScourge() {
        BoneyardScourge scourge = new BoneyardScourge();
        harness.setGraveyard(player1, List.of(scourge));
        harness.addToBattlefield(player1, new ShivanDragon());
        castPlanarCleansing();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Boneyard Scourge");
        harness.assertNotInGraveyard(player1, "Boneyard Scourge");
    }

    @Test
    @DisplayName("Declining Boneyard Scourge's trigger leaves it in the graveyard")
    void decliningLeavesScourgeInGraveyard() {
        BoneyardScourge scourge = new BoneyardScourge();
        harness.setGraveyard(player1, List.of(scourge));
        harness.addToBattlefield(player1, new ShivanDragon());
        castPlanarCleansing();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Boneyard Scourge");
        harness.assertNotOnBattlefield(player1, "Boneyard Scourge");
    }

    @Test
    @DisplayName("A non-Dragon's death does not trigger Boneyard Scourge")
    void nonDragonDeathDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new BoneyardScourge()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        castPlanarCleansing();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Boneyard Scourge");
    }

    private void castPlanarCleansing() {
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
