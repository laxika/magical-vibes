package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HardbristleBandit.class, Shock.class})
class HardbristleBanditTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds one mana of the chosen color")
    void tapsForAnyColor() {
        Permanent bandit = addReadyBandit();

        tapForMana(bandit, ManaColor.BLUE);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bandit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps after its controller commits a crime")
    void untapsAfterCrime() {
        Permanent bandit = addReadyBandit();
        tapForMana(bandit, ManaColor.GREEN);

        commitCrime();

        assertThat(bandit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The crime trigger untaps it only once each turn")
    void crimeTriggerFiresOnlyOnceEachTurn() {
        Permanent bandit = addReadyBandit();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        tapForMana(bandit, ManaColor.GREEN);
        castCrimeSpell();
        tapForMana(bandit, ManaColor.WHITE);
        castCrimeSpell();

        assertThat(bandit.isTapped()).isTrue();
    }

    private void tapForMana(Permanent bandit, ManaColor color) {
        int banditIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bandit);
        harness.activateAbility(player1, banditIndex, 0, null, null);
        harness.handleListChoice(player1, color.name());
    }

    private Permanent addReadyBandit() {
        Permanent bandit = harness.addToBattlefieldAndReturn(player1, new HardbristleBandit());
        bandit.setSummoningSick(false);
        return bandit;
    }

    private void commitCrime() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        castCrimeSpell();
    }

    private void castCrimeSpell() {
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
