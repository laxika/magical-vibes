package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CuboidColonyTest extends BaseCardTest {

    private Permanent addColony(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CuboidColony());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    

    @Nested
    @DisplayName("Flash")
    class FlashTests {

        @Test
        @DisplayName("Can cast during opponent's turn")
        void canCastDuringOpponentsTurn() {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            harness.setHand(player1, List.of(new CuboidColony()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.getGameService().passPriority(harness.getGameData(), player2);
            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        }

        @Test
        @DisplayName("Can cast during combat step")
        void canCastDuringCombat() {
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();

            harness.setHand(player1, List.of(new CuboidColony()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);

            GameData gd = harness.getGameData();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        }
    }

    @Nested
    @DisplayName("Increment")
    class IncrementTests {

        @Test
        @DisplayName("Casting a two-mana spell puts a +1/+1 counter on the 1/1 (2 > 1)")
        void twoManaSpellAddsCounter() {
            Permanent colony = addColony(player1);
            setUpMainPhase(player1);

            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.castCreature(player1, 0);
            harness.passBothPriorities();

            assertThat(colony.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("Casting a one-mana spell adds no counter (1 is not greater than power or toughness)")
        void oneManaSpellAddsNoCounter() {
            Permanent colony = addColony(player1);
            setUpMainPhase(player1);

            harness.addMana(player1, ManaColor.RED, 1);
            harness.setHand(player1, List.of(new Shock()));
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(colony.getPlusOnePlusOneCounters()).isZero();
        }
    }
}
