package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ControlWinCondition.class, Counterspell.class, Unsummon.class})
class ControlWinConditionTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness track the controller's turns taken")
    void powerAndToughnessTrackControllerTurns() {
        gd.turnsTakenByPlayer.put(player1.getId(), 3);
        Permanent condition = addCreatureReady(player1, new ControlWinCondition());

        assertThat(gqs.getEffectivePower(gd, condition)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, condition)).isEqualTo(3);

        gd.turnsTakenByPlayer.put(player1.getId(), 5);

        assertThat(gqs.getEffectivePower(gd, condition)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, condition)).isEqualTo(5);
    }

    @Test
    @DisplayName("Only the controller's turns count")
    void onlyControllersTurnsCount() {
        gd.turnsTakenByPlayer.put(player1.getId(), 2);
        gd.turnsTakenByPlayer.put(player2.getId(), 7);
        Permanent condition = addCreatureReady(player1, new ControlWinCondition());

        assertThat(gqs.getEffectivePower(gd, condition)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, condition)).isEqualTo(2);
    }

    @Test
    @DisplayName("The creature spell cannot be countered")
    void creatureSpellCannotBeCountered() {
        ControlWinCondition condition = new ControlWinCondition();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFromHand(player1, condition, "{4}{U}{U}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, condition.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Control Win Condition");
    }

    @Test
    @DisplayName("Shroud prevents both players from targeting the creature")
    void neitherPlayerCanTargetCreature() {
        gd.turnsTakenByPlayer.put(player1.getId(), 3);
        Permanent condition = addCreatureReady(player1, new ControlWinCondition());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, condition.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passPriority(player1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, condition.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Control Win Condition");
    }

    @Test
    @DisplayName("A controller with no turns taken has a zero-toughness creature")
    void diesWhenControllerHasTakenNoTurns() {
        gd.turnsTakenByPlayer.put(player1.getId(), 0);
        addCreatureReady(player1, new ControlWinCondition());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Control Win Condition");
        harness.assertInGraveyard(player1, "Control Win Condition");
    }

    @Test
    @DisplayName("The characteristic ability works in hand and graveyard using the owner's turns")
    void powerAndToughnessApplyOutsideBattlefield() {
        gd.turnsTakenByPlayer.put(player1.getId(), 4);
        gd.turnsTakenByPlayer.put(player2.getId(), 7);
        ControlWinCondition condition = new ControlWinCondition();
        harness.setHand(player1, List.of(condition));

        assertThat(gqs.getEffectiveCardPower(gd, condition)).isEqualTo(4);
        assertThat(gqs.getEffectiveCardToughness(gd, condition)).isEqualTo(4);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(condition));
        gd.turnsTakenByPlayer.put(player1.getId(), 5);

        assertThat(gqs.getEffectiveCardPower(gd, condition)).isEqualTo(5);
        assertThat(gqs.getEffectiveCardToughness(gd, condition)).isEqualTo(5);
    }

}
