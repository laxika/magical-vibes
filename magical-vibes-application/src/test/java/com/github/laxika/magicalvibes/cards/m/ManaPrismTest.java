package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManaPrismTest extends BaseCardTest {

    // ===== First ability: {T}: Add {C} =====

    @Test
    @DisplayName("First ability taps for one colorless mana")
    void firstAbilityAddsColorless() {
        harness.addToBattlefield(player1, new ManaPrism());
        Permanent prism = gd.playerBattlefields.get(player1.getId()).getFirst();
        prism.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(prism.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Second ability: {1}, {T}: Add one mana of any color =====

    @Test
    @DisplayName("Second ability prompts for a color and adds one mana of it")
    void secondAbilityAddsChosenColor() {
        harness.addToBattlefield(player1, new ManaPrism());
        Permanent prism = gd.playerBattlefields.get(player1.getId()).getFirst();
        prism.setSummoningSick(false);

        // Pay the {1} activation cost.
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(prism.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
