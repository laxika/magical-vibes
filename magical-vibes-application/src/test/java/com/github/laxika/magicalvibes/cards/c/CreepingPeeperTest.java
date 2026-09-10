package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AquamorphEntity;
import com.github.laxika.magicalvibes.cards.b.BottomlessPoolLockerRoom;
import com.github.laxika.magicalvibes.cards.m.MysticRemora;
import com.github.laxika.magicalvibes.cards.p.PhantasmalBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CreepingPeeper.class, MysticRemora.class, BottomlessPoolLockerRoom.class,
        AquamorphEntity.class, PhantasmalBear.class})
class CreepingPeeperTest extends BaseCardTest {

    @Test
    void usesManaToCastAnEnchantmentSpell() {
        addReadyPeeper();
        harness.setHand(player1, List.of(new MysticRemora()));
        activatePeeperMana();

        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isEqualTo(1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mystic Remora");
        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isZero();
    }

    @Test
    void usesManaToUnlockARoomDoor() {
        addReadyPeeper();
        Permanent room = harness.addToBattlefieldAndReturn(player1, new BottomlessPoolLockerRoom());
        activatePeeperMana();

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 0);

        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isZero();
    }

    @Test
    void usesManaToTurnAPermanentFaceUp() {
        addReadyPeeper();
        harness.setHand(player1, List.of(new AquamorphEntity()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent entity = findPermanent(player1, "Aquamorph Entity");
        activatePeeperMana();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(entity));

        harness.handleListChoice(player1, "5/1");

        assertThat(entity.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isZero();
    }

    @Test
    void cannotUseManaToCastACreatureSpell() {
        addReadyPeeper();
        activatePeeperMana();
        harness.setHand(player1, List.of(new PhantasmalBear()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isEqualTo(1);
    }

    private Permanent addReadyPeeper() {
        Permanent peeper = harness.addToBattlefieldAndReturn(player1, new CreepingPeeper());
        peeper.setSummoningSick(false);
        return peeper;
    }

    private void activatePeeperMana() {
        harness.activateAbility(player1, 0, null, null);
    }
}
