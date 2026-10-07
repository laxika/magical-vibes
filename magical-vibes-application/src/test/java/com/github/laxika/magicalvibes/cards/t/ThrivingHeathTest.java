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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrivingHeath.class})
class ThrivingHeathTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and offers every color except white")
    void entersTappedAndChoosesNonWhiteColor() {
        harness.setHand(player1, List.of(new ThrivingHeath()));

        harness.playLand(player1, 0);

        Permanent heath = findPermanent(player1, "Thriving Heath");
        assertThat(heath.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "BLACK", "RED", "GREEN");
        assertThat(choice.options()).doesNotContain("WHITE");

        harness.handleListChoice(player1, "BLUE");

        assertThat(heath.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("The first mana ability adds white mana")
    void firstAbilityAddsWhiteMana() {
        Permanent heath = addReadyHeath();
        heath.setChosenColor(CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(heath.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second mana ability adds mana of the chosen color")
    void secondAbilityAddsChosenColorMana() {
        Permanent heath = addReadyHeath();
        heath.setChosenColor(CardColor.GREEN);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(heath.isTapped()).isTrue();
    }

    private Permanent addReadyHeath() {
        return harness.addToBattlefieldAndReturn(player1, new ThrivingHeath());
    }

    @ParameterizedTest
    @EnumSource(value = CardColor.class, names = {"BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Each permitted entry choice produces its color without using the stack")
    void entryChoiceDeterminesMana(CardColor color) {
        harness.setHand(player1, List.of(new ThrivingHeath()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, color.name());
        assertThat(gd.stack).isEmpty();

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color.name())))
                .isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Thriving Heath").isTapped()).isTrue();
    }

    @Test
    @DisplayName("White cannot be chosen as Thriving Heath enters")
    void rejectsWhiteEntryChoice() {
        harness.setHand(player1, List.of(new ThrivingHeath()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "WHITE"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(findPermanent(player1, "Thriving Heath").getChosenColor()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "RED");
        assertThat(findPermanent(player1, "Thriving Heath").getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Neither mana option can be used while the land is tapped")
    void cannotProduceManaWhileTapped() {
        harness.setHand(player1, List.of(new ThrivingHeath()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "BLACK");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
