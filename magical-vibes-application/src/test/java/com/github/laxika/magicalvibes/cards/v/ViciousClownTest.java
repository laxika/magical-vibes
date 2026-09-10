package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViciousClown.class, GrizzlyBears.class, LeatherbackBaloth.class})
class ViciousClownTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 when another creature you control with power 2 or less enters")
    void getsBoostWhenLowPowerCreatureEnters() {
        Permanent clown = addClown();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllStackEntries();

        assertThat(gqs.getEffectivePower(gd, clown)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, clown)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for a creature with power greater than 2")
    void doesNotTriggerForHighPowerCreature() {
        Permanent clown = addClown();

        harness.setHand(player1, List.of(new LeatherbackBaloth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        resolveAllStackEntries();

        assertThat(gqs.getEffectivePower(gd, clown)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, clown)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for a creature entering under an opponent's control")
    void doesNotTriggerForOpponentCreature() {
        Permanent clown = addClown();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        resolveAllStackEntries();

        assertThat(gqs.getEffectivePower(gd, clown)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent clown = addClown();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllStackEntries();
        assertThat(gqs.getEffectivePower(gd, clown)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, clown)).isEqualTo(2);
    }

    private void resolveAllStackEntries() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private Permanent addClown() {
        harness.addToBattlefield(player1, new ViciousClown());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
