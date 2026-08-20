package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShepherdOfTheCosmosTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target permanent card with mana value 2 or less")
    void returnsLowManaValuePermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castShepherd();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can return a low-mana-value land permanent")
    void returnsLandPermanent() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castShepherd();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("ETB rejects nonpermanents and permanents with mana value greater than 2")
    void rejectsCardsOutsideTheFilter() {
        Card highManaValuePermanent = new AvatarOfMight();
        Card nonpermanent = new HolyDay();
        GrizzlyBears legal = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(highManaValuePermanent, nonpermanent, legal));

        castShepherd();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(legal.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(highManaValuePermanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB only targets cards in its controller's graveyard")
    void onlyTargetsOwnGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        castShepherd();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be foretold and cast from exile on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        ShepherdOfTheCosmos shepherd = new ShepherdOfTheCosmos();
        harness.setHand(player1, List.of(shepherd));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(shepherd.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, shepherd.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shepherd of the Cosmos");
    }

    private void castShepherd() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShepherdOfTheCosmos()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
