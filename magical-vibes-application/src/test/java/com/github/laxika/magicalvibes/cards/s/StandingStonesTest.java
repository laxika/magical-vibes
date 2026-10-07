package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StandingStones.class})
class StandingStonesTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T}, Pay 1 life adds one mana of the chosen color")
    void addsChosenColorAfterPayingCost() {
        Permanent standingStones = addStandingStones();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(standingStones.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent standingStones = addStandingStones();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(standingStones.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent standingStones = addStandingStones();
        standingStones.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED"})
    void canProduceEachOtherColorUsingColoredManaForGenericCost(ManaColor color) {
        Permanent standingStones = addStandingStones();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        PendingInteraction.ColorChoice choice =
                (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        harness.assertLife(player1, 19);
        assertThat(standingStones.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutEnoughLifeToPay() {
        Permanent standingStones = addStandingStones();
        harness.setLife(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 0);
        assertThat(standingStones.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addStandingStones() {
        return harness.addToBattlefieldAndReturn(player1, new StandingStones());
    }
}
