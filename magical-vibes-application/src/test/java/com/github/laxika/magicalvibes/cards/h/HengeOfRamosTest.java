package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HengeOfRamosTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Henge of Ramos adds colorless mana without using the stack")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new HengeOfRamos());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent henge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(henge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying two mana lets Henge of Ramos produce a chosen color")
    void payTwoManaForAnyColor() {
        harness.addToBattlefield(player1, new HengeOfRamos());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        Permanent henge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(henge.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The any-color ability cannot be activated without paying two mana")
    void anyColorAbilityRequiresTwoMana() {
        harness.addToBattlefield(player1, new HengeOfRamos());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
