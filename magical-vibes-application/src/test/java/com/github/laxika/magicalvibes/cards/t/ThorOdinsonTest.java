package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThorOdinson.class, GrizzlyBears.class, Shock.class})
class ThorOdinsonTest extends BaseCardTest {

    @Test
    @DisplayName("Both prowess abilities trigger for a noncreature spell")
    void noncreatureSpellTriggersBothProwessAbilities() {
        Permanent thor = addThor();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thor)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, thor)).isEqualTo(6);
    }

    @Test
    @DisplayName("Neither prowess ability triggers for a creature spell")
    void creatureSpellDoesNotTriggerProwess() {
        Permanent thor = addThor();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, thor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, thor)).isEqualTo(4);
    }

    @Test
    @DisplayName("The double prowess boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent thor = addThor();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thor)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, thor)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentNoncreatureSpellDoesNotTriggerProwess() {
        Permanent thor = addThor();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(gqs.getEffectivePower(gd, thor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, thor)).isEqualTo(4);
    }

    private Permanent addThor() {
        Permanent thor = harness.addToBattlefieldAndReturn(player1, new ThorOdinson());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return thor;
    }
}
