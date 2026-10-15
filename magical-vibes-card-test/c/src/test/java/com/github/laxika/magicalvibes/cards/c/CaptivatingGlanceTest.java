package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptivatingGlance.class, DeeptreadMerrow.class, Forest.class})
class CaptivatingGlanceTest extends BaseCardTest {

    /** Attaches Captivating Glance (controlled by {@code auraController}) to {@code creature}. */
    private Permanent attachGlance(Player auraController, Permanent creature) {
        Permanent glance = new Permanent(new CaptivatingGlance());
        glance.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(glance);
        return glance;
    }

    /** Runs player1 through their end step so the controller-end-step clash trigger resolves. */
    private void runPlayer1EndStep() {
        advancePlayer1ToEndStep();
        resolveClash();
    }

    private void resolveClash() {
        resolveAllTriggers();
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry scry) {
            Player chooser = scry.playerId().equals(player1.getId()) ? player1 : player2;
            gs.handleInteractionAnswer(gd, chooser, new InteractionAnswer.ScryOrder(
                    java.util.List.of(0), java.util.List.of()));
        }
    }

    /** Advances player1 to the end step without resolving the triggered ability. */
    private void advancePlayer1ToEndStep() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // POSTCOMBAT_MAIN -> END_STEP, clash trigger onto stack
    }

    private Permanent addCreature(Player owner) {
        return addCreatureReady(owner, new DeeptreadMerrow());
    }

    @Test
    @DisplayName("Winning the clash gives the controller control of the enchanted creature")
    void wonClashGainsControl() {
        Permanent creature = addCreature(player2);
        attachGlance(player1, creature);

        // player1 reveals higher mana value (Deeptread Merrow MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new DeeptreadMerrow());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        runPlayer1EndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Losing the clash gives the opponent control of the enchanted creature")
    void lostClashOpponentGainsControl() {
        Permanent creature = addCreature(player1);
        attachGlance(player1, creature);

        // player1 reveals lower mana value (Forest MV 0 < Deeptread Merrow MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new DeeptreadMerrow());

        runPlayer1EndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("A tie counts as 'otherwise', so the opponent gains control")
    void tiedClashOpponentGainsControl() {
        Permanent creature = addCreature(player1);
        attachGlance(player1, creature);

        // Equal mana values (both Deeptread Merrow MV 2) → no one wins the clash (CR 701.30d).
        gd.playerDecks.get(player1.getId()).addFirst(new DeeptreadMerrow());
        gd.playerDecks.get(player2.getId()).addFirst(new DeeptreadMerrow());

        runPlayer1EndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Does not clash on the opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        Permanent creature = addCreature(player2);
        attachGlance(player1, creature);

        // Even though player1 would win, it is player2's end step, so nothing happens.
        gd.playerDecks.get(player1.getId()).addFirst(new DeeptreadMerrow());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("The control change persists after Captivating Glance leaves the battlefield")
    void controlChangePersistsAfterAuraLeaves() {
        Permanent creature = addCreature(player2);
        Permanent glance = attachGlance(player1, creature);

        gd.playerDecks.get(player1.getId()).addFirst(new DeeptreadMerrow());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        runPlayer1EndStep();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, glance));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Still clashes when the Aura leaves before its triggered ability resolves")
    void stillClashesWhenAuraLeavesBeforeResolution() {
        Permanent creature = addCreature(player2);
        Permanent glance = attachGlance(player1, creature);

        gd.playerDecks.get(player1.getId()).addFirst(new DeeptreadMerrow());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        advancePlayer1ToEndStep();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, glance));
        resolveClash();

        assertThat(gameLogContains("clashes")).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Still clashes when the enchanted creature leaves before resolution")
    void stillClashesWhenCreatureLeavesBeforeResolution() {
        Permanent creature = addCreature(player2);
        attachGlance(player1, creature);
        harness.setLibrary(player1, java.util.List.of(new DeeptreadMerrow()));
        harness.setLibrary(player2, java.util.List.of(new Forest()));

        advancePlayer1ToEndStep();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        resolveClash();

        assertThat(gameLogContains("clashes")).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Clashing players may choose whether to put their revealed card on the bottom")
    void clashOffersLibraryPlacementChoice() {
        Permanent creature = addCreature(player2);
        attachGlance(player1, creature);
        harness.setLibrary(player1, java.util.List.of(new DeeptreadMerrow(), new Forest()));
        harness.setLibrary(player2, java.util.List.of(new Forest(), new DeeptreadMerrow()));

        advancePlayer1ToEndStep();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("With both libraries empty, neither player wins and the opponent gains control")
    void bothLibrariesEmptyOpponentGainsControl() {
        Permanent creature = addCreature(player1);
        attachGlance(player1, creature);
        harness.setLibrary(player1, java.util.List.of());
        harness.setLibrary(player2, java.util.List.of());

        runPlayer1EndStep();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }
}
