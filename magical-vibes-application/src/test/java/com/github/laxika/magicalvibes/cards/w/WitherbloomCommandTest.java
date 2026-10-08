package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitherbloomCommand.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, HowlingMine.class, JayemdaeTome.class, LlanowarElves.class})
class WitherbloomCommandTest extends BaseCardTest {

    @Test
    void millsReturnsLandAndDrainsOpponent() {
        var forest = new Forest();
        harness.setGraveyard(player1, List.of(forest, new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new HillGiant(), new LlanowarElves()));
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3}, List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertInHand(player1, forest.getName());
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void destroysEligibleNoncreatureNonlandPermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3},
                List.of(fountain.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertLife(player2, 18);
    }

    @Test
    void givesTargetCreatureMinusThreeMinusOneUntilEndOfTurn() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2},
                List.of(fountain.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(0);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void rejectsInvalidDestroyTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2,
                new int[]{1, 3}, List.of(creature.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsLandJustMilledFromOwnLibrary() {
        var forest = new Forest();
        var bears = new GrizzlyBears();
        var giant = new HillGiant();
        var remaining = new Forest();
        harness.setLibrary(player1, List.of(forest, bears, giant, remaining));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3},
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears, giant).doesNotContain(forest);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotDeclineReturningAvailableLand() {
        var forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3},
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void emptyLibraryAndNoLandDoNotPreventDrain() {
        var opponentsLand = new Forest();
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(opponentsLand));
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3},
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsLand);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Witherbloom Command");
    }

    @Test
    void minusOneToughnessKillsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3},
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void resolvesDrainWhenDestroyTargetLeavesBattlefield() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3},
                List.of(fountain.getId(), player2.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(fountain);
        gd.playerHands.get(player2.getId()).add(fountain.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(fountain.getCard());
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Witherbloom Command");
    }

    @Test
    void rejectsLandForDestroyMode() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2,
                new int[]{1, 3}, List.of(forest.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsControllerForOpponentLifeLossMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2,
                new int[]{2, 3}, List.of(creature.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysPermanentAtManaValueLimit() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3},
                List.of(mine.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Howling Mine");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void rejectsNoncreatureNonlandAboveManaValueLimit() {
        Permanent tome = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2,
                new int[]{1, 3}, List.of(tome.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNothingWhenAllTargetsLeaveBattlefield() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2},
                List.of(fountain.getId(), creature.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(fountain, creature));
        gd.playerHands.get(player2.getId()).addAll(List.of(fountain.getCard(), creature.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(fountain.getCard(), creature.getCard());
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Witherbloom Command");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotReturnNonlandFromGraveyard() {
        var bears = new GrizzlyBears();
        var forest = new Forest();
        harness.setGraveyard(player1, List.of(bears, forest));
        harness.setLibrary(player2, List.of(new HillGiant()));
        harness.setHand(player1, List.of(new WitherbloomCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3},
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears).doesNotContain(forest);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
