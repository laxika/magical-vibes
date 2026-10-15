package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowcloakVampire.class})
class ShadowcloakVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life grants flying until end of turn")
    void payLifeGrantsFlying() {
        Permanent vampire = addCreatureReady(player1, new ShadowcloakVampire());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(vampire.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent vampire = addCreatureReady(player1, new ShadowcloakVampire());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(vampire.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(vampire.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot activate with less than 2 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new ShadowcloakVampire());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Ability requires no mana and can be activated repeatedly")
    void canActivateMultipleTimes() {
        Permanent vampire = addCreatureReady(player1, new ShadowcloakVampire());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(vampire.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Life is paid on activation but flying waits for resolution")
    void lifeIsPaidBeforeFlyingIsGranted() {
        Permanent vampire = addCreatureReady(player1, new ShadowcloakVampire());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 18);
        assertThat(vampire.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(vampire.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("A tapped, summoning-sick vampire can activate the ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new ShadowcloakVampire());
        vampire.setSummoningSick(true);
        vampire.tap();
        harness.setLife(player1, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        assertThat(vampire.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(vampire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the vampire whose ability was activated gains flying")
    void grantsFlyingOnlyToSource() {
        Permanent source = addCreatureReady(player1, new ShadowcloakVampire());
        Permanent other = addCreatureReady(player1, new ShadowcloakVampire());
        Permanent opponent = addCreatureReady(player2, new ShadowcloakVampire());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(source.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(opponent.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }
}
