package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BottomlessPoolLockerRoom;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.r.RazorkinHordecaller;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InquisitiveGlimmer.class, GhostlyPrison.class, BottomlessPoolLockerRoom.class,
        RazorkinHordecaller.class})
class InquisitiveGlimmerTest extends BaseCardTest {

    @Test
    void reducesTheGenericCostOfEnchantmentSpells() {
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotApplyTheUnlockReductionToCastingARoom() {
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        harness.setHand(player1, List.of(new BottomlessPoolLockerRoom()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesTheGenericCostOfUnlockingARoomDoor() {
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new BottomlessPoolLockerRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);

        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void doesNotReduceColoredManaInAnUnlockCost() {
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new BottomlessPoolLockerRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.unlockRoomDoor(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();
    }

    @Test
    void doesNotReduceUnlockCostsForAnOpponentsRoom() {
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        Permanent room = harness.addToBattlefieldAndReturn(player2, new BottomlessPoolLockerRoom());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.unlockRoomDoor(
                player2, gd.playerBattlefields.get(player2.getId()).indexOf(room), 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();
    }

    @Test
    void reducesCastingTheLockerRoomHalfByExactlyOne() {
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        harness.setHand(player1, List.of(new BottomlessPoolLockerRoom()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bottomless Pool // Locker Room");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void multipleGlimmersReduceRoomCastingCumulatively() {
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        harness.setHand(player1, List.of(new BottomlessPoolLockerRoom()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bottomless Pool // Locker Room");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void multipleGlimmersReduceUnlockCostsCumulatively() {
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        Permanent room = harness.addToBattlefieldAndReturn(player1, new BottomlessPoolLockerRoom());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);

        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void doesNotReduceTheColoredOnlyCostOfAnEnchantmentCreature() {
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        harness.setHand(player1, List.of(new InquisitiveGlimmer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotReduceAnOpponentsEnchantmentSpell() {
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BottomlessPoolLockerRoom()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player2, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceANonEnchantmentCreatureSpell() {
        harness.addToBattlefield(player1, new InquisitiveGlimmer());
        harness.setHand(player1, List.of(new RazorkinHordecaller()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
