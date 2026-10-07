package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiteflameWitch.class})
class SpiteflameWitchTest extends BaseCardTest {

    @Test
    @DisplayName("{B}{R}: each player loses 1 life")
    void abilityMakesEachPlayerLoseOneLife() {
        addCreatureReady(player1, new SpiteflameWitch());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, player1LifeBefore - 1);
        harness.assertLife(player2, player2LifeBefore - 1);
    }

    @Test
    void canActivateWhileSummoningSickAndTapped() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new SpiteflameWitch());
        witch.setSummoningSick(true);
        witch.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(witch.isTapped()).isTrue();
    }

    @Test
    void canActivateTwiceAndLifeLossWaitsForResolution() {
        addCreatureReady(player1, new SpiteflameWitch());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        addCreatureReady(player1, new SpiteflameWitch());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLACK", "RED"})
    void cannotPayActivationWithTwoManaOfOnlyOneRequiredColor(ManaColor color) {
        addCreatureReady(player1, new SpiteflameWitch());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, color, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
