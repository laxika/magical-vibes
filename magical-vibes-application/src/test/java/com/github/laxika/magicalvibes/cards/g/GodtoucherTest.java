package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Godtoucher.class, AvatarOfMight.class, GrizzlyBears.class, Shock.class})
class GodtoucherTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage dealt to the targeted power-5+ creature this turn")
    void preventsDamage() {
        addCreatureReady(player1, new Godtoucher());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        // Shock the protected 8/8 — all damage should be prevented, so no damage is marked.
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        assertThat(avatar.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 5")
    void rejectsLowPowerTarget() {
        addCreatureReady(player1, new Godtoucher());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID smallCreature = harness.getPermanentId(player2, "Grizzly Bears");

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, smallCreature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void protectsOpponentsCreatureWithExactlyFivePowerAgainstRepeatedDamage() {
        Permanent godtoucher = addCreatureReady(player1, new Godtoucher());
        Permanent avatar = addCreatureReady(player2, new AvatarOfMight());
        avatar.setPowerModifier(-3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        assertThat(godtoucher.isTapped()).isTrue();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, avatar.getId());
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        assertThat(avatar.getMarkedDamage()).isZero();
    }

    @Test
    void targetBecomingTooSmallBeforeResolutionReceivesNoProtection() {
        addCreatureReady(player1, new Godtoucher());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, avatar.getId());
        avatar.setPowerModifier(-4);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        assertThat(avatar.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void protectionPersistsWhenPowerFallsBelowFiveAfterResolution() {
        addCreatureReady(player1, new Godtoucher());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();
        avatar.setPowerModifier(-4);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        assertThat(avatar.getMarkedDamage()).isZero();
    }

    @Test
    void protectionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new Godtoucher());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, avatar.getId());

        assertThat(avatar.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void preventsCombatDamageToTargetWithoutPreventingItsDamage() {
        addCreatureReady(player1, new Godtoucher());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);

        assertThat(avatar.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Avatar of Might");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new Godtoucher());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, avatar.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayActivationWithoutWhiteMana() {
        addCreatureReady(player1, new Godtoucher());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, avatar.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
