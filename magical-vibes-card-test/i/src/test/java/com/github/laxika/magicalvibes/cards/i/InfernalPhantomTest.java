package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.PatchedPlaything;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalPhantom.class, DazzlingTheaterPropRoom.class, Murder.class, PatchedPlaything.class})
class InfernalPhantomTest extends BaseCardTest {

    @Test
    void getsBoostWhenAnEnchantmentYouControlEnters() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        castSimpleEnchantment(player1);

        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, phantom)).isEqualTo(3);
    }

    @Test
    void getsBoostWhenYouFullyUnlockARoom() {
        Permanent room = castRoom();
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(4);
    }

    @Test
    void doesNotTriggerForAnOpponentsEnchantment() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        harness.forceActivePlayer(player2);
        castSimpleEnchantment(player2);

        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void deathTriggerDealsDamageEqualToItsEffectivePower() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        harness.setLife(player2, 20);
        castSimpleEnchantment(player1);

        destroyWithMurder(phantom);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        castSimpleEnchantment(player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, phantom)).isEqualTo(3);
    }

    @Test
    void boostsAccumulateForMultipleEnchantmentEntries() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        castSimpleEnchantment(player1);
        castSimpleEnchantment(player1);

        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, phantom)).isEqualTo(3);
    }

    @Test
    void triggersForAnEnchantmentEnteringWithoutBeingCast() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());

        harness.enterBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(4);
    }

    @Test
    void unlockingOnlyTheFirstDoorDoesNotTrigger() {
        Permanent room = harness.addToBattlefieldAndReturn(player1, new DazzlingTheaterPropRoom());
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.unlockRoomDoor(player1, 0, 0);
        resolveAllTriggers();

        assertThat(room.isRoomFullyUnlocked()).isFalse();
        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void roomEntryAndFullyUnlockingItEachGiveABoost() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        castSimpleEnchantment(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player1, 1, 1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, phantom)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerWhenAnOpponentFullyUnlocksARoom() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        harness.forceActivePlayer(player2);
        castSimpleEnchantment(player2);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player2, 0, 1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void deathTriggerCanDealDamageToACreature() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PatchedPlaything());

        destroyWithMurder(phantom);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void dyingBeforeTheEerieTriggerResolvesUsesUnboostedPower() {
        Permanent phantom = harness.addToBattlefieldAndReturn(player1, new InfernalPhantom());
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        destroyWithMurder(phantom);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(phantom);
        assertThat(gd.stack).isEmpty();
    }

    private void castSimpleEnchantment(com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(caster, ManaColor.WHITE, 4);
        harness.castModalSorcery(caster, 0, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private Permanent castRoom() {
        castSimpleEnchantment(player1);
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void destroyWithMurder(Permanent target) {
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
