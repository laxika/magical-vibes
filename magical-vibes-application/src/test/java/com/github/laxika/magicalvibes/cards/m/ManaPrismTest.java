package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ManaPrism.class)
class ManaPrismTest extends BaseCardTest {

    // ===== First ability: {T}: Add {C} =====

    @Test
    @DisplayName("First ability taps for one colorless mana")
    void firstAbilityAddsColorless() {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new ManaPrism());
        prism.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(prism.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Second ability: {1}, {T}: Add one mana of any color =====

    @Test
    @DisplayName("Second ability prompts for a color and adds one mana of it")
    void secondAbilityAddsChosenColor() {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new ManaPrism());
        prism.setSummoningSick(false);

        // Pay the {1} activation cost.
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(prism.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Second ability cannot be activated without paying its generic cost")
    void secondAbilityRequiresGenericMana() {
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new ManaPrism());
        prism.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(prism.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
