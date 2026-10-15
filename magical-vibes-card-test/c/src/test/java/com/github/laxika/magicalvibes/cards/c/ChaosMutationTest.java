package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosMutation.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class, SoulWarden.class, Clone.class})
class ChaosMutationTest extends BaseCardTest {

    @Test
    void exilesTargetsAndReplacesEachFromItsControllersLibrary() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        FountainOfYouth firstBottomCard = new FountainOfYouth();
        FountainOfYouth secondBottomCard = new FountainOfYouth();
        harness.setLibrary(player1, List.of(firstBottomCard, new LlanowarElves()));
        harness.setLibrary(player2, List.of(secondBottomCard, new GrizzlyBears()));
        castChaosMutation(List.of(bear.getId(), elves.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bear.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(elves.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstBottomCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondBottomCard);
    }

    @Test
    void rejectsTwoCreaturesControlledByTheSamePlayer() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    void rejectsNonCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void mayBeCastWithNoTargets() {
        prepareCard();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void replacesCreaturesInActivePlayerOrderRegardlessOfTargetOrder() {
        harness.forceActivePlayer(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setLibrary(player1, List.of(new SoulWarden()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        castChaosMutation(List.of(elves.getId(), bear.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Soul Warden");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 21);
    }

    @Test
    void putsOnlyRevealedNoncreaturesBelowTheUnrevealedLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        FountainOfYouth first = new FountainOfYouth();
        FountainOfYouth second = new FountainOfYouth();
        GrizzlyBears unrevealed = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, new LlanowarElves(), unrevealed));

        castChaosMutation(List.of(target.getId()));

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(unrevealed);
        assertThat(gd.playerDecks.get(player2.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void exilesCreatureEvenWhenItsControllersLibraryIsEmpty() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of());

        castChaosMutation(List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void returnsEntireLibraryWhenNoCreatureIsFound() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        FountainOfYouth first = new FountainOfYouth();
        FountainOfYouth second = new FountainOfYouth();
        harness.setLibrary(player2, List.of(first, second));

        castChaosMutation(List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void replacesOnlyTheTargetStillLegalOnResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        LlanowarElves untouchedLibraryCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(untouchedLibraryCard));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        prepareCard();
        harness.castInstant(player1, 0, List.of(bear.getId(), elves.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bear));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouchedLibraryCard);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(elves.getCard());
    }

    @Test
    void completesEnteringCloneChoiceBeforeRevealingForTheNextPlayer() {
        harness.forceActivePlayer(player1);
        Permanent original = harness.addToBattlefieldAndReturn(player1, new SoulWarden());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Clone clone = new Clone();
        GrizzlyBears replacement = new GrizzlyBears();
        harness.setLibrary(player1, List.of(clone));
        harness.setLibrary(player2, List.of(replacement));

        castChaosMutation(List.of(elves.getId(), bear.getId()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(replacement);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Soul Warden"))).hasSize(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private void castChaosMutation(List<UUID> targetIds) {
        prepareCard();
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new ChaosMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
