package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.Anticipate;
import com.github.laxika.magicalvibes.cards.e.EldraziSkyspawner;
import com.github.laxika.magicalvibes.cards.r.RuinousPath;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlamogsReclaimer.class, Anticipate.class, RuinousPath.class, EldraziSkyspawner.class})
class UlamogsReclaimerTest extends BaseCardTest {

    @Test
    void returnsTargetInstantAfterPuttingOpponentOwnedExiledCardIntoGraveyard() {
        Anticipate instant = new Anticipate();
        RuinousPath exiledCard = new RuinousPath();
        harness.setGraveyard(player1, List.of(instant));
        harness.setExile(player2, List.of(exiledCard));

        castReclaimer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        harness.assertInHand(player1, "Anticipate");
        harness.assertInGraveyard(player2, "Ruinous Path");
    }

    @Test
    void decliningToProcessAnExiledCardDoesNotReturnTheTarget() {
        Anticipate instant = new Anticipate();
        RuinousPath exiledCard = new RuinousPath();
        harness.setGraveyard(player1, List.of(instant));
        harness.setExile(player2, List.of(exiledCard));

        castReclaimer();

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Anticipate");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
    }

    @Test
    void doesNotReturnACreatureCard() {
        EldraziSkyspawner creature = new EldraziSkyspawner();
        harness.setGraveyard(player1, List.of(creature));
        harness.setExile(player2, List.of(new RuinousPath()));

        castReclaimer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Eldrazi Skyspawner");
    }

    @Test
    void returnsTargetSorceryAndProcessesOnlyOneCard() {
        RuinousPath sorcery = new RuinousPath();
        Anticipate processed = new Anticipate();
        EldraziSkyspawner unprocessed = new EldraziSkyspawner();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setExile(player2, List.of(processed, unprocessed));

        castReclaimer();
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(processed.getId()));

        harness.assertInHand(player1, "Ruinous Path");
        harness.assertNotInGraveyard(player1, "Ruinous Path");
        harness.assertInGraveyard(player2, "Anticipate");
        assertThat(gd.findExiledCard(unprocessed.getId())).isNotNull();
    }

    @Test
    void doesNotReturnTargetWhenExileIsEmpty() {
        Anticipate instant = new Anticipate();
        harness.setGraveyard(player1, List.of(instant));

        castReclaimer();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Anticipate");
        harness.assertNotInHand(player1, "Anticipate");
    }

    @Test
    void cannotProcessControllerOwnedExiledCard() {
        Anticipate instant = new Anticipate();
        RuinousPath ownExiledCard = new RuinousPath();
        harness.setGraveyard(player1, List.of(instant));
        harness.setExile(player1, List.of(ownExiledCard));

        castReclaimer();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(ownExiledCard.getId())).isNotNull();
        harness.assertInGraveyard(player1, "Anticipate");
        harness.assertNotInHand(player1, "Anticipate");
    }

    @Test
    void doesNotProcessWhenTheGraveyardTargetLeavesBeforeResolution() {
        Anticipate instant = new Anticipate();
        RuinousPath exiledCard = new RuinousPath();
        harness.setGraveyard(player1, List.of(instant));
        harness.setExile(player2, List.of(exiledCard));

        castReclaimer();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(instant));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        harness.assertNotInHand(player1, "Anticipate");
    }

    @Test
    void canProcessASingleFaceDownOpponentOwnedExiledCard() {
        Anticipate instant = new Anticipate();
        RuinousPath exiledCard = new RuinousPath();
        harness.setGraveyard(player1, List.of(instant));
        gd.exiledCards.add(new ExiledCardEntry(exiledCard, player2.getId(), null, true));

        castReclaimer();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        harness.assertInHand(player1, "Anticipate");
        harness.assertInGraveyard(player2, "Ruinous Path");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    private void castReclaimer() {
        harness.castFromHand(player1, new UlamogsReclaimer(), "{4}{U}");
        harness.passBothPriorities();
    }
}
