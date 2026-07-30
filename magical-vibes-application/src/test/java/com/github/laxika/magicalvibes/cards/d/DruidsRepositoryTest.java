package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DruidsRepositoryTest extends BaseCardTest {

    @Test
    @DisplayName("Each attacking creature you control puts a charge counter on the enchantment")
    void attackersAddChargeCounters() {
        Permanent repository = addRepository();

        Permanent bears1 = addReadyBears();
        Permanent bears2 = addReadyBears();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1, 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(repository.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(bears1.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(bears2.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Opponent's attackers do not add charge counters")
    void opponentAttackersDoNotTrigger() {
        Permanent repository = addRepository();

        Permanent oppBears = new Permanent(new GrizzlyBears());
        oppBears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppBears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));
        harness.passBothPriorities();

        assertThat(repository.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Removing a charge counter adds one mana of the chosen color")
    void removeCounterAddsChosenColorMana() {
        Permanent repository = addRepository();
        repository.setCounterCount(CounterType.CHARGE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(before + 1);
        assertThat(repository.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        // No {T} in the cost — the enchantment stays untapped.
        assertThat(repository.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the mana ability with no charge counters")
    void cannotActivateWithoutCounters() {
        addRepository();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addRepository() {
        Permanent perm = new Permanent(new DruidsRepository());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        return bears;
    }
}
