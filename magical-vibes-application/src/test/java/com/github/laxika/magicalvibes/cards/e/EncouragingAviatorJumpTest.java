package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EncouragingAviatorJump.class, GrizzlyBears.class, EternalStudent.class, Island.class})
class EncouragingAviatorJumpTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever Encouraging Aviator attacks, it becomes prepared with a Jump copy in exile")
    void attackingPreparesIt() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(aviator.isPrepared()).isTrue();
        UUID copyId = aviator.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Casting the prepared Jump copy unprepares Encouraging Aviator and grants flying")
    void castingPreparedJumpUnpreparesAndGrantsFlying() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        UUID copyId = aviator.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, copyId, target.getId());
        resolveAllTriggers();

        assertThat(aviator.isPrepared()).isFalse();
        assertThat(aviator.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Attacking while already prepared does not create another Jump copy")
    void attackingWhilePreparedKeepsSameCopy() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        UUID copyId = aviator.getPreparedSpellCardId();

        harness.performUntapStep(player1);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(aviator.isPrepared()).isTrue();
        assertThat(aviator.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.exiledCards).hasSize(1);
    }

    @Test
    @DisplayName("The prepared Jump can target an opposing creature and flying expires at end of turn")
    void preparedJumpTargetsOpponentUntilEndOfTurn() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());
        Permanent target = addCreatureReady(player2, new EternalStudent());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromExile(player1, aviator.getPreparedSpellCardId(), target.getId());

        assertThat(aviator.isPrepared()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A prepared Jump requires paying its blue mana cost")
    void cannotCastPreparedJumpWithoutMana() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        UUID copyId = aviator.getPreparedSpellCardId();

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, aviator.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(aviator.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    @DisplayName("A prepared Jump cannot target a noncreature and an illegal attempt leaves it prepared")
    void invalidTargetDoesNotUnprepare() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        UUID copyId = aviator.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        assertThat(aviator.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    @DisplayName("Leaving the battlefield removes an uncast prepared Jump copy")
    void leavingBattlefieldRemovesPreparedCopy() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        UUID copyId = aviator.getPreparedSpellCardId();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aviator);

        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("Leaving the battlefield after casting Jump does not stop it resolving")
    void preparedJumpResolvesAfterAviatorLeaves() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());
        Permanent target = addCreatureReady(player1, new EternalStudent());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, aviator.getPreparedSpellCardId(), target.getId());

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aviator);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attacking again after casting Jump prepares a new copy")
    void attackingAfterCastingPreparesAgain() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        UUID firstCopyId = aviator.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, firstCopyId, aviator.getId());
        resolveAllTriggers();

        harness.performUntapStep(player1);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(aviator.isPrepared()).isTrue();
        assertThat(aviator.getPreparedSpellCardId()).isNotNull().isNotEqualTo(firstCopyId);
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.findExiledCard(firstCopyId)).isNull();
    }

    @Test
    @DisplayName("An Aviator that leaves before its attack trigger resolves does not create Jump")
    void leavingBeforeTriggerResolvesDoesNotPrepare() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());
        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aviator);
        resolveAllTriggers();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }
}
