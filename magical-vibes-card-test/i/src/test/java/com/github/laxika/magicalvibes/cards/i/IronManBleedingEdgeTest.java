package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HERBIELovableRobot;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronManBleedingEdge.class, GrizzlyBears.class, HERBIELovableRobot.class, SolRing.class})
class IronManBleedingEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an artifact spell as a nonlegendary token only once each turn")
    void copiesArtifactSpellAsNonlegendaryTokenOnlyOnceEachTurn() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new IronManBleedingEdge());
        harness.setHand(player1, List.of(legendaryArtifact("Test Artifact"), legendaryArtifact("Second Artifact")));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        List<Permanent> copies = findPermanents(player1, "Test Artifact");
        assertThat(copies).hasSize(2);
        assertThat(copies).filteredOn(permanent -> permanent.getCard().isToken()).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().getSupertypes())
                        .doesNotContain(CardSupertype.LEGENDARY));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the copy does not use the once-per-turn allowance")
    void decliningCopyDoesNotUseAllowance() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new IronManBleedingEdge());
        harness.setHand(player1, List.of(legendaryArtifact("First Artifact"), legendaryArtifact("Second Artifact")));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Does not trigger for a nonartifact spell")
    void doesNotTriggerForNonartifactSpell() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new IronManBleedingEdge());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The copy choice is made when the triggered ability resolves")
    void copyChoiceIsMadeOnResolution() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new IronManBleedingEdge());

        harness.castFromHand(player1, new SolRing(), "{1}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(2);
    }

    @Test
    @DisplayName("A real legendary artifact creature is copied as a nonlegendary token")
    void copiesLegendaryArtifactCreature() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new IronManBleedingEdge());
        harness.castFromHand(player1, new HERBIELovableRobot(), "{2}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "H.E.R.B.I.E., Lovable Robot")).hasSize(2)
                .filteredOn(permanent -> permanent.getCard().isToken()).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().getSupertypes())
                        .doesNotContain(CardSupertype.LEGENDARY));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's artifact spell is not copied")
    void doesNotCopyOpponentsArtifact() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new IronManBleedingEdge());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SolRing(), "{1}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player2, "Sol Ring")).hasSize(1);
        assertThat(findPermanents(player1, "Sol Ring")).isEmpty();
    }

    @Test
    @DisplayName("The copy allowance resets on the next turn")
    void copyAllowanceResetsNextTurn() {
        prepareMainPhase();
        harness.addToBattlefield(player1, new IronManBleedingEdge());
        harness.setLibrary(player1, List.of(new SolRing(), new SolRing()));
        harness.setLibrary(player2, List.of(new SolRing(), new SolRing()));
        harness.castFromHand(player1, new SolRing(), "{1}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Sol Ring")).hasSize(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SolRing(), "{1}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sol Ring")).hasSize(4);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Card legendaryArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return card;
    }
}
