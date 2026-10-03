package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DashingBloodsucker.class, DazzlingTheaterPropRoom.class})
class DashingBloodsuckerTest extends BaseCardTest {

    @Test
    void getsPowerBoostAndLifelinkWhenAnEnchantmentYouControlEnters() {
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new DashingBloodsucker());
        castRoom();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void getsPowerBoostAndLifelinkWhenYouFullyUnlockARoom() {
        Permanent room = castRoom();
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new DashingBloodsucker());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void doesNotTriggerForAnOpponentsEnchantment() {
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new DashingBloodsucker());
        harness.forceActivePlayer(player2);
        castRoom(player2);


        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostAndLifelinkWearOffAtEndOfTurn() {
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new DashingBloodsucker());
        castRoom();

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void roomEntryAndFullyUnlockingItGiveSeparateCumulativeBoosts() {
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new DashingBloodsucker());
        Permanent room = castRoom();

        assertThat(room.isRoomFullyUnlocked()).isFalse();
        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player1, 1, 1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void doesNotTriggerWhenAnOpponentFullyUnlocksARoom() {
        harness.forceActivePlayer(player2);
        Permanent room = castRoom(player2);
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new DashingBloodsucker());
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player2, 0, 1);
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachEnchantmentEnteringTriggersIndependently() {
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new DashingBloodsucker());
        castRoom();
        harness.passBothPriorities();
        castRoom();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isTrue();
    }

    private Permanent castRoom() {
        return castRoom(player1);
    }

    private Permanent castRoom(Player player) {
        harness.setHand(player, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player, ManaColor.WHITE, 4);
        harness.castModalSorcery(player, 0, 0, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player.getId()).getLast();
    }
}
