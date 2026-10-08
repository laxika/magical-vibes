package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(YavimayaAncients.class)
class YavimayaAncientsTest extends BaseCardTest {

    private Permanent addAncients() {
        return addCreatureReady(player1, new YavimayaAncients());
    }

    @Test
    @DisplayName("Ability gives +1/-2 until end of turn")
    void abilityBoosts() {
        Permanent ancients = addAncients();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(ancients.getEffectivePower()).isEqualTo(3);
        assertThat(ancients.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can activate while summoning sick because the ability does not require tapping")
    void canActivateWhileSummoningSick() {
        Permanent ancients = harness.addToBattlefieldAndReturn(player1, new YavimayaAncients());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ancients.getEffectivePower()).isEqualTo(3);
        assertThat(ancients.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can activate while tapped during an opponent's turn")
    void canActivateWhileTappedOnOpponentsTurn() {
        Permanent ancients = addAncients();
        ancients.setTapped(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ancients.isTapped()).isTrue();
        assertThat(ancients.getEffectivePower()).isEqualTo(3);
        assertThat(ancients.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("A pending activation has no effect after its source dies")
    void pendingActivationDoesNotBoostAnotherAncients() {
        Permanent ancients = addAncients();
        Permanent otherAncients = addAncients();
        harness.addMana(player1, ManaColor.GREEN, 5);

        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }

        assertThat(ancients.getEffectivePower()).isEqualTo(5);
        assertThat(ancients.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ancients);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ancients);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ancients.getCard());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(otherAncients.getPowerModifier()).isZero();
        assertThat(otherAncients.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Activating four times kills it via lethal toughness reduction")
    void repeatedActivationsKillIt() {
        Permanent ancients = addAncients();
        harness.addMana(player1, ManaColor.GREEN, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.clearPriorityPassed();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ancients);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(ancients.getCard());
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent ancients = addAncients();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(ancients.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ancients.getPowerModifier()).isEqualTo(0);
        assertThat(ancients.getToughnessModifier()).isEqualTo(0);
    }
}
