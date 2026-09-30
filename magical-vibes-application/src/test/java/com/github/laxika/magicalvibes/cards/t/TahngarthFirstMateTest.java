package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TahngarthFirstMate.class, GrizzlyBears.class})
class TahngarthFirstMateTest extends BaseCardTest {

    @Test
    @DisplayName("It cannot be blocked by more than one creature")
    void cannotBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new TahngarthFirstMate());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(0, 0),
                new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("When tapped, it may join an opponent's attack under that opponent's control")
    void tappedTahngarthJoinsOpponentsAttack() {
        Permanent tahngarth = addCreatureReady(player1, new TahngarthFirstMate());
        tahngarth.tap();
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tahngarth);
        assertThat(tahngarth.isAttacking()).isTrue();
        assertThat(tahngarth.getAttackTarget()).isEqualTo(player1.getId());

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tahngarth);
    }

    @Test
    @DisplayName("The ability does not trigger while Tahngarth is untapped")
    void untappedTahngarthDoesNotTrigger() {
        addCreatureReady(player1, new TahngarthFirstMate());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining leaves Tahngarth under its controller's control")
    void decliningLeavesControlUnchanged() {
        Permanent tahngarth = addCreatureReady(player1, new TahngarthFirstMate());
        tahngarth.tap();
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tahngarth);
        assertThat(tahngarth.isAttacking()).isFalse();
    }
}
