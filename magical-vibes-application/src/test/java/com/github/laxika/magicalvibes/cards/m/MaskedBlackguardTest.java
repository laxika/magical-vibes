package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaskedBlackguard.class})
class MaskedBlackguardTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during the opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new MaskedBlackguard()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        gs.passPriority(gd, player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving the ability gives +1/+1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent blackguard = addCreatureReady(player1, new MaskedBlackguard());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(blackguard.getPowerModifier()).isEqualTo(1);
        assertThat(blackguard.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations give a cumulative boost")
    void repeatedActivationsStack() {
        Permanent blackguard = addCreatureReady(player1, new MaskedBlackguard());
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(blackguard.getPowerModifier()).isEqualTo(2);
        assertThat(blackguard.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent blackguard = addCreatureReady(player1, new MaskedBlackguard());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blackguard.getPowerModifier()).isZero();
        assertThat(blackguard.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent blackguard = harness.addToBattlefieldAndReturn(player1, new MaskedBlackguard());
        blackguard.setSummoningSick(true);
        blackguard.tap();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(blackguard.getPowerModifier()).isEqualTo(1);
        assertThat(blackguard.getToughnessModifier()).isEqualTo(1);
        assertThat(blackguard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires black mana")
    void cannotActivateWithoutBlackMana() {
        Permanent blackguard = addCreatureReady(player1, new MaskedBlackguard());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(blackguard.getPowerModifier()).isZero();
        assertThat(blackguard.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An ability whose source has left does not boost another Blackguard")
    void departedSourceDoesNotBoostAnotherBlackguard() {
        Permanent source = addCreatureReady(player1, new MaskedBlackguard());
        Permanent other = addCreatureReady(player1, new MaskedBlackguard());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }
}
