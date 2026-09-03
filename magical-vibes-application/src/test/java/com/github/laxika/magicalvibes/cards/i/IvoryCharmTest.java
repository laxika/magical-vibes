package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.v.VolcanicDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IvoryCharm.class, IronTuskElephant.class, VolcanicDragon.class, Incinerate.class, Island.class})
class IvoryCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: All creatures get -2/-0 until end of turn")
    class WeakenAllMode {

        @Test
        @DisplayName("Weakens creatures on both battlefields")
        void weakensEveryCreature() {
            harness.addToBattlefield(player1, new IronTuskElephant());
            harness.addToBattlefield(player2, new IronTuskElephant());
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            Permanent mine = findPermanent(player1, "Iron Tusk Elephant");
            Permanent theirs = findPermanent(player2, "Iron Tusk Elephant");
            assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(3);
            assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(1);
        }

        @Test
        @DisplayName("Wears off at the cleanup step")
        void wearsOff() {
            harness.addToBattlefield(player1, new IronTuskElephant());
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Iron Tusk Elephant"))).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Mode 1: Tap target creature")
    class TapMode {

        @Test
        @DisplayName("Taps the targeted creature")
        void tapsTarget() {
            harness.addToBattlefield(player2, new IronTuskElephant());
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            UUID targetId = harness.getPermanentId(player2, "Iron Tusk Elephant");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(findPermanent(player2, "Iron Tusk Elephant").isTapped()).isTrue();
        }

        @Test
        @DisplayName("Cannot target a player with the tap mode")
        void cannotTargetPlayer() {
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, player2.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent with the tap mode")
        void cannotTargetNoncreaturePermanent() {
            harness.addToBattlefield(player2, new Island());
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            UUID targetId = harness.getPermanentId(player2, "Island");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Prevent the next 1 damage to any target")
    class PreventDamageMode {

        @Test
        @DisplayName("Adds a 1-damage prevention shield to a target creature")
        void shieldOnCreature() {
            harness.addToBattlefield(player1, new IronTuskElephant());
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            UUID targetId = harness.getPermanentId(player1, "Iron Tusk Elephant");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            assertThat(findPermanent(player1, "Iron Tusk Elephant").getDamagePreventionShield()).isEqualTo(1);
        }

        @Test
        @DisplayName("Prevents only the next damage dealt to a targeted creature")
        void preventsOnlyNextDamageToCreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new VolcanicDragon());
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 2, target.getId());
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new Incinerate()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.castAndResolveInstant(player1, 0, target.getId());

            assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
            assertThat(target.getMarkedDamage()).isEqualTo(2);

            harness.setHand(player1, List.of(new Incinerate()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.castAndResolveInstant(player1, 0, target.getId());

            harness.assertNotOnBattlefield(player2, "Volcanic Dragon");
        }

        @Test
        @DisplayName("Adds a 1-damage prevention shield to a target player")
        void shieldOnPlayer() {
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 2, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent with the prevention mode")
        void cannotTargetNoncreaturePermanent() {
            harness.addToBattlefield(player2, new Island());
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            UUID targetId = harness.getPermanentId(player2, "Island");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Prevents the next damage dealt to a targeted player")
        void preventsNextDamageToPlayer() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 2, player2.getId());
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new Incinerate()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.castAndResolveInstant(player1, 0, player2.getId());

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
        }
    }
}
