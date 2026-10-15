package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaiLiCensor.class})
class DaiLiCensorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives Dai Li Censor +2/+2 until end of turn")
    void sacrificesAnotherCreatureAndBoostsItself() {
        Permanent censor = addCensorReady();
        harness.addToBattlefield(player1, new DaiLiCensor());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(censor.getPowerModifier()).isEqualTo(2);
        assertThat(censor.getToughnessModifier()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Dai Li Censor");
    }

    @Test
    @DisplayName("Dai Li Censor cannot sacrifice itself")
    void cannotSacrificeItself() {
        addCensorReady();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dai Li Censor can be activated only once each turn")
    void onlyOnceEachTurn() {
        addCensorReady();
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new DaiLiCensor());
        harness.addToBattlefield(player1, new DaiLiCensor());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The Dai Li Censor boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent censor = addCensorReady();
        harness.addToBattlefield(player1, new DaiLiCensor());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(censor.getPowerModifier()).isZero();
        assertThat(censor.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The sacrifice is paid before the boost resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent censor = addCensorReady();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DaiLiCensor());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(censor).doesNotContain(sacrifice);
        harness.assertInGraveyard(player1, "Dai Li Censor");
        assertThat(censor.getPowerModifier()).isZero();
        assertThat(censor.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(censor.getPowerModifier()).isEqualTo(2);
        assertThat(censor.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Insufficient mana does not consume the turn's activation")
    void failedPaymentDoesNotConsumeActivation() {
        Permanent censor = addCensorReady();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DaiLiCensor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(censor, sacrifice);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(censor.getPowerModifier()).isEqualTo(2);
        assertThat(censor.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addCensorReady();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DaiLiCensor());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Censor can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent censor = harness.addToBattlefieldAndReturn(player1, new DaiLiCensor());
        censor.setSummoningSick(true);
        censor.tap();
        harness.addToBattlefield(player1, new DaiLiCensor());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(censor.getPowerModifier()).isEqualTo(2);
        assertThat(censor.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The activation limit resets on the opponent's turn")
    void canActivateAgainOnOpponentsTurn() {
        Permanent censor = addCensorReady();
        harness.addToBattlefield(player1, new DaiLiCensor());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.addToBattlefield(player1, new DaiLiCensor());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(censor.getPowerModifier()).isEqualTo(2);
        assertThat(censor.getToughnessModifier()).isEqualTo(2);
    }

    private Permanent addCensorReady() {
        return addCreatureReady(player1, new DaiLiCensor());
    }
}
