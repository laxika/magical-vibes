package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AnodetLurker;
import com.github.laxika.magicalvibes.cards.e.EbonDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BringerOfTheWhiteDawn.class, AnodetLurker.class, EbonDrake.class})
class BringerOfTheWhiteDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for the five-color alternate cost")
    void castsForAlternateCost() {
        harness.setHand(player1, List.of(new BringerOfTheWhiteDawn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bringer of the White Dawn");
    }

    @Test
    @DisplayName("Upkeep trigger offers an artifact card in the graveyard as an optional target")
    void upkeepOffersGraveyardArtifact() {
        harness.addToBattlefield(player1, new BringerOfTheWhiteDawn());
        Card artifact = new AnodetLurker();
        harness.setGraveyard(player1, List.of(artifact));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        assertThat(choice.minCount()).isZero();
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chosen artifact card returns from the graveyard to the battlefield")
    void returnsChosenArtifactToBattlefield() {
        harness.addToBattlefield(player1, new BringerOfTheWhiteDawn());
        Card artifact = new AnodetLurker();
        harness.setGraveyard(player1, List.of(artifact));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Anodet Lurker");
        harness.assertNotInGraveyard(player1, "Anodet Lurker");
    }

    @Test
    @DisplayName("Declining the optional return leaves the artifact in the graveyard")
    void mayDeclineArtifactReturn() {
        harness.addToBattlefield(player1, new BringerOfTheWhiteDawn());
        Card artifact = new AnodetLurker();
        harness.setGraveyard(player1, List.of(artifact));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Anodet Lurker");
        harness.assertNotOnBattlefield(player1, "Anodet Lurker");
    }

    @Test
    @DisplayName("The upkeep trigger does not offer artifacts from an opponent's graveyard")
    void opponentArtifactIsNotATarget() {
        harness.addToBattlefield(player1, new BringerOfTheWhiteDawn());
        harness.setGraveyard(player2, List.of(new AnodetLurker()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Anodet Lurker");
    }

    @Test
    @DisplayName("The upkeep trigger only occurs during its controller's upkeep")
    void onlyTriggersDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new BringerOfTheWhiteDawn());
        harness.setGraveyard(player1, List.of(new AnodetLurker()));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Anodet Lurker");
    }

    @Test
    @DisplayName("Nonartifact cards in the graveyard are not legal targets")
    void nonArtifactIsNotATarget() {
        harness.addToBattlefield(player1, new BringerOfTheWhiteDawn());
        harness.setGraveyard(player1, List.of(new EbonDrake()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Ebon Drake");
    }
}
