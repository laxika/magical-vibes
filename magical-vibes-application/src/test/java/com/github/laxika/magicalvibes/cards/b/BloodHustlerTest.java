package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BloodHustler.class)
class BloodHustlerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller commits a crime")
    void putsCounterOnCrime() {
        Permanent hustler = harness.addToBattlefieldAndReturn(player1, new BloodHustler());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());

        activateDrain(hustler);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hustler)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
    }

    @Test
    @DisplayName("The crime trigger fires only once each turn")
    void crimeTriggerFiresOnlyOnceEachTurn() {
        Permanent hustler = harness.addToBattlefieldAndReturn(player1, new BloodHustler());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        activateDrain(hustler);
        harness.passBothPriorities();
        harness.passBothPriorities();
        activateDrain(hustler);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hustler)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target yourself with the drain ability")
    void cannotTargetYourself() {
        harness.addToBattlefield(player1, new BloodHustler());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateDrain(Permanent hustler) {
        harness.activateAbility(player1, indexOf(player1, hustler), null, player2.getId());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
