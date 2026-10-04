package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.ThrabenValiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FerventCathar.class, ThrabenValiant.class})
class FerventCatharTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes target creature unable to block this turn")
    void etbMakesTargetUnableToBlock() {
        Permanent blocker = addCreatureReady(player2, new ThrabenValiant());
        harness.setHand(player1, List.of(new FerventCathar()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = blocker.getId();
        harness.castCreature(player1, 0, 0, targetId);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);

        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Target creature cannot declare as blocker after ETB resolves")
    void targetCannotDeclareAsBlocker() {
        Permanent attacker = addCreatureReady(player1, new ThrabenValiant());
        addCreatureReady(player2, new ThrabenValiant());

        harness.setHand(player1, List.of(new FerventCathar()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Thraben Valiant");
        harness.castCreature(player1, 0, 0, targetId);

        resolveAllTriggers();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entering an empty battlefield requires targeting Cathar itself")
    void targetsItselfWhenEnteringEmptyBattlefield() {
        harness.setHand(player1, List.of(new FerventCathar()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fervent Cathar");
        Permanent cathar = findPermanent(player1, "Fervent Cathar");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, cathar.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(cathar.getId());
        harness.passBothPriorities();
        assertThat(cathar.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new ThrabenValiant());
        harness.setHand(player1, List.of(new FerventCathar()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Thraben Valiant");
        harness.castCreature(player1, 0, 0, targetId);

        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Can target its controller's creature without affecting other creatures")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new ThrabenValiant());
        Permanent other = addCreatureReady(player2, new ThrabenValiant());
        harness.setHand(player1, List.of(new FerventCathar()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        assertThat(findPermanent(player1, "Fervent Cathar").isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Blocking restriction expires at the end of the turn")
    void blockingRestrictionExpires() {
        Permanent target = addCreatureReady(player2, new ThrabenValiant());
        harness.setHand(player1, List.of(new FerventCathar()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The trigger resolves even if Cathar leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent target = addCreatureReady(player2, new ThrabenValiant());
        harness.setHand(player1, List.of(new FerventCathar()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Haste allows Cathar to attack the turn it enters")
    void canAttackImmediately() {
        Permanent target = addCreatureReady(player2, new ThrabenValiant());
        harness.setHand(player1, List.of(new FerventCathar()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(findPermanent(player1, "Fervent Cathar").isAttacking()).isTrue();
    }
}
