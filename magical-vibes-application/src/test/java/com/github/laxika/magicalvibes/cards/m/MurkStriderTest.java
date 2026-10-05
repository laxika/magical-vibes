package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MurkStrider.class, GrizzlyBears.class, PathToExile.class})
class MurkStriderTest extends BaseCardTest {

    @Test
    void processesAnExiledCardBeforeReturningTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));

        castMurkStrider();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        harness.assertInGraveyard(player2, "Path to Exile");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void mayDeclineProcessingAndLeavesTargetCreatureOnTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));

        castMurkStrider();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
    }

    @Test
    void doesNotProcessControllerOwnedExiledCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player1, List.of(exiledCard));

        castMurkStrider();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
    }

    private void castMurkStrider() {
        harness.castFromHand(player1, new MurkStrider(), "{3}{U}");
        harness.passBothPriorities();
    }

    @Test
    @CardUsed(MurkStrider.class)
    void canReturnItselfAfterProcessing() {
        MurkStrider exiledCard = new MurkStrider();
        harness.setExile(player2, List.of(exiledCard));

        castMurkStrider();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Murk Strider"));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        harness.assertInGraveyard(player2, "Murk Strider");
        harness.assertInHand(player1, "Murk Strider");
        harness.assertNotOnBattlefield(player1, "Murk Strider");
    }

    @Test
    @CardUsed(MurkStrider.class)
    void doesNothingWhenThereAreNoExiledCards() {
        castMurkStrider();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Murk Strider"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Murk Strider");
        harness.assertNotInHand(player1, "Murk Strider");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed(MurkStrider.class)
    void canProcessASingleFaceDownOpponentOwnedCard() {
        MurkStrider exiledCard = new MurkStrider();
        gd.addToExile(player2.getId(), exiledCard, null, true);

        castMurkStrider();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Murk Strider"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        harness.assertInGraveyard(player2, "Murk Strider");
        harness.assertInHand(player1, "Murk Strider");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void doesNotProcessWhenTheTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        harness.setLibrary(player2, List.of());

        castMurkStrider();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new PathToExile()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
