package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivergentTransformations.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class, SoulWarden.class})
class DivergentTransformationsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles both creatures before replacing each from its controller's library")
    void exilesBothCreaturesAndReplacesEachFromItsControllersLibrary() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        prepareCard();

        harness.setLibrary(player1, List.of(new FountainOfYouth(), new LlanowarElves()));
        harness.setLibrary(player2, List.of(new FountainOfYouth(), new GrizzlyBears()));

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Requires two different creature targets")
    void requiresTwoDifferentCreatureTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNonCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void replacesCreaturesInTurnOrderRegardlessOfTargetOrder() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.forceActivePlayer(player1);
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new SoulWarden()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        prepareCard();

        harness.castAndResolveInstant(player1, 0, List.of(second.getId(), first.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Soul Warden");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 21);
    }

    @Test
    void replacesTwoCreaturesControlledByTheSamePlayer() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new LlanowarElves(), new LlanowarElves()));
        prepareCard();

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player2, "Llanowar Elves")).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    void leavesNoncreatureCardsInLibraryWhenNoCreatureCanBeFound() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        FountainOfYouth artifact = new FountainOfYouth();
        harness.setLibrary(player1, List.of(artifact));
        harness.setLibrary(player2, List.of());
        prepareCard();

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void replacesOnlyTheTargetThatRemainsLegal() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        LlanowarElves replacement = new LlanowarElves();
        GrizzlyBears untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(replacement));
        harness.setLibrary(player2, List.of(untouched));
        prepareCard();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(second);
        gd.playerHands.get(player2.getId()).add(second.getCard());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void cannotBeCastWithOnlyOneTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void undauntedDoesNotReduceCostByMoreThanOneInATwoPlayerGame() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DivergentTransformations()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new DivergentTransformations()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
