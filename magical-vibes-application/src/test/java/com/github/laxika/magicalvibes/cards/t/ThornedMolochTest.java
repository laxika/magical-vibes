package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThornedMolochTest extends BaseCardTest {

    private Permanent addMoloch() {
        Permanent moloch = addCreatureReady(player1, new ThornedMoloch());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return moloch;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn (prowess)")
    void noncreatureSpellPumps() {
        Permanent moloch = addMoloch();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities(); // resolve Shock
        harness.passBothPriorities(); // resolve prowess trigger

        assertThat(gqs.getEffectivePower(gd, moloch)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, moloch)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent moloch = addMoloch();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, moloch)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, moloch)).isEqualTo(2);
    }

    @Test
    @DisplayName("The prowess boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent moloch = addMoloch();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moloch)).isEqualTo(3);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, moloch)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, moloch)).isEqualTo(2);
    }

    @Test
    @DisplayName("Has first strike while attacking")
    void hasFirstStrikeWhileAttacking() {
        Permanent moloch = addMoloch();

        assertThat(gqs.hasKeyword(gd, moloch, Keyword.FIRST_STRIKE)).isFalse();

        moloch.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, moloch, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Loses first strike when no longer attacking")
    void losesFirstStrikeWhenNotAttacking() {
        Permanent moloch = addMoloch();
        moloch.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, moloch, Keyword.FIRST_STRIKE)).isTrue();

        moloch.setAttacking(false);

        assertThat(gqs.hasKeyword(gd, moloch, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not have first strike while not attacking")
    void noFirstStrikeWhileNotAttacking() {
        Permanent moloch = addMoloch();

        assertThat(gqs.hasKeyword(gd, moloch, Keyword.FIRST_STRIKE)).isFalse();
    }
}
