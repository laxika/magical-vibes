package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndergrowthRecon.class, Forest.class, GrizzlyBears.class})
class UndergrowthReconTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target land from the graveyard tapped at upkeep")
    void returnsTargetLandTapped() {
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new UndergrowthRecon());
        harness.setGraveyard(player1, List.of(forest));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        Permanent returnedForest = findPermanent(player1, "Forest");
        assertThat(returnedForest.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Does not target a nonland card in the graveyard")
    void onlyTargetsLands() {
        harness.addToBattlefield(player1, new UndergrowthRecon());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new UndergrowthRecon());
        harness.setGraveyard(player1, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot return a land from an opponent's graveyard")
    void doesNotTargetOpponentsLand() {
        harness.addToBattlefield(player1, new UndergrowthRecon());
        harness.setGraveyard(player2, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Returns only the selected land when multiple lands are available")
    void returnsOnlySelectedLand() {
        Forest selected = new Forest();
        Forest other = new Forest();
        harness.addToBattlefield(player1, new UndergrowthRecon());
        harness.setGraveyard(player1, List.of(other, selected));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, selected);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getCard().getId()).isEqualTo(selected.getId());
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not choose a replacement when the targeted land leaves the graveyard")
    void doesNotRetargetMissingLand() {
        Forest selected = new Forest();
        Forest other = new Forest();
        harness.addToBattlefield(player1, new UndergrowthRecon());
        harness.setGraveyard(player1, List.of(selected, other));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
