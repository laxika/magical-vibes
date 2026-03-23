package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkSpy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JadeGuardianTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB — put a +1/+1 counter on target Merfolk you control")
    class EtbTests {

        @Test
        @DisplayName("Puts a +1/+1 counter on target Merfolk you control")
        void putsCounterOnTargetMerfolk() {
            harness.addToBattlefield(player1, new MerfolkSpy());
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            UUID merfolkId = harness.getPermanentId(player1, "Merfolk Spy");
            gs.playCard(gd, player1, 0, 0, merfolkId, null);

            // Resolve creature spell — ETB triggers
            harness.passBothPriorities();
            // Resolve ETB triggered ability
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();

            Permanent merfolk = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(merfolkId))
                    .findFirst().orElseThrow();
            assertThat(merfolk.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("Resolving creature spell puts ETB trigger on stack")
        void resolvingPutsEtbOnStack() {
            harness.addToBattlefield(player1, new MerfolkSpy());
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            UUID merfolkId = harness.getPermanentId(player1, "Merfolk Spy");
            gs.playCard(gd, player1, 0, 0, merfolkId, null);

            // Resolve creature spell — enters battlefield, ETB triggers
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Jade Guardian"));

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Jade Guardian");
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(merfolkId);
        }

        @Test
        @DisplayName("Cannot target a non-Merfolk creature you control")
        void cannotTargetNonMerfolk() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bearsId, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be a Merfolk creature you control");
        }

        @Test
        @DisplayName("Cannot target opponent's Merfolk")
        void cannotTargetOpponentMerfolk() {
            harness.addToBattlefield(player2, new MerfolkSpy());
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            UUID opponentMerfolkId = harness.getPermanentId(player2, "Merfolk Spy");

            assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, opponentMerfolkId, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be a Merfolk creature you control");
        }

        @Test
        @DisplayName("Can cast without a target when no Merfolk you control")
        void canCastWithoutTarget() {
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Jade Guardian");
        }

        @Test
        @DisplayName("ETB does not trigger when cast without a target")
        void etbDoesNotTriggerWithoutTarget() {
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            harness.castCreature(player1, 0);

            // Resolve creature spell
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Jade Guardian"));
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("ETB fizzles if target Merfolk is removed before resolution")
        void etbFizzlesIfTargetRemoved() {
            harness.addToBattlefield(player1, new MerfolkSpy());
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            UUID merfolkId = harness.getPermanentId(player1, "Merfolk Spy");
            gs.playCard(gd, player1, 0, 0, merfolkId, null);

            // Resolve creature spell — ETB on stack
            harness.passBothPriorities();

            // Remove target before ETB resolves
            gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(merfolkId));

            // Resolve ETB — fizzles
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        }
    }
}
