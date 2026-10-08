package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StreamHopper.class})
class StreamHopperTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {U/R} grants flying until end of turn")
    void payingManaGrantsFlying() {
        Permanent hopper = addCreatureReady(player1, new StreamHopper());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hopper.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent hopper = addCreatureReady(player1, new StreamHopper());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(hopper.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(hopper.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("A summoning-sick creature can activate the ability without tapping")
    void canActivateWhileSummoningSick() {
        Permanent hopper = harness.addToBattlefieldAndReturn(player1, new StreamHopper());
        hopper.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(hopper.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        harness.passBothPriorities();

        assertThat(hopper.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(hopper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped creature can activate and only the source gains flying")
    void tappedSourceGrantsFlyingOnlyToItself() {
        Permanent hopper = addCreatureReady(player1, new StreamHopper());
        Permanent otherHopper = addCreatureReady(player1, new StreamHopper());
        Permanent opposingHopper = addCreatureReady(player2, new StreamHopper());
        hopper.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hopper.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(hopper.isTapped()).isTrue();
        assertThat(otherHopper.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(opposingHopper.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Green mana cannot pay the blue/red hybrid activation cost")
    void cannotPayWithGreenMana() {
        Permanent hopper = addCreatureReady(player1, new StreamHopper());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(hopper.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }
}
