package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ThrivingGrove.class)
class ThrivingGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and offers every color except green")
    void entersTappedAndChoosesNonGreenColor() {
        harness.setHand(player1, List.of(new ThrivingGrove()));

        harness.playLand(player1, 0);

        Permanent grove = findPermanent(player1, "Thriving Grove");
        assertThat(grove.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED");
        assertThat(choice.options()).doesNotContain("GREEN");

        harness.handleListChoice(player1, "BLUE");

        assertThat(grove.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("The first mana ability adds green mana")
    void firstAbilityAddsGreenMana() {
        Permanent grove = addReadyGrove();
        grove.setChosenColor(CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(grove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second mana ability adds mana of the chosen color")
    void secondAbilityAddsChosenColorMana() {
        Permanent grove = addReadyGrove();
        grove.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(grove.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = CardColor.class, names = {"WHITE", "BLUE", "BLACK", "RED"})
    @DisplayName("Each legal entry choice produces exactly one mana of that color without using the stack")
    void entryChoiceDeterminesMana(CardColor color) {
        harness.setHand(player1, List.of(new ThrivingGrove()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, color.name());
        Permanent grove = findPermanent(player1, "Thriving Grove");
        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 1, null, null);

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor.name().equals(color.name()) ? 1 : 0);
        }
        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"GREEN", "COLORLESS"})
    @DisplayName("Green and colorless cannot be chosen on entry")
    void rejectsIllegalEntryColor(String color) {
        harness.setHand(player1, List.of(new ThrivingGrove()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handleListChoice(player1, color))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "WHITE");
        assertThat(findPermanent(player1, "Thriving Grove").getChosenColor()).isEqualTo(CardColor.WHITE);
    }

    private Permanent addReadyGrove() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new ThrivingGrove());
        grove.setSummoningSick(false);
        return grove;
    }
}
