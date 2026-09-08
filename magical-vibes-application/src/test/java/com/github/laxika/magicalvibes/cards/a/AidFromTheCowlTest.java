package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AidFromTheCowlTest extends BaseCardTest {

    @Test
    @DisplayName("Revolt puts a revealed permanent card onto the battlefield when accepted")
    void putsPermanentOntoBattlefield() {
        addAidAndRevoltSetup();
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("A nonpermanent card may be put on the bottom")
    void nonPermanentMayGoToBottom() {
        addAidAndRevoltSetup();
        var instant = new Shock();
        var bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(instant, bears));

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(instant);
    }

    @Test
    @DisplayName("Declining the nonpermanent bottom choice leaves it on top")
    void declineNonPermanentBottomChoice() {
        addAidAndRevoltSetup();
        var instant = new Shock();
        harness.setLibrary(player1, List.of(instant));

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(instant);
    }

    @Test
    @DisplayName("Does not trigger without revolt")
    void doesNotTriggerWithoutRevolt() {
        harness.addToBattlefield(player1, new AidFromTheCowl());
        var forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        advanceToEndStep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(forest);
    }

    private void addAidAndRevoltSetup() {
        harness.addToBattlefield(player1, new AidFromTheCowl());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
