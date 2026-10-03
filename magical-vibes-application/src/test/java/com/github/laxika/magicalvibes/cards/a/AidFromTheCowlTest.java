package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ConsulateCrackdown;
import com.github.laxika.magicalvibes.cards.c.Conviction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AidFromTheCowl.class, Forest.class, GrizzlyBears.class, Shock.class, ZuranOrb.class,
        Conviction.class, ConsulateCrackdown.class})
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

    @Test
    @DisplayName("An opponent's permanent leaving does not enable revolt")
    void opponentsPermanentLeavingDoesNotEnableRevolt() {
        harness.addToBattlefield(player1, new AidFromTheCowl());
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        var forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        advanceToEndStep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Revolt does not trigger Aid during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        addAidAndRevoltSetup();
        var forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Declining a permanent leaves it on top without offering to bottom it")
    void declinePermanentLeavesItOnTop() {
        addAidAndRevoltSetup();
        var forest = new Forest();
        var instant = new Shock();
        harness.setLibrary(player1, List.of(forest, instant));

        advanceToEndStep();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, instant);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library produces no choice")
    void emptyLibraryDoesNothing() {
        addAidAndRevoltSetup();
        harness.setLibrary(player1, List.of());

        advanceToEndStep();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A revealed noncreature permanent triggers its enters ability")
    void nonCreaturePermanentTriggersEntersAbility() {
        addAidAndRevoltSetup();
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.setLibrary(player1, List.of(new ConsulateCrackdown()));

        advanceToEndStep();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Consulate Crackdown");
        harness.assertNotOnBattlefield(player2, "Zuran Orb");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Zuran Orb"));
    }

    @Test
    @DisplayName("A revealed Aura with no legal recipient stays in the library")
    void auraWithoutLegalRecipientStaysInLibrary() {
        addAidAndRevoltSetup();
        var aura = new Conviction();
        harness.setLibrary(player1, List.of(aura));

        advanceToEndStep();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        harness.assertNotOnBattlefield(player1, "Conviction");
        harness.assertNotInGraveyard(player1, "Conviction");
    }

    @Test
    @DisplayName("The controller chooses what a revealed Aura enchants as it enters")
    void auraEntersAttachedToChosenCreature() {
        addAidAndRevoltSetup();
        harness.addToBattlefield(player2, new GrizzlyBears());
        var creature = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setLibrary(player1, List.of(new Conviction()));

        advanceToEndStep();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.assertOnBattlefield(player1, "Conviction");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Conviction")
                        && creature.getId().equals(permanent.getAttachedTo()));
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
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
