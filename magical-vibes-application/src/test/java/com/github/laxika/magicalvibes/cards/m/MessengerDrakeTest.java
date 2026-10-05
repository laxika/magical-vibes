package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MessengerDrake.class, GrizzlyBears.class, WrathOfGod.class})
class MessengerDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Messenger Drake dies in combat, controller draws a card")
    void diesInCombatDrawsCard() {
        Permanent drakePerm = harness.addToBattlefieldAndReturn(player1, new MessengerDrake());
        drakePerm.setSummoningSick(false);
        drakePerm.setBlocking(true);
        drakePerm.addBlockingTarget(0);

        GrizzlyBears big = new GrizzlyBears();
        big.setPower(5);
        big.setToughness(5);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, big);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Messenger Drake");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Messenger Drake"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Messenger Drake dies from Wrath of God, controller draws a card")
    void diesFromWrathDrawsCard() {
        harness.addToBattlefield(player1, new MessengerDrake());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Messenger Drake");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Messenger Drake survives combat, no death trigger fires")
    void survivesNoTrigger() {
        Permanent drakePerm = harness.addToBattlefieldAndReturn(player1, new MessengerDrake());
        drakePerm.setSummoningSick(false);
        drakePerm.setBlocking(true);
        drakePerm.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Messenger Drake");
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Messenger Drake"));
    }

    @Test
    @DisplayName("Simultaneous deaths draw one card for each Drake's controller after resolution")
    void simultaneousDeathsDrawForRespectiveControllers() {
        harness.addToBattlefield(player1, new MessengerDrake());
        harness.addToBattlefield(player2, new MessengerDrake());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Messenger Drake");
        harness.assertInGraveyard(player2, "Messenger Drake");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
