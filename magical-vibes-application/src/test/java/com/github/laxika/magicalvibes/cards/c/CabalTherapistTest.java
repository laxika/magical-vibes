package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.cards.d.Defile;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalTherapist.class, UniversalAutomaton.class, Defile.class, SnowCoveredForest.class})
class CabalTherapistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature names a nonland card and discards all matching cards")
    void sacrificesCreatureAndDiscardsMatchingCards() {
        addCabalTherapist();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UniversalAutomaton());
        List<Card> targetHand = new ArrayList<>(List.of(
                new UniversalAutomaton(), new UniversalAutomaton(), new Defile(), new SnowCoveredForest()));
        harness.setHand(player2, targetHand);

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Universal Automaton", "Defile").doesNotContain("Snow-Covered Forest");
        harness.handleListChoice(player1, "Universal Automaton");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(targetHand.get(2), targetHand.get(3));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(targetHand.get(0), targetHand.get(1));
        harness.assertNotOnBattlefield(player1, "Universal Automaton");
    }

    @Test
    @DisplayName("Declining the sacrifice does not discard from the target's hand")
    void decliningSacrificeDoesNothing() {
        addCabalTherapist();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UniversalAutomaton());
        harness.setHand(player2, List.of(new UniversalAutomaton(), new Defile()));

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("The ability does nothing when no creature can be sacrificed")
    void noCreatureToSacrificeDoesNothing() {
        Permanent therapist = harness.addToBattlefieldAndReturn(player1, new CabalTherapist());
        harness.setHand(player2, List.of(new UniversalAutomaton(), new Defile()));

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(therapist);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cabal Therapist can sacrifice itself and target its controller")
    void canSacrificeItselfAndTargetController() {
        Permanent therapist = harness.addToBattlefieldAndReturn(player1, new CabalTherapist());
        Card matching = new UniversalAutomaton();
        Card other = new Defile();
        harness.setHand(player1, List.of(matching, other));

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, therapist.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matching, other);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Universal Automaton");

        harness.assertNotOnBattlefield(player1, "Cabal Therapist");
        harness.assertInGraveyard(player1, "Cabal Therapist");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(matching);
    }

    @Test
    @DisplayName("Naming a card absent from the hand discards nothing")
    void namingAbsentCardDiscardsNothing() {
        addCabalTherapist();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UniversalAutomaton());
        Card handCard = new Defile();
        harness.setHand(player2, List.of(handCard));

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cabal Therapist");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Any nonland Oracle card name is legal even outside the implemented catalog")
    void canNameNonlandCardOutsideCatalog() {
        addCabalTherapist();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UniversalAutomaton());
        Card handCard = new Defile();
        harness.setHand(player2, List.of(handCard));

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Shichifukujin Dragon");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty target hand still allows naming a nonland card")
    void canTargetEmptyHand() {
        Permanent therapist = harness.addToBattlefieldAndReturn(player1, new CabalTherapist());
        harness.setHand(player2, List.of());

        advanceToFirstMainPhase();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, therapist.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cabal Therapist");

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability does not trigger during the opponent's first main phase")
    void doesNotTriggerOnOpponentsTurn() {
        addCabalTherapist();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Cabal Therapist");
    }

    private void addCabalTherapist() {
        harness.addToBattlefield(player1, new CabalTherapist());
    }

    private void advanceToFirstMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }
}
