package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DracoTest extends BaseCardTest {

    private boolean controlsDraco(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Draco"));
    }

    @Nested
    @DisplayName("Domain cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Costs full {16} with no basic land types")
        void fullCostWithoutLands() {
            harness.setHand(player1, List.of(new Draco()));
            harness.addMana(player1, ManaColor.COLORLESS, 16);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Draco");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("Cannot cast with one mana short of full cost")
        void cannotCastOneShort() {
            harness.setHand(player1, List.of(new Draco()));
            harness.addMana(player1, ManaColor.COLORLESS, 15);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Reduced by {2} for each basic land type among your lands")
        void reducedByTwoPerBasicLandType() {
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Island());
            harness.addToBattlefield(player1, new Mountain());
            harness.setHand(player1, List.of(new Draco()));
            // 3 basic land types => reduced by {6} => {10}
            harness.addMana(player1, ManaColor.COLORLESS, 10);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("Duplicate basic land types count only once")
        void duplicateTypesCountOnce() {
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Forest());
            harness.setHand(player1, List.of(new Draco()));
            // Only one distinct type (Forest) => reduced by {2} => {14}, not {12}
            harness.addMana(player1, ManaColor.COLORLESS, 13);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Domain upkeep tax")
    class UpkeepTax {

        private void advanceToUpkeep(Player activePlayer) {
            harness.forceActivePlayer(activePlayer);
            harness.forceStep(TurnStep.UNTAP);
            harness.clearPriorityPassed();
            harness.passBothPriorities(); // advances to UPKEEP, trigger fires
        }

        @Test
        @DisplayName("Declining the payment sacrifices Draco")
        void declineSacrifices() {
            harness.addToBattlefield(player1, new Draco());

            advanceToUpkeep(player1);
            harness.passBothPriorities(); // resolve trigger → may-pay prompt

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);

            assertThat(controlsDraco(player1)).isFalse();
        }

        @Test
        @DisplayName("Paying the reduced cost keeps Draco")
        void payKeepsDraco() {
            harness.addToBattlefield(player1, new Draco());
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player1, new Island());

            advanceToUpkeep(player1);
            harness.passBothPriorities();
            // 2 basic land types => {10} reduced by {4} => pay {6}
            harness.addMana(player1, ManaColor.COLORLESS, 6);
            harness.handleMayAbilityChosen(player1, true);

            assertThat(controlsDraco(player1)).isTrue();
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("With all five basic land types the cost is reduced to {0}")
        void fiveTypesReduceToZero() {
            harness.addToBattlefield(player1, new Draco());
            harness.addToBattlefield(player1, new Plains());
            harness.addToBattlefield(player1, new Island());
            harness.addToBattlefield(player1, new Swamp());
            harness.addToBattlefield(player1, new Mountain());
            harness.addToBattlefield(player1, new Forest());

            advanceToUpkeep(player1);
            harness.passBothPriorities();
            // {10} reduced by {10} => {0}; accepting pays nothing and keeps Draco
            harness.handleMayAbilityChosen(player1, true);

            assertThat(controlsDraco(player1)).isTrue();
        }

        @Test
        @DisplayName("Does not trigger during the opponent's upkeep")
        void noTriggerOnOpponentUpkeep() {
            harness.addToBattlefield(player1, new Draco());

            advanceToUpkeep(player2);
            harness.passBothPriorities();

            assertThat(controlsDraco(player1)).isTrue();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }
}
