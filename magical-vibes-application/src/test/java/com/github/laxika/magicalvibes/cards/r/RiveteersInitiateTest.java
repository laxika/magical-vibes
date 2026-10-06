package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RiveteersInitiate.class})
class RiveteersInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("The ability can be paid with black mana")
    void gainsDeathtouchWithBlackMana() {
        Permanent initiate = addCreatureReady(player1, new RiveteersInitiate());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The ability can be paid with green mana")
    void gainsDeathtouchWithGreenMana() {
        Permanent initiate = addCreatureReady(player1, new RiveteersInitiate());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent initiate = addCreatureReady(player1, new RiveteersInitiate());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new RiveteersInitiate());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Red mana cannot pay the black or green hybrid symbol")
    void cannotPayHybridSymbolWithRedMana() {
        addCreatureReady(player1, new RiveteersInitiate());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped summoning-sick initiate can activate without tapping")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent initiate = addCreatureReady(player1, new RiveteersInitiate());
        initiate.setSummoningSick(true);
        initiate.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DEATHTOUCH)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DEATHTOUCH)).isTrue();
        assertThat(initiate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the initiate whose ability resolves gains deathtouch")
    void grantsDeathtouchOnlyToSource() {
        Permanent initiate = addCreatureReady(player1, new RiveteersInitiate());
        Permanent otherInitiate = addCreatureReady(player1, new RiveteersInitiate());
        Permanent opposingInitiate = addCreatureReady(player2, new RiveteersInitiate());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherInitiate, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingInitiate, Keyword.DEATHTOUCH)).isFalse();
    }
}
