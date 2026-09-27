package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoomsTimePlatform.class, Forest.class, GrizzlyBears.class})
class DoomsTimePlatformTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles a target nonland card from your graveyard with two time counters")
    void attackingExilesTargetNonlandCardFromYourGraveyard() {
        Forest land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(land, nonland));
        addAttackTriggerSourceAndAttacker();

        declareAttackers(List.of(1));

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(nonland.getId());

        harness.handleMultipleCardsChosen(player1, List.of(nonland.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(nonland.getId());
        assertThat(gd.exiledCardTimeCounters).containsEntry(nonland.getId(), 2);
    }

    @Test
    @DisplayName("The suspended card counts down and can be cast for free")
    void suspendedCardCanBeCastForFree() {
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonland));
        addAttackTriggerSourceAndAttacker();

        declareAttackers(List.of(1));
        harness.handleMultipleCardsChosen(player1, List.of(nonland.getId()));
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(nonland.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A land is not a legal target")
    void landCannotBeTargeted() {
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        addAttackTriggerSourceAndAttacker();

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.exiledCardTimeCounters).isEmpty();
    }

    @Test
    @DisplayName("The required graveyard target cannot be declined")
    void cannotDeclineRequiredTarget() {
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonland));
        addAttackTriggerSourceAndAttacker();

        declareAttackers(List.of(1));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void addAttackTriggerSourceAndAttacker() {
        harness.addToBattlefield(player1, new DoomsTimePlatform());
        addCreatureReady(player1, new GrizzlyBears());
    }
}
