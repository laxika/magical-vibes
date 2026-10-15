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

@CardUsed(ThrivingIsle.class)
class ThrivingIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and offers every color except blue")
    void entersTappedAndChoosesNonBlueColor() {
        harness.setHand(player1, List.of(new ThrivingIsle()));

        harness.playLand(player1, 0);

        Permanent isle = findPermanent(player1, "Thriving Isle");
        assertThat(isle.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLACK", "RED", "GREEN");
        assertThat(choice.options()).doesNotContain("BLUE");

        harness.handleListChoice(player1, "GREEN");

        assertThat(isle.getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("The first mana ability adds blue mana")
    void firstAbilityAddsBlueMana() {
        Permanent isle = addReadyIsle();
        isle.setChosenColor(CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(isle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second mana ability adds mana of the chosen color")
    void secondAbilityAddsChosenColorMana() {
        Permanent isle = addReadyIsle();
        isle.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(isle.isTapped()).isTrue();
    }

    private Permanent addReadyIsle() {
        Permanent isle = harness.addToBattlefieldAndReturn(player1, new ThrivingIsle());
        isle.setSummoningSick(false);
        return isle;
    }

    @ParameterizedTest
    @EnumSource(value = CardColor.class, names = {"WHITE", "BLACK", "RED", "GREEN"})
    @DisplayName("An entry choice produces exactly one mana of that color without using the stack")
    void entryChoiceDeterminesMana(CardColor color) {
        harness.setHand(player1, List.of(new ThrivingIsle()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, color.name());
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor.name().equals(color.name()) ? 1 : 0);
        }
        assertThat(findPermanent(player1, "Thriving Isle").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Blue cannot be chosen even when submitted directly")
    void rejectsBlueChoice() {
        harness.setHand(player1, List.of(new ThrivingIsle()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "BLUE"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "GREEN");
        assertThat(findPermanent(player1, "Thriving Isle").getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Separate Isles retain separate color choices")
    void separateIslesRetainTheirChoices() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new ThrivingIsle());
        harness.handleListChoice(player1, "WHITE");
        Permanent second = harness.enterBattlefieldAndReturn(player1, new ThrivingIsle());
        harness.handleListChoice(player1, "BLACK");
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(first.getChosenColor()).isEqualTo(CardColor.WHITE);
        assertThat(second.getChosenColor()).isEqualTo(CardColor.BLACK);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
