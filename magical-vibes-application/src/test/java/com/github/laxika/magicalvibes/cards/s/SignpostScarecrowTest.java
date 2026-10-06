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

@CardUsed(SignpostScarecrow.class)
class SignpostScarecrowTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one mana of the chosen color without tapping Signpost Scarecrow")
    void manaAbilityAddsChosenColor() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new SignpostScarecrow());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(scarecrow.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void manaAbilityCanProduceEveryColorWhileTappedAndSummoningSick(ManaColor color) {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new SignpostScarecrow());
        scarecrow.tap();
        scarecrow.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(scarecrow.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityCanBeActivatedRepeatedlyByPayingEachTime() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new SignpostScarecrow());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(scarecrow.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityCannotUseItsOwnOutputToPayItsCost() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new SignpostScarecrow());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(scarecrow.isTapped()).isFalse();
    }
}
