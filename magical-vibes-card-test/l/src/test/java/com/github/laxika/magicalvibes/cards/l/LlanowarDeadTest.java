package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LlanowarDead.class)
class LlanowarDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Llanowar Dead produces one black mana")
    void tappingProducesBlackMana() {
        Permanent dead = addCreatureReady(player1, new LlanowarDead());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(dead.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Llanowar Dead cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent dead = harness.addToBattlefieldAndReturn(player1, new LlanowarDead());
        dead.setSummoningSick(true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(dead.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana is available immediately without using the stack")
    void manaIsAvailableImmediately() {
        addCreatureReady(player1, new LlanowarDead());

        harness.tapPermanent(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("An already tapped Llanowar Dead cannot produce more mana")
    void cannotActivateAgainWhileTapped() {
        Permanent dead = addCreatureReady(player1, new LlanowarDead());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dead.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Llanowar Dead produces mana for its controller")
    void opponentControlledCopyProducesManaForOpponent() {
        Permanent dead = addCreatureReady(player2, new LlanowarDead());

        harness.tapPermanent(player2, 0);

        assertThat(dead.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}