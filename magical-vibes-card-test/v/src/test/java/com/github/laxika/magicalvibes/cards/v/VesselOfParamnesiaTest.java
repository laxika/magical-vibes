package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VesselOfParamnesia.class, Island.class, GrizzlyBears.class})
class VesselOfParamnesiaTest extends BaseCardTest {

    @Test
    void sacrificesItselfMillsTargetPlayerAndDrawsACard() {
        Island drawnCard = new Island();
        harness.addToBattlefield(player1, new VesselOfParamnesia());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Vessel of Paramnesia");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void canTargetItsControllerAndMillsBeforeDrawing() {
        VesselOfParamnesia vessel = new VesselOfParamnesia();
        Island first = new Island();
        Island second = new Island();
        Island third = new Island();
        Island drawnCard = new Island();
        harness.addToBattlefield(player1, vessel);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third, drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(vessel);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third, drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(vessel, first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void millsAllAvailableCardsFromAShortLibraryAndStillDraws() {
        Island first = new Island();
        Island second = new Island();
        Island drawnCard = new Island();
        harness.addToBattlefield(player1, new VesselOfParamnesia());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void stillDrawsWhenTargetPlayersLibraryIsEmpty() {
        Island drawnCard = new Island();
        harness.addToBattlefield(player1, new VesselOfParamnesia());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void cannotActivateWithoutBlueManaAndDoesNotSacrifice() {
        harness.addToBattlefield(player1, new VesselOfParamnesia());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vessel of Paramnesia");
        harness.assertNotInGraveyard(player1, "Vessel of Paramnesia");
        assertThat(gd.stack).isEmpty();
    }
}
