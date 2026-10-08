package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BribersPurse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JeskaiCharm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WardenOfTheEye.class, Shock.class, GrizzlyBears.class, Forest.class,
        BribersPurse.class, JeskaiCharm.class})
class WardenOfTheEyeTest extends BaseCardTest {

    private void castWardenOfTheEye() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WardenOfTheEye(), "{2}{U}{R}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted noncreature, nonland card from graveyard to hand")
    void etbReturnsNoncreatureNonlandCardToHand() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        castWardenOfTheEye();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Creature and land cards are not legal ETB targets")
    void creatureAndLandCardsAreNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));

        castWardenOfTheEye();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void returnsOnlyTheChosenArtifactCard() {
        BribersPurse purse = new BribersPurse();
        JeskaiCharm charm = new JeskaiCharm();
        harness.setGraveyard(player1, List.of(purse, charm));

        castWardenOfTheEye();
        harness.handleMultipleCardsChosen(player1, List.of(purse.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Briber's Purse");
        harness.assertNotInGraveyard(player1, "Briber's Purse");
        harness.assertInGraveyard(player1, "Jeskai Charm");
        harness.assertNotInHand(player1, "Jeskai Charm");
    }

    @Test
    void cannotChooseAnOpponentsGraveyardCard() {
        JeskaiCharm own = new JeskaiCharm();
        JeskaiCharm opponents = new JeskaiCharm();
        harness.setGraveyard(player1, List.of(own));
        harness.setGraveyard(player2, List.of(opponents));

        castWardenOfTheEye();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponents.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(own.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Jeskai Charm");
        harness.assertInGraveyard(player2, "Jeskai Charm");
    }

    @Test
    void cannotDeclineTheRequiredTarget() {
        JeskaiCharm charm = new JeskaiCharm();
        harness.setGraveyard(player1, List.of(charm));

        castWardenOfTheEye();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(charm.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Jeskai Charm");
    }

    @Test
    void doesNotChooseAnotherCardWhenTheTargetLeavesTheGraveyard() {
        JeskaiCharm charm = new JeskaiCharm();
        BribersPurse purse = new BribersPurse();
        harness.setGraveyard(player1, List.of(charm, purse));

        castWardenOfTheEye();
        harness.handleMultipleCardsChosen(player1, List.of(charm.getId()));
        harness.setGraveyard(player1, List.of(purse));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Jeskai Charm");
        harness.assertNotInHand(player1, "Briber's Purse");
        harness.assertInGraveyard(player1, "Briber's Purse");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
