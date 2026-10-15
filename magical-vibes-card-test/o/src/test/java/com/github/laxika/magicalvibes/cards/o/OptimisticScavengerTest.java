package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OptimisticScavenger.class, GloriousAnthem.class, GrizzlyBears.class, Forest.class,
        DazzlingTheaterPropRoom.class})
class OptimisticScavengerTest extends BaseCardTest {

    @Test
    void enchantmentEnteringPutsCounterOnTargetCreature() {
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new OptimisticScavenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(scavenger.getId(), target.getId()).doesNotContain(land.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void fullyUnlockingRoomPutsCounterOnTargetCreature() {
        Permanent room = castRoom();
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new OptimisticScavenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(scavenger.getId(), target.getId()).doesNotContain(land.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void roomEntryAndFullyUnlockingEachPutOneCounterOnScavengerItself() {
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new OptimisticScavenger());
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, scavenger.getId());
        harness.passBothPriorities();

        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent room = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(room.isRoomFullyUnlocked()).isFalse();

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player1, 1, 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, scavenger.getId());
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enchantmentsEnteringWithoutBeingCastTriggerEachTime() {
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new OptimisticScavenger());

        for (int i = 1; i <= 2; i++) {
            harness.enterBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, scavenger.getId());
            harness.passBothPriorities();

            assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(i);
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    void opponentsEnchantmentEntryAndFullyUnlockingDoNotTrigger() {
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new OptimisticScavenger());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.castModalSorcery(player2, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player2, 0, 1);

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isRoomFullyUnlocked()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castRoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
