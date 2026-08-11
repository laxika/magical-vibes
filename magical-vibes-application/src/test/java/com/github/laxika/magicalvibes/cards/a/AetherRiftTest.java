package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.UndergroundRiver;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AetherRiftTest extends BaseCardTest {

    @Test
    @DisplayName("A creature discarded at upkeep returns unless a player pays 5 life")
    void creatureReturnsWhenNobodyPays() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A player paying 5 life prevents the discarded creature from returning")
    void paymentPreventsReturn() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A noncreature discarded at upkeep is not returned or offered a payment")
    void noncreatureIsNotReturned() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new UndergroundRiver()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Underground River");
    }

    @Test
    @DisplayName("A player unable to pay is skipped and the next player can pay")
    void unablePlayerIsSkipped() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.setLife(player1, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }
}
