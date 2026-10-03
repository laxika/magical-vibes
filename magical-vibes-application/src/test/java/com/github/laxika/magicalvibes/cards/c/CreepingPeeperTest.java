package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AquamorphEntity;
import com.github.laxika.magicalvibes.cards.b.BottomlessPoolLockerRoom;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CreepingPeeper.class, BottomlessPoolLockerRoom.class, AquamorphEntity.class})
class CreepingPeeperTest extends BaseCardTest {

    @Test
    void usesManaToCastAnEnchantmentSpell() {
        addReadyPeeper();
        harness.setHand(player1, List.of(new BottomlessPoolLockerRoom()));
        activatePeeperMana();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isEqualTo(1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bottomless Pool // Locker Room");
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
        harness.setHand(player1, List.of(new CreepingPeeper()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void manaAbilityResolvesImmediatelyAndCannotBeActivatedAgainWhileTapped() {
        Permanent peeper = addReadyPeeper();

        activatePeeperMana();

        assertThat(peeper.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isEqualTo(1);
        assertThatThrownBy(this::activatePeeperMana)
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void summoningSickPeeperCannotActivateItsTapAbility() {
        Permanent peeper = harness.addToBattlefieldAndReturn(player1, new CreepingPeeper());
        peeper.setSummoningSick(true);

        assertThatThrownBy(this::activatePeeperMana)
                .isInstanceOf(IllegalStateException.class);
        assertThat(peeper.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isZero();
    }

    @Test
    void failedRoomUnlockKeepsTheManaRestricted() {
        addReadyPeeper();
        Permanent room = harness.addToBattlefieldAndReturn(player1, new BottomlessPoolLockerRoom());
        activatePeeperMana();

        assertThatThrownBy(() -> harness.unlockRoomDoor(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(room), 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(room.isRoomDoorUnlocked(1)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isEqualTo(1);
        harness.setHand(player1, List.of(new CreepingPeeper()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotSpendManaToCastAMorphCreatureFaceDown() {
        addReadyPeeper();
        activatePeeperMana();
        harness.setHand(player1, List.of(new AquamorphEntity()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getEnchantmentOrRoomUnlockOrTurnFaceUpMana(ManaColor.BLUE)).isEqualTo(1);
    }

    private Permanent addReadyPeeper() {
        return addCreatureReady(player1, new CreepingPeeper());
    }

    private void activatePeeperMana() {
        harness.activateAbility(player1, 0, null, null);
    }
}
