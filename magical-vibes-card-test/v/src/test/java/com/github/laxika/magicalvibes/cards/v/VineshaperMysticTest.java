package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.m.MerrowCommerce;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.d.DeeprootWarrior;
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
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({VineshaperMystic.class, DeeprootWarrior.class, RaptorCompanion.class, MerrowCommerce.class})
class VineshaperMysticTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB — put a +1/+1 counter on each of up to two target Merfolk you control")
    @CardUsed({VineshaperMystic.class, DeeprootWarrior.class, RaptorCompanion.class, MerrowCommerce.class})
    class EtbTests {

        @Test
        @DisplayName("Puts a +1/+1 counter on one target Merfolk you control")
        void putsCounterOnOneTargetMerfolk() {
            Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new DeeprootWarrior());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID merfolkId = merfolk.getId();
            harness.castCreature(player1, 0, List.of(merfolkId));

            // Resolve creature spell — ETB triggers
            harness.passBothPriorities();
            // Resolve ETB triggered ability
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();

            assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        @DisplayName("Puts a +1/+1 counter on each of two target Merfolk you control")
        void putsCounterOnTwoTargetMerfolk() {
            Permanent merfolk1 = harness.addToBattlefieldAndReturn(player1, new DeeprootWarrior());
            Permanent merfolk2 = harness.addToBattlefieldAndReturn(player1, new DeeprootWarrior());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID merfolk1Id = merfolk1.getId();
            UUID merfolk2Id = merfolk2.getId();
            harness.castCreature(player1, 0, List.of(merfolk1Id, merfolk2Id));

            // Resolve creature spell — ETB triggers
            harness.passBothPriorities();
            // Resolve ETB triggered ability
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();

            assertThat(merfolk1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(merfolk2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        @DisplayName("Resolving creature spell puts ETB trigger on stack")
        void resolvingPutsEtbOnStack() {
            Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new DeeprootWarrior());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID merfolkId = merfolk.getId();
            harness.castCreature(player1, 0, List.of(merfolkId));

            // Resolve creature spell — enters battlefield, ETB triggers
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Vineshaper Mystic");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vineshaper Mystic");
        }

        @Test
        @DisplayName("Cannot target a non-Merfolk creature you control")
        void cannotTargetNonMerfolk() {
            harness.addToBattlefield(player1, new RaptorCompanion());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID bearsId = harness.getPermanentId(player1, "Raptor Companion");

            assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bearsId)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be a Merfolk creature you control");
        }

        @Test
        @DisplayName("Cannot target opponent's Merfolk")
        void cannotTargetOpponentMerfolk() {
            harness.addToBattlefield(player2, new DeeprootWarrior());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID opponentMerfolkId = harness.getPermanentId(player2, "Deeproot Warrior");

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
        @DisplayName("ETB can choose zero targets even though the Mystic is a legal target")
        void etbCanChooseZeroTargets() {
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castCreature(player1, 0);

            // Resolve creature spell
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Vineshaper Mystic");
            harness.handlePermanentChosen(player1, player1.getId());
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            assertThat(gd.stack).isEmpty();
            assertThat(findPermanent(player1, "Vineshaper Mystic")
                    .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }

        @Test
        @DisplayName("Can target itself since Vineshaper Mystic is a Merfolk")
        void canTargetItself() {
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);
            harness.castCreature(player1, 0);
            harness.passBothPriorities();

            Permanent mystic = findPermanent(player1, "Vineshaper Mystic");
            harness.handlePermanentChosen(player1, mystic.getId());
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(mystic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        @DisplayName("ETB partially resolves if one of two targets is removed")
        void etbPartiallyResolvesIfOneTargetRemoved() {
            Permanent merfolk1 = harness.addToBattlefieldAndReturn(player1, new DeeprootWarrior());
            Permanent merfolk2 = harness.addToBattlefieldAndReturn(player1, new DeeprootWarrior());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID merfolk1Id = merfolk1.getId();
            UUID merfolk2Id = merfolk2.getId();
            harness.castCreature(player1, 0, List.of(merfolk1Id, merfolk2Id));

            // Resolve creature spell — ETB triggers
            harness.passBothPriorities();

            // Remove first target before ETB resolves
            gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(merfolk1Id));

            // Resolve ETB — partially resolves
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();

            assertThat(merfolk2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        @DisplayName("ETB fizzles if all targets are removed before resolution")
        void etbFizzlesIfAllTargetsRemoved() {
            Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new DeeprootWarrior());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            UUID merfolkId = merfolk.getId();
            harness.castCreature(player1, 0, List.of(merfolkId));

            // Resolve creature spell — ETB on stack
            harness.passBothPriorities();

            // Remove target before ETB resolves
            gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(merfolkId));

            // Resolve ETB — fizzles
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        }

        @Test
        @CardUsed({VineshaperMystic.class, MerrowCommerce.class})
        @DisplayName("Can put a counter on a noncreature Merfolk permanent")
        void canTargetNoncreatureMerfolk() {
            Permanent commerce = harness.addToBattlefieldAndReturn(player1, new MerrowCommerce());
            harness.setHand(player1, List.of(new VineshaperMystic()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castCreature(player1, 0, List.of(commerce.getId()));
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(commerce.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

    }
}
