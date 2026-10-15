package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KabiraCrossroads.class})
class KabiraCrossroadsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 2 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new KabiraCrossroads()));

        harness.playLand(player1, 0);

        Permanent crossroads = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(crossroads.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Tapping for mana adds white mana")
    void tapsForWhiteMana() {
        Permanent crossroads = harness.addToBattlefieldAndReturn(player1, new KabiraCrossroads());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(crossroads.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot produce mana while tapped after entering")
    void cannotProduceManaWhileTapped() {
        harness.setHand(player1, List.of(new KabiraCrossroads()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering without being played gains life only for its controller on resolution")
    void enteringWithoutBeingPlayedGainsLifeForController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        Permanent crossroads = harness.enterBattlefieldAndReturn(player2, new KabiraCrossroads());

        assertThat(crossroads.isTapped()).isTrue();
        harness.assertLife(player2, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 12);
        assertThat(gd.stack).isEmpty();
    }
}
