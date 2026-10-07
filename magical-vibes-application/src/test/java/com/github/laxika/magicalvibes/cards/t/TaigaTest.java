package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Taiga.class)
class TaigaTest extends BaseCardTest {

    @Test
    @DisplayName("Taiga rejects a mana color outside its choices")
    void rejectsUnlistedManaColor() {
        addTaigaReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "BLUE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Taiga produces red mana")
    void producesRedMana() {
        Permanent taiga = addTaigaReady();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(taiga.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taiga produces green mana")
    void producesGreenMana() {
        Permanent taiga = addTaigaReady();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(taiga.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taiga's mana ability resolves without using the stack")
    void manaAbilityDoesNotUseStack() {
        addTaigaReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Taiga cannot produce mana again while tapped")
    void cannotActivateWhileTapped() {
        addTaigaReady();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taiga enters untapped and can produce mana the turn it is played")
    void canProduceManaImmediatelyAfterBeingPlayed() {
        harness.setHand(player1, List.of(new Taiga()));

        harness.playLand(player1, 0);

        Permanent taiga = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(taiga.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(taiga.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private Permanent addTaigaReady() {
        return harness.addToBattlefieldAndReturn(player1, new Taiga());
    }
}
