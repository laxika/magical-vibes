package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OmoQueenOfVesuva;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WonderscapeSage.class, Desert.class, Island.class, CommandTower.class, OmoQueenOfVesuva.class})
class WonderscapeSageTest extends BaseCardTest {

    @Test
    @DisplayName("Returning a basic land draws, then requires a discard")
    void basicLandRequiresDiscard() {
        addCreatureReady(player1, new WonderscapeSage());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Returning a land with a nonbasic land type skips the discard")
    void nonbasicLandSkipsDiscard() {
        addCreatureReady(player1, new WonderscapeSage());
        harness.addToBattlefield(player1, new Desert());
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Island", "Island", "Desert");
    }

    @Test
    void nonbasicLandWithoutLandTypesStillRequiresDiscard() {
        addCreatureReady(player1, new WonderscapeSage());
        harness.addToBattlefield(player1, new CommandTower());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Command Tower");
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void tappedLandCanBeReturnedAndPaidBeforeDrawing() {
        Permanent sage = addCreatureReady(player1, new WonderscapeSage());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        land.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CommandTower()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sage.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
        harness.assertNotInHand(player1, "Command Tower");

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Island");
        harness.assertInHand(player1, "Command Tower");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void chosenLandDeterminesWhetherDiscardIsRequired() {
        addCreatureReady(player1, new WonderscapeSage());
        harness.addToBattlefield(player1, new Island());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, desert.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Desert");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Desert", "Island");
    }

    @Test
    void basicLandWithEverythingCounterUsesTypesFromBattlefield() {
        addCreatureReady(player1, new WonderscapeSage());
        harness.addToBattlefield(player1, new OmoQueenOfVesuva());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setCounterCount(CounterType.EVERYTHING, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CommandTower()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Island", "Command Tower");
    }

    @Test
    void cannotPayReturnCostWithOpponentsLand() {
        addCreatureReady(player1, new WonderscapeSage());
        harness.addToBattlefield(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.stack).isEmpty();
    }
}
