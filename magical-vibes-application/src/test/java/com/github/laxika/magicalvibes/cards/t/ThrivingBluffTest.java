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

@CardUsed({ThrivingBluff.class})
class ThrivingBluffTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and offers every color except red")
    void entersTappedAndChoosesNonRedColor() {
        harness.setHand(player1, List.of(new ThrivingBluff()));

        harness.playLand(player1, 0);

        Permanent bluff = findPermanent(player1, "Thriving Bluff");
        assertThat(bluff.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "GREEN");
        assertThat(choice.options()).doesNotContain("RED");

        harness.handleListChoice(player1, "BLUE");

        assertThat(bluff.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("The first mana ability adds red mana")
    void firstAbilityAddsRedMana() {
        Permanent bluff = addReadyBluff();
        bluff.setChosenColor(CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(bluff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second mana ability adds mana of the chosen color")
    void secondAbilityAddsChosenColorMana() {
        Permanent bluff = addReadyBluff();
        bluff.setChosenColor(CardColor.GREEN);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(bluff.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = CardColor.class, names = {"WHITE", "BLUE", "BLACK", "GREEN"})
    @DisplayName("Each allowed entrance choice produces exactly one mana immediately")
    void chosenColorProducesManaAfterEntering(CardColor color) {
        Permanent bluff = harness.enterBattlefieldAndReturn(player1, new ThrivingBluff());
        assertThat(bluff.isTapped()).isTrue();
        harness.handleListChoice(player1, color.name());
        assertThat(gd.stack).isEmpty();
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor.name().equals(color.name()) ? 1 : 0);
        }
        assertThat(bluff.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Red cannot be submitted as the entrance color choice")
    void rejectsRedChoice() {
        Permanent bluff = harness.enterBattlefieldAndReturn(player1, new ThrivingBluff());

        assertThatThrownBy(() -> harness.handleListChoice(player1, "RED"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(bluff.getChosenColor()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLUE");
        assertThat(bluff.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Each Bluff remembers its own chosen color")
    void multipleBluffsKeepIndependentChoices() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new ThrivingBluff());
        harness.handleListChoice(player1, "WHITE");
        Permanent second = harness.enterBattlefieldAndReturn(player1, new ThrivingBluff());
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

    private Permanent addReadyBluff() {
        Permanent bluff = harness.addToBattlefieldAndReturn(player1, new ThrivingBluff());
        bluff.setSummoningSick(false);
        return bluff;
    }
}
