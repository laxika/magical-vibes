package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OrderOfTheGoldenCricket.class)
class OrderOfTheGoldenCricketTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking and paying {W} grants flying until end of turn")
    void payingGrantsFlying() {
        Permanent cricket = addReadyCricket(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(cricket.hasKeyword(Keyword.FLYING)).isFalse();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the may-pay does not grant flying")
    void decliningDoesNotGrantFlying() {
        Permanent cricket = addReadyCricket(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Accepting without mana does not grant flying")
    void cannotPayDoesNotGrantFlying() {
        Permanent cricket = addReadyCricket(player1);
        // No mana added

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Paying consumes white mana and leaves other mana untouched")
    void payingConsumesWhiteMana() {
        Permanent cricket = addReadyCricket(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonwhite mana cannot pay the white may-pay cost")
    void nonWhiteManaCannotPay() {
        Permanent cricket = addReadyCricket(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent cricket = addReadyCricket(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(cricket.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Paying grants flying only to the attacking Cricket")
    void onlyAttackingCricketGainsFlying() {
        Permanent attacker = addReadyCricket(player1);
        Permanent nonAttacker = addReadyCricket(player1);
        Permanent defender = addReadyCricket(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(attacker.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(nonAttacker.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(defender.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The attacking controller pays even when player two attacks")
    void playerTwoPaysForOwnAttackTrigger() {
        Permanent cricket = addReadyCricket(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player2, true));

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining payment preserves white mana")
    void decliningPreservesMana() {
        Permanent cricket = addReadyCricket(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, false));

        assertThat(cricket.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    private Permanent addReadyCricket(Player player) {
        return addCreatureReady(player, new OrderOfTheGoldenCricket());
    }
}
