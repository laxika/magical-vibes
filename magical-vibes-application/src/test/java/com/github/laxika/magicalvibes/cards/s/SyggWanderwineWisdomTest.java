package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniOutlandChaperone;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronShieldElf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyggWanderwineWisdom.class, IronShieldElf.class, Forest.class, AjaniOutlandChaperone.class})
class SyggWanderwineWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants a combat-damage draw trigger to the target creature")
    void etbGrantsCombatDamageDraw() {
        Permanent target = addCreatureReady(player1, new IronShieldElf());
        harness.setHand(player1, List.of(new SyggWanderwineWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        target.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Transforming into Wanderbrine Shield grants protection from every color")
    void backFaceGrantsProtectionUntilNextTurn() {
        Permanent sygg = addCreatureReady(player1, new SyggWanderwineWisdom());
        Permanent target = addCreatureReady(player1, new IronShieldElf());

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(sygg.isTransformed()).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isTrue();

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.WHITE)).isFalse();
    }

    @Test
    void frontFaceCannotBeBlocked() {
        Permanent sygg = addCreatureReady(player1, new SyggWanderwineWisdom());
        addCreatureReady(player2, new IronShieldElf());
        sygg.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void backFaceCannotBeBlockedWithoutProtection() {
        Permanent sygg = addCreatureReady(player1, new SyggWanderwineWisdom());
        Permanent target = addCreatureReady(player1, new IronShieldElf());
        addCreatureReady(player2, new IronShieldElf());
        transformToBack(target);
        sygg.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void grantedAbilityDrawsForCombatDamageToPlaneswalker() {
        Permanent attacker = addCreatureReady(player1, new IronShieldElf());
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniOutlandChaperone());
        ajani.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new SyggWanderwineWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castCreature(player1, 0, attacker.getId());
        resolveAllTriggers();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        attacker.setAttacking(true);
        attacker.setAttackTarget(ajani.getId());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ajani);
    }

    @Test
    void opponentsCreatureCanGainTheDrawAbilityAndItsControllerDraws() {
        Permanent target = addCreatureReady(player2, new IronShieldElf());
        harness.setHand(player1, List.of(new SyggWanderwineWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        int ownHandBefore = gd.playerHands.get(player1.getId()).size();
        int opposingHandBefore = gd.playerHands.get(player2.getId()).size();
        target.setAttacking(true);
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opposingHandBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownHandBefore);
    }

    @Test
    void grantedDrawAbilityExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new IronShieldElf());
        harness.setHand(player1, List.of(new SyggWanderwineWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player2.getId()).size();
        target.setAttacking(true);
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
    }

    @Test
    void opponentsFirstMainPhaseDoesNotTransformSygg() {
        Permanent sygg = addCreatureReady(player1, new SyggWanderwineWisdom());
        harness.addMana(player1, ManaColor.WHITE, 1);
        advanceToPrecombatMain(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(sygg.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingBlueTransformsBackAndGrantsDrawAbility() {
        Permanent sygg = addCreatureReady(player1, new SyggWanderwineWisdom());
        Permanent target = addCreatureReady(player1, new IronShieldElf());
        transformToBack(target);
        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(sygg.isTransformed()).isFalse();
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        target.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void decliningWhitePaymentLeavesFrontFaceUnchanged() {
        Permanent sygg = addCreatureReady(player1, new SyggWanderwineWisdom());
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sygg.isTransformed()).isFalse();
    }

    @Test
    void decliningBluePaymentLeavesBackFaceUnchanged() {
        Permanent sygg = addCreatureReady(player1, new SyggWanderwineWisdom());
        Permanent target = addCreatureReady(player1, new IronShieldElf());
        transformToBack(target);
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sygg.isTransformed()).isTrue();
    }

    @Test
    void protectionDoesNotExpireAtOpponentsTurnStart() {
        addCreatureReady(player1, new SyggWanderwineWisdom());
        Permanent target = addCreatureReady(player1, new IronShieldElf());
        transformToBack(target);
        gd.expireFloatingEffectsAtTurnStart(player2.getId());

        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, target, color)).isTrue();
        }
    }

    @Test
    void backFaceProtectionCannotTargetOpponentsCreature() {
        addCreatureReady(player1, new SyggWanderwineWisdom());
        Permanent ownTarget = addCreatureReady(player1, new IronShieldElf());
        Permanent opposingTarget = addCreatureReady(player2, new IronShieldElf());
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownTarget.getId());
        resolveAllTriggers();
        assertThat(gqs.hasProtectionFrom(gd, opposingTarget, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownTarget, CardColor.BLACK)).isTrue();
    }

    private void transformToBack(Permanent target) {
        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
