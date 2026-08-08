package com.github.laxika.magicalvibes.cards.e;

import java.util.List;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExavaRakdosBloodWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castExava(true);

        Permanent exava = findPermanent(player1, "Exava, Rakdos Blood Witch");
        assertThat(exava.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, exava)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, exava)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining unleash leaves it without a counter")
    void decliningLeavesNoCounter() {
        castExava(false);

        assertThat(findPermanent(player1, "Exava, Rakdos Blood Witch")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An unleashed Exava can't block")
    void unleashedCantBlock() {
        Permanent exava = addCreatureReady(player1, new ExavaRakdosBloodWitch());
        exava.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Another creature you control with a +1/+1 counter gains haste")
    void counteredAllyGainsHaste() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ExavaRakdosBloodWitch());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(true);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A creature without a +1/+1 counter does not gain haste")
    void uncounteredAllyStaysSummoningSick() {
        addCreatureReady(player1, new ExavaRakdosBloodWitch());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's creature with a +1/+1 counter does not gain haste")
    void opponentCreatureUnaffected() {
        addCreatureReady(player1, new ExavaRakdosBloodWitch());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(true);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castExava(boolean unleash) {
        harness.setHand(player1, List.of(new ExavaRakdosBloodWitch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
