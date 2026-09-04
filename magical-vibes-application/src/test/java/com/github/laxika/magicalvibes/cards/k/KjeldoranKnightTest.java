package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KjeldoranKnight.class, BalduvianBears.class})
class KjeldoranKnightTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{W} ability gives +1/+0 until end of turn")
    void powerBoost() {
        addCreatureReady(player1, new KjeldoranKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent knight = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Power boost can be activated multiple times")
    void powerBoostStacksFromMultipleActivations() {
        addCreatureReady(player1, new KjeldoranKnight());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent knight = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(knight.getEffectivePower()).isEqualTo(3);
        assertThat(knight.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("{W}{W} ability gives +0/+2 until end of turn")
    void toughnessBoost() {
        addCreatureReady(player1, new KjeldoranKnight());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent knight = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(knight.getEffectivePower()).isEqualTo(1);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Both boosts stack and wear off at end of turn")
    void bothBoostsWearOff() {
        addCreatureReady(player1, new KjeldoranKnight());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent knight = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getEffectivePower()).isEqualTo(1);
        assertThat(knight.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate power boost without mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new KjeldoranKnight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void bandingLetsActivePlayerAssignBlockerDamage() {
        Permanent knight = addCreatureReady(player1, new KjeldoranKnight());
        Permanent bear = addCreatureReady(player1, new BalduvianBears());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(bear.getId(), 2));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knight);
        harness.assertInGraveyard(player1, bear.getCard().getName());
        harness.assertInGraveyard(player2, blocker.getCard().getName());
    }
}
