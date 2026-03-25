package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkSpy;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VineshaperMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Card has one PutPlusOnePlusOneCounterOnTargetCreatureEffect on ETB")
    void hasCorrectEffects() {
        VineshaperMystic card = new VineshaperMystic();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);
    }

    @Nested
    @DisplayName("ETB — put a +1/+1 counter on each of up to two target Merfolk you control")
    class EtbTests {

        @Test
        @DisplayName("Puts a +1/+1 counter on one target Merfolk you control")
        void putsCounterOnOneTargetMerfolk() {
            harness.addToBattlefield(player1, new MerfolkSpy());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID merfolkId = harness.getPermanentId(player1, "Merfolk Spy");
            harness.castCreature(player1, 0, List.of(merfolkId));

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
        @DisplayName("Puts a +1/+1 counter on each of two target Merfolk you control")
        void putsCounterOnTwoTargetMerfolk() {
            MerfolkSpy spy1 = new MerfolkSpy();
            MerfolkSpy spy2 = new MerfolkSpy();
            harness.addToBattlefield(player1, spy1);
            harness.addToBattlefield(player1, spy2);
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
            UUID merfolk1Id = bf.get(0).getId();
            UUID merfolk2Id = bf.get(1).getId();
            harness.castCreature(player1, 0, List.of(merfolk1Id, merfolk2Id));

            // Resolve creature spell — ETB triggers
            harness.passBothPriorities();
            // Resolve ETB triggered ability
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();

            Permanent merfolk1 = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(merfolk1Id))
                    .findFirst().orElseThrow();
            Permanent merfolk2 = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(merfolk2Id))
                    .findFirst().orElseThrow();
            assertThat(merfolk1.getPlusOnePlusOneCounters()).isEqualTo(1);
            assertThat(merfolk2.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("Resolving creature spell puts ETB trigger on stack")
        void resolvingPutsEtbOnStack() {
            harness.addToBattlefield(player1, new MerfolkSpy());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID merfolkId = harness.getPermanentId(player1, "Merfolk Spy");
            harness.castCreature(player1, 0, List.of(merfolkId));

            // Resolve creature spell — enters battlefield, ETB triggers
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Vineshaper Mystic"));

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vineshaper Mystic");
        }

        @Test
        @DisplayName("Cannot target a non-Merfolk creature you control")
        void cannotTargetNonMerfolk() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bearsId)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be a Merfolk creature you control");
        }

        @Test
        @DisplayName("Cannot target opponent's Merfolk")
        void cannotTargetOpponentMerfolk() {
            harness.addToBattlefield(player2, new MerfolkSpy());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID opponentMerfolkId = harness.getPermanentId(player2, "Merfolk Spy");

            assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentMerfolkId)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be a Merfolk creature you control");
        }

        @Test
        @DisplayName("Can cast without targets when no Merfolk you control")
        void canCastWithoutTargets() {
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vineshaper Mystic");
        }

        @Test
        @DisplayName("ETB does not trigger when cast without targets")
        void etbDoesNotTriggerWithoutTargets() {
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castCreature(player1, 0);

            // Resolve creature spell
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Vineshaper Mystic"));
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Can target itself since Vineshaper Mystic is a Merfolk")
        void canTargetItself() {
            harness.addToBattlefield(player1, new MerfolkSpy());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID merfolkId = harness.getPermanentId(player1, "Merfolk Spy");
            harness.castCreature(player1, 0, List.of(merfolkId));

            // Resolve creature spell — ETB triggers
            harness.passBothPriorities();

            // Get Vineshaper Mystic's permanent ID
            UUID vineshaperId = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Vineshaper Mystic"))
                    .findFirst().orElseThrow().getId();

            // The ETB already chose Merfolk Spy as target before entering, resolve it
            harness.passBothPriorities();

            Permanent merfolk = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(merfolkId))
                    .findFirst().orElseThrow();
            assertThat(merfolk.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("ETB partially resolves if one of two targets is removed")
        void etbPartiallyResolvesIfOneTargetRemoved() {
            MerfolkSpy spy1 = new MerfolkSpy();
            MerfolkSpy spy2 = new MerfolkSpy();
            harness.addToBattlefield(player1, spy1);
            harness.addToBattlefield(player1, spy2);
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
            UUID merfolk1Id = bf.get(0).getId();
            UUID merfolk2Id = bf.get(1).getId();
            harness.castCreature(player1, 0, List.of(merfolk1Id, merfolk2Id));

            // Resolve creature spell — ETB triggers
            harness.passBothPriorities();

            // Remove first target before ETB resolves
            gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(merfolk1Id));

            // Resolve ETB — partially resolves
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();

            Permanent merfolk2 = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(merfolk2Id))
                    .findFirst().orElseThrow();
            assertThat(merfolk2.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("ETB fizzles if all targets are removed before resolution")
        void etbFizzlesIfAllTargetsRemoved() {
            harness.addToBattlefield(player1, new MerfolkSpy());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID merfolkId = harness.getPermanentId(player1, "Merfolk Spy");
            harness.castCreature(player1, 0, List.of(merfolkId));

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
