package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JarethLeonineTitan.class, GrizzlyBears.class})
class JarethLeonineTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives Jareth +7/+7 until end of turn")
    void blockingBoostsJareth() {
        Permanent jareth = harness.addToBattlefieldAndReturn(player1, new JarethLeonineTitan());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        jareth.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, java.util.List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(jareth.getEffectivePower()).isEqualTo(11);
        assertThat(jareth.getEffectiveToughness()).isEqualTo(14);
    }

    @Test
    @DisplayName("Jareth gains the chosen protection until end of turn")
    void gainsProtectionOfChosenColor() {
        Permanent jareth = harness.addToBattlefieldAndReturn(player1, new JarethLeonineTitan());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(jareth.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Jareth's chosen protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent jareth = harness.addToBattlefieldAndReturn(player1, new JarethLeonineTitan());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(jareth.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }
}
