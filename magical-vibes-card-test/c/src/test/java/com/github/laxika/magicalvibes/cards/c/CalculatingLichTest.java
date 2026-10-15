package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CalculatingLich.class, GrizzlyBears.class})
class CalculatingLichTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature attacking an opponent makes that player lose 1 life")
    void creaturesAttackingOpponentCauseLifeLoss() {
        addCreatureReady(player1, new CalculatingLich());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A creature attacking the Lich controller does not trigger it")
    void creatureAttackingLichControllerDoesNotTrigger() {
        addCreatureReady(player1, new CalculatingLich());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Lich triggers for its own attack even if it leaves before resolution")
    void ownAttackTriggerResolvesAfterLichLeavesBattlefield() {
        Permanent lich = addCreatureReady(player1, new CalculatingLich());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(lich);
        gd.playerGraveyards.get(player1.getId()).add(lich.getCard());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Lich triggers independently for the same attacking creature")
    void multipleLichesEachCauseLifeLoss() {
        addCreatureReady(player1, new CalculatingLich());
        addCreatureReady(player1, new CalculatingLich());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(2));
        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Menace prevents the Lich from being blocked by only one creature")
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new CalculatingLich());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Two blockers are legal and do not prevent the attack trigger's life loss")
    void twoBlockersAreLegalAndDoNotPreventLifeLoss() {
        addCreatureReady(player1, new CalculatingLich());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
