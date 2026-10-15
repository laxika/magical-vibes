package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArdentElementalist.class, Divination.class, GrizzlyBears.class, LightningBolt.class})
class ArdentElementalistTest extends BaseCardTest {

    @Test
    void etbReturnsInstantToHand() {
        LightningBolt lightningBolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(lightningBolt));

        castArdentElementalist();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(lightningBolt.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    void etbReturnsSorceryToHand() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));

        castArdentElementalist();

        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
    }

    @Test
    void creatureIsNotALegalTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castArdentElementalist();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void emptyGraveyardProducesNoChoice() {
        castArdentElementalist();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    void opponentsGraveyardCannotSupplyATarget() {
        harness.setGraveyard(player2, List.of(new LightningBolt()));

        castArdentElementalist();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Lightning Bolt");
        harness.assertNotInHand(player1, "Lightning Bolt");
    }

    @Test
    void returnsOnlyTheChosenCardFromMixedGraveyard() {
        LightningBolt lightningBolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(lightningBolt, new Divination(), new GrizzlyBears()));

        castArdentElementalist();
        harness.handleMultipleCardsChosen(player1, List.of(lightningBolt.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
        harness.assertInGraveyard(player1, "Divination");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotDeclineMandatoryTargetSelection() {
        LightningBolt lightningBolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(lightningBolt));

        castArdentElementalist();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(lightningBolt.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lightning Bolt");
    }

    @Test
    void abilityStillReturnsCardAfterElementalistDies() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));

        castArdentElementalist();
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Ardent Elementalist"));

        harness.assertNotOnBattlefield(player1, "Ardent Elementalist");
        harness.assertInGraveyard(player1, "Ardent Elementalist");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
    }

    @Test
    void targetLeavingGraveyardDoesNotReturnAnotherCard() {
        LightningBolt lightningBolt = new LightningBolt();
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(lightningBolt, divination));

        castArdentElementalist();
        harness.handleMultipleCardsChosen(player1, List.of(lightningBolt.getId()));
        harness.setGraveyard(player1, List.of(divination));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Lightning Bolt");
        harness.assertNotInHand(player1, "Divination");
        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castArdentElementalist() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ArdentElementalist(), "{3}{R}");
        harness.passBothPriorities();
    }
}
