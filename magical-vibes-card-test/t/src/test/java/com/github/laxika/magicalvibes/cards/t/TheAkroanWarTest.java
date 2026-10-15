package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheAkroanWar.class, Confiscate.class, GiantSpider.class, GrizzlyBears.class, HillGiant.class, WallOfSwords.class})
class TheAkroanWarTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I gains control of an opponent's creature")
    void chapterIGainsControlOfOpponentsCreature() {
        Permanent saga = addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentCreature.getId(), ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    @DisplayName("Chapter II forces opposing creatures to attack until your next turn")
    void chapterIIForcesOpposingCreaturesToAttack() {
        addSagaWithLore(1);
        triggerNextChapter();
        resolveAllTriggers();

        addCreatureReady(player2, new GrizzlyBears());
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Chapter III damages only tapped creatures based on their power")
    void chapterIIIDamagesTappedCreatures() {
        addSagaWithLore(2);
        Permanent tappedWall = addCreatureReady(player1, new WallOfSwords());
        tappedWall.tap();
        Permanent tappedSpider = addCreatureReady(player2, new GiantSpider());
        tappedSpider.tap();
        Permanent untappedGiant = addCreatureReady(player2, new HillGiant());

        triggerNextChapter();
        resolveAllTriggers();

        assertThat(tappedWall.getMarkedDamage()).isEqualTo(3);
        assertThat(tappedSpider.getMarkedDamage()).isEqualTo(2);
        assertThat(untappedGiant.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "The Akroan War");
    }

    @Test
    @DisplayName("Chapter I can target a creature you already control")
    void chapterICanTargetOwnCreature() {
        addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(ownCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
    }

    @Test
    @DisplayName("Chapter I control continues when the Saga changes controller")
    void chapterIControlContinuesWhenSagaChangesController() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        triggerNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castEnchantment(player2, 0, saga.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(saga);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Chapter I control ends when the Saga leaves the battlefield")
    void chapterIControlEndsWhenSagaLeaves() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        triggerNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, saga));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Chapter I does nothing if the Saga leaves before resolution")
    void chapterIDoesNothingIfSagaLeavesBeforeResolution() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        triggerNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, saga));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Chapter II requires attacks without goading creatures")
    void chapterIIDoesNotGoadCreatures() {
        addSagaWithLore(1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        triggerNextChapter();
        resolveAllTriggers();

        assertThat(gqs.isGoaded(gd, creature)).isFalse();
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Chapter II does not force tapped creatures or defenders to attack")
    void chapterIIOnlyRequiresAttacksWhenAble() {
        addSagaWithLore(1);
        Permanent tappedCreature = addCreatureReady(player2, new GrizzlyBears());
        tappedCreature.tap();
        addCreatureReady(player2, new WallOfSwords());
        harness.addToBattlefield(player2, new HillGiant());

        triggerNextChapter();
        resolveAllTriggers();

        declareAttackers(player2, List.of());
        assertThat(tappedCreature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Chapter II does not force your own creatures to attack")
    void chapterIIDoesNotRequireOwnCreaturesToAttack() {
        addSagaWithLore(1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        triggerNextChapter();
        resolveAllTriggers();

        declareAttackers(player1, List.of());
        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The stolen creature returns only after chapter III resolves")
    void stolenCreatureReturnsAfterFinalChapter() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        triggerNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();
        saga.setCounterCount(CounterType.LORE, 2);
        creature.tap();

        triggerNextChapter();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, saga);
        resolveAllTriggers();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, saga);
        harness.assertInGraveyard(player1, "The Akroan War");
    }

    @Test
    @DisplayName("Chapter III kills tapped creatures with lethal self-damage")
    void chapterIIIKillsTappedCreaturesWithLethalDamage() {
        addSagaWithLore(2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        bears.tap();
        giant.tap();

        triggerNextChapter();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "The Akroan War");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheAkroanWar());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
