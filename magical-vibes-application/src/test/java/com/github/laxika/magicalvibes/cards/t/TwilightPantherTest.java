package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TwilightPanther.class})
class TwilightPantherTest extends BaseCardTest {

    @Test
    @DisplayName("Activating grants deathtouch until end of turn")
    void activationGrantsDeathtouch() {
        Permanent panther = addCreatureReady(player1, new TwilightPanther());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, panther, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent panther = addCreatureReady(player1, new TwilightPanther());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, panther, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Deathtouch remains during the end step before cleanup")
    void deathtouchRemainsDuringEndStep() {
        Permanent panther = addCreatureReady(player1, new TwilightPanther());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.hasKeyword(gd, panther, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The ability grants deathtouch only to its source when it resolves")
    void grantsDeathtouchOnlyToSourceOnResolution() {
        Permanent panther = addCreatureReady(player1, new TwilightPanther());
        Permanent otherPanther = addCreatureReady(player1, new TwilightPanther());
        Permanent opposingPanther = addCreatureReady(player2, new TwilightPanther());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, panther, Keyword.DEATHTOUCH)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, panther, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherPanther, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingPanther, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Panther can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent panther = addCreatureReady(player1, new TwilightPanther());
        panther.setSummoningSick(true);
        panther.tap();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, panther, Keyword.DEATHTOUCH)).isTrue();
        assertThat(panther.isTapped()).isTrue();
    }

    @Test
    @DisplayName("White mana cannot pay the black activation cost")
    void activationRequiresBlackMana() {
        Permanent panther = addCreatureReady(player1, new TwilightPanther());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, panther, Keyword.DEATHTOUCH)).isFalse();
    }
}
