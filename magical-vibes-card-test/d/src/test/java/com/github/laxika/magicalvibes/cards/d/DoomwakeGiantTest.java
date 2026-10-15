package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomwakeGiant.class, GrizzlyBears.class, GloriousAnthem.class})
class DoomwakeGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry gives opposing creatures -1/-1 until end of turn")
    void ownEntryShrinksOpposingCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDoomwakeGiant();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another enchantment entering under your control triggers it")
    void anotherEnchantmentEntryShrinksOpposingCreatures() {
        harness.addToBattlefield(player1, new DoomwakeGiant());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(1);
    }

    @Test
    @DisplayName("The effect wears off at the end of the turn")
    void shrinkWearsOffAtEndOfTurn() {
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDoomwakeGiant();

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger it")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new DoomwakeGiant());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second Doomwake Giant triggers both itself and the existing giant")
    void secondGiantStacksShrinkAndKillsOpposingCreatures() {
        harness.addToBattlefield(player1, new DoomwakeGiant());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDoomwakeGiant();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves are not affected")
    void laterCreatureEntryIsNotAffected() {
        Permanent existingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDoomwakeGiant();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent laterBear = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existingBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, existingBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, laterBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBear)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves are affected")
    void creatureEntryBeforeResolutionIsAffected() {
        castDoomwakeGiant();
        harness.passBothPriorities();
        Permanent opposingBear = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-enchantment creature entering under your control does not trigger it")
    void nonEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new DoomwakeGiant());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
    }

    private void castDoomwakeGiant() {
        harness.castFromHand(player1, new DoomwakeGiant(), "{4}{B}");
    }
}
