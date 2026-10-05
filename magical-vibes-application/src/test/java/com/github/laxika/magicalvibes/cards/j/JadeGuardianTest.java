package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.m.MerrowCommerce;
import com.github.laxika.magicalvibes.cards.r.RiverHeraldsBoon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.g.GrazingWhiptail;
import com.github.laxika.magicalvibes.cards.h.HeadwaterSentries;
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

@CardUsed({JadeGuardian.class, HeadwaterSentries.class, GrazingWhiptail.class, RiverHeraldsBoon.class, MerrowCommerce.class})
class JadeGuardianTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB — put a +1/+1 counter on target Merfolk you control")
    @CardUsed({JadeGuardian.class, HeadwaterSentries.class, GrazingWhiptail.class, RiverHeraldsBoon.class})
    class EtbTests {

        @Test
        @DisplayName("Puts a +1/+1 counter on target Merfolk you control")
        void putsCounterOnTargetMerfolk() {
            Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new HeadwaterSentries());
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            UUID merfolkId = merfolk.getId();
            harness.castCreature(player1, 0, merfolkId);

            // Resolve creature spell — ETB triggers
            harness.passBothPriorities();
            // Resolve ETB triggered ability
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();

            assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        @DisplayName("Resolving creature spell puts ETB trigger on stack")
        void resolvingPutsEtbOnStack() {
            harness.addToBattlefield(player1, new HeadwaterSentries());
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            UUID merfolkId = harness.getPermanentId(player1, "Headwater Sentries");
            harness.castCreature(player1, 0, merfolkId);

            // Resolve creature spell — enters battlefield, ETB triggers
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Jade Guardian");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Jade Guardian");
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(merfolkId);
        }

        @Test
        @DisplayName("Cannot target a non-Merfolk creature you control")
        void cannotTargetNonMerfolk() {
            harness.addToBattlefield(player1, new GrazingWhiptail());
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            UUID bearsId = harness.getPermanentId(player1, "Grazing Whiptail");

            assertThatThrownBy(() -> harness.castCreature(player1, 0, bearsId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be a Merfolk creature you control");
        }

        @Test
        @DisplayName("Cannot target opponent's Merfolk")
        void cannotTargetOpponentMerfolk() {
            harness.addToBattlefield(player2, new HeadwaterSentries());
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            UUID opponentMerfolkId = harness.getPermanentId(player2, "Headwater Sentries");

            assertThatThrownBy(() -> harness.castCreature(player1, 0, opponentMerfolkId))
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
        @DisplayName("ETB can target Jade Guardian itself when it enters alone")
        void etbTargetsSelfWhenEnteringAlone() {
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            harness.castCreature(player1, 0);

            // Resolve creature spell
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Jade Guardian");
            UUID guardianId = harness.getPermanentId(player1, "Jade Guardian");
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                    .containsExactly(guardianId);
            harness.handlePermanentChosen(player1, guardianId);
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                    .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        @DisplayName("ETB fizzles if target Merfolk is removed before resolution")
        void etbFizzlesIfTargetRemoved() {
            harness.addToBattlefield(player1, new HeadwaterSentries());
            harness.setHand(player1, List.of(new JadeGuardian()));
            harness.addMana(player1, ManaColor.GREEN, 4);

            UUID merfolkId = harness.getPermanentId(player1, "Headwater Sentries");
            harness.castCreature(player1, 0, merfolkId);

            // Resolve creature spell — ETB on stack
            harness.passBothPriorities();

            // Remove target before ETB resolves
            gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(merfolkId));

            // Resolve ETB — fizzles
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        }
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting Jade Guardian")
    void opponentCannotTargetGuardian() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new JadeGuardian());
        harness.setHand(player2, List.of(new RiverHeraldsBoon()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of(guardian.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Hexproof permits its controller to target Jade Guardian")
    void controllerCanTargetGuardian() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new JadeGuardian());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, List.of(guardian.getId()));

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed({JadeGuardian.class, MerrowCommerce.class})
    @DisplayName("ETB can put a counter on a noncreature Merfolk permanent")
    void canTargetNoncreatureMerfolk() {
        Permanent commerce = harness.addToBattlefieldAndReturn(player1, new MerrowCommerce());
        harness.setHand(player1, List.of(new JadeGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(commerce.getId());
        harness.handlePermanentChosen(player1, commerce.getId());
        harness.passBothPriorities();

        assertThat(commerce.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

}
