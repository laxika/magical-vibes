package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisorderInTheCourt.class, GrizzlyBears.class, FountainOfYouth.class, EsixFractalBloom.class})
class DisorderInTheCourtTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles X creatures, investigates X times, and returns them tapped at the next end step")
    void exilesInvestigatesAndReturnsTapped() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondOwnCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(2);

        harness.castInstantForX(player1, 0, 2,
                List.of(ownCreature.getId(), secondOwnCreature.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).hasSize(2);

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("X=0 investigates zero times and exiles no creatures")
    void xZeroDoesNothing() {
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(0);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(fountain.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Returns opposing creatures tapped under their owner's control")
    void returnsOpposingCreaturesToOwner() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(1);

        harness.castInstantForX(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();

        advanceToEndStep();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1).allMatch(Permanent::isTapped);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Investigates the full X when only some targets remain legal")
    void partialIllegalTargetsStillInvestigateFullX() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(2);

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(2);

        advanceToEndStep();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1).allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first.getCard());
    }

    @Test
    @DisplayName("Does not investigate when all targets become illegal")
    void allIllegalTargetsPreventInvestigation() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(1);

        harness.castInstantForX(player1, 0, 1, List.of(creature.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Disorder in the Court");
    }

    @Test
    @DisplayName("Requires exactly X creature targets")
    void requiresExactlyXTargets() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The return is a delayed triggered ability that can be responded to")
    void returnUsesStackAtBeginningOfEndStep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(1);

        harness.castInstantForX(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();
        advanceToEndStep();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
        assertThat(gd.stack).hasSize(1);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Esix replaces only the first of multiple investigations")
    void investigationsCreateTokensSeparately() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new EsixFractalBloom());
        Permanent copySource = addCreatureReady(player2, new GrizzlyBears());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DisorderInTheCourt()));
        addMana(2);

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copySource.getId());

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1)
                .allMatch(permanent -> permanent.getCard().isToken());
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    private void addMana(int x) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        if (x > 0) {
            harness.addMana(player1, ManaColor.COLORLESS, x);
        }
    }

    private void advanceToEndStep() {
        harness.passUntil(TurnStep.END_STEP);
    }
}
