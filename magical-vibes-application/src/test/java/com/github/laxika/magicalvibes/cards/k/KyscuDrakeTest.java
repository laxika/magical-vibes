package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpittingDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KyscuDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +0/+1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent drake = addCreatureReady(player1, new KyscuDrake());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getPowerModifier()).isEqualTo(0);
        assertThat(drake.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate pump more than once each turn")
    void cannotActivatePumpMoreThanOncePerTurn() {
        addCreatureReady(player1, new KyscuDrake());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Pump resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent drake = addCreatureReady(player1, new KyscuDrake());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drake.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(drake.getPowerModifier()).isEqualTo(0);
        assertThat(drake.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Tutor ability can't be activated without Spitting Drake")
    void tutorRequiresSpittingDrake() {
        addCreatureReady(player1, new KyscuDrake());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Sacrificing with Spitting Drake searches out Viashivan Dragon onto the battlefield")
    void tutorPutsViashivanDragonOntoBattlefield() {
        addCreatureReady(player1, new KyscuDrake());
        UUID spittingId = harness.addToBattlefieldAndReturn(player1, new SpittingDrake()).getId();

        Card dragon = new Card();
        dragon.setName("Viashivan Dragon");
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(dragon, new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, spittingId);

        harness.assertInGraveyard(player1, "Kyscu Drake");
        harness.assertInGraveyard(player1, "Spitting Drake");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Viashivan Dragon"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Viashivan Dragon");
    }
}
