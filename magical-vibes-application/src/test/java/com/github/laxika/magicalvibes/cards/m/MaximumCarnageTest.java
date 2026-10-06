package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LurkingLizards;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MaximumCarnage.class, LurkingLizards.class})
class MaximumCarnageTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I requires the Saga controller's creature to attack")
    void chapterIRequiresControllerCreatureToAttack() {
        castAndResolveChapterI();
        addCreatureReady(player1, new LurkingLizards());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Chapter I requires opposing creatures that enter later to attack")
    void chapterIRequiresLaterOpposingCreatureToAttack() {
        castAndResolveChapterI();
        addCreatureReady(player2, new LurkingLizards());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Chapter I attack requirements expire at the beginning of the controller's next turn")
    void chapterIAttackRequirementsExpireAtNextTurn() {
        castAndResolveChapterI();
        addCreatureReady(player2, new LurkingLizards());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackers(player2, List.of());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Chapter I does not make creatures goaded")
    void chapterIDoesNotMakeCreaturesGoaded() {
        Permanent existingCreature = addCreatureReady(player2, new LurkingLizards());
        castAndResolveChapterI();
        Permanent laterCreature = addCreatureReady(player1, new LurkingLizards());

        assertThat(harness.getGameQueryService().isGoaded(gd, existingCreature)).isFalse();
        assertThat(harness.getGameQueryService().isGoaded(gd, laterCreature)).isFalse();
    }

    @Test
    @DisplayName("Chapter I does not force a tapped creature to attack")
    void chapterIDoesNotForceTappedCreatureToAttack() {
        castAndResolveChapterI();
        Permanent creature = addCreatureReady(player2, new LurkingLizards());
        creature.tap();

        declareAttackers(player2, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Chapter I does not let a creature with summoning sickness attack")
    void chapterIRespectsSummoningSickness() {
        castAndResolveChapterI();
        harness.addToBattlefield(player2, new LurkingLizards());

        declareAttackers(player2, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("In a two-player game an opposing creature may attack the Saga's controller")
    void chapterIAllowsOpponentToAttackController() {
        castAndResolveChapterI();
        Permanent creature = addCreatureReady(player2, new LurkingLizards());

        declareAttackers(player2, List.of(0));

        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Chapter II adds three red mana")
    void chapterIIAddsThreeRedMana() {
        addSagaWithLore(1);
        advanceToNextChapter();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Chapter III deals five damage to each opponent")
    void chapterIIIDealsFiveDamageToOpponent() {
        addSagaWithLore(2);
        harness.setLife(player2, 20);
        advanceToNextChapter();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Chapter III leaves the controller unharmed and sacrifices the Saga after resolution")
    void chapterIIISacrificesSagaWithoutDamagingController() {
        addSagaWithLore(2);
        harness.setLife(player1, 20);

        advanceToNextChapter();

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Maximum Carnage");
        harness.assertInGraveyard(player1, "Maximum Carnage");
    }

    private void castAndResolveChapterI() {
        harness.setHand(player1, List.of(new MaximumCarnage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new MaximumCarnage());
        saga.setCounterCount(CounterType.LORE, loreCounters);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
